package com.gtnewhorizons.modularui.common.internal.wrapper;

import com.gtnewhorizons.modularui.api.forge.ItemHandlerHelper;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ModularUIContainer extends Container {

    private final ModularUIContext context;
    private boolean initialisedContainer = false;
    private final List<BaseSlot> sortedShiftClickSlots = new ArrayList<>();

    private final Map<String, List<Slot>> sortingAreas = new HashMap<>();
    private final Map<String, Integer> sortRowSizes = new HashMap<>();

    public ModularUIContainer(ModularUIContext context, ModularWindow mainWindow) {
        this.context = context;
        this.context.initialize(this, mainWindow);
        checkSlotIds();
        sortSlots();
        initialisedContainer = true;
    }

    public void sortSlots() {
        this.sortedShiftClickSlots.sort(Comparator.comparingInt(BaseSlot::getShiftClickPriority));
    }

    public ModularUIContext getContext() {
        return context;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityAlive();
    }

    private void checkSlotIds() {
        for (int i = 0; i < inventorySlots.size(); i++) {
            ((Slot) (inventorySlots.get(i))).slotNumber = i;
        }
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        if (slotIn instanceof BaseSlot && ((BaseSlot) slotIn).getShiftClickPriority() > Integer.MIN_VALUE) {
            sortedShiftClickSlots.add((BaseSlot) slotIn);
        }
        Slot ret = super.addSlotToContainer(slotIn);

        if (initialisedContainer) {
            sortSlots();
        }
        return ret;
    }

    public void removeSlot(Slot slot) {
        if (slot != inventorySlots.get(slot.slotNumber)) {
            throw new IllegalStateException("Could not find slot in container!");
        }
        inventorySlots.remove(slot.slotNumber);

        if (slot instanceof BaseSlot && sortedShiftClickSlots.remove(slot) && initialisedContainer) {
            sortSlots();
        }
        checkSlotIds();
        for (List<Slot> slots : sortingAreas.values()) {
            slots.removeIf(slot1 -> slot1 == slot);
        }
    }

    public void setRowSize(String sortArea, int size) {
        sortRowSizes.put(sortArea, size);
    }

    public void setSlotSortable(String area, BaseSlot slot) {
        if (slot != inventorySlots.get(slot.slotNumber)) {
            throw new IllegalArgumentException("Slot is not at the expected index!");
        }
        this.sortingAreas.computeIfAbsent(area, section1 -> new ArrayList<>()).add(slot);
    }

    @Override
    public void detectAndSendChanges() {
        if (getContext().isClient()) return; // this can actually happen with Container#slotClick
        super.detectAndSendChanges();
        if (context.getValidator() != null && !context.getValidator().get()) {
            context.tryClose();
        }
        // ModularWindow#serverUpdate calls ISyncedWidget#detectAndSendChanges,
        // and GT DataControllerWidget might close window.
        this.context.forEachWindowTopToBottom(window -> {
            if (window.isInitialized()) {
                // do not allow syncing before the client is initialized
                window.serverUpdate();
            }
        });
    }

    public void sendSlotChange(ItemStack stack, int index) {
        for (Object listener : this.crafters) {
            ((ICrafting) (listener)).sendSlotContents(this, index, stack);
        }
    }

    @Override
    public void putStackInSlot(int p_75141_1_, ItemStack p_75141_2_) {
        try {
            super.putStackInSlot(p_75141_1_, p_75141_2_);
        } catch (IndexOutOfBoundsException e) {
            // This can legitimately happen (though not easy to reproduce):
            // 1. player opens container
            // 2. soon after that player clicks button that shows another window which contains slot
            // 3. server sends S2FPacketSetSlot for the said slot, requested by #detectAndSendChanges
            // 4. but client hasn't finished init and blow up
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, int clickTypeIn, EntityPlayer player) {
        if (slotId >= 0 && slotId < this.inventorySlots.size()) {
            Slot slot = (Slot) this.inventorySlots.get(slotId);
            if (slot instanceof BaseSlot && !((BaseSlot) slot).isEnabled()) return null;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        if (slot instanceof BaseSlot && !((BaseSlot) slot).isEnabled()) return false;
        return super.canDragIntoSlot(slot);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = (Slot) (this.inventorySlots.get(index));
        if (slot instanceof BaseSlot && !((BaseSlot) slot).isPhantom()) {
            if (!slot.canTakeStack(playerIn) || !((BaseSlot) slot).isEnabled()) return null;
            ItemStack stack = slot.getStack();
            if (stack != null) {
                final ItemStack original = stack.copy();
                ItemStack remainder = transferItem((BaseSlot) slot, stack.copy());
                stack.stackSize = remainder.stackSize;
                if (stack.stackSize < 1) {
                    slot.putStack(null);
                }
                if (!ItemStack.areItemStacksEqual(original, remainder)) {
                    return original;
                }
            }
        }
        return null;
    }

    protected ItemStack transferItem(BaseSlot fromSlot, ItemStack fromStack) {
        // slice slots based on priorities and handle transfer for each same-priority slots
        List<List<BaseSlot>> priorityList = new ArrayList<>();
        List<BaseSlot> tmpSlots = new ArrayList<>();
        int lastPriority = Integer.MAX_VALUE;
        for (BaseSlot slot : this.sortedShiftClickSlots) {
            if (fromSlot.getShiftClickPriority() == slot.getShiftClickPriority()
                    && fromSlot.getItemHandler() == slot.getItemHandler()) continue;
            if (!slot.canInsert || !slot.isItemValidPhantom(fromStack) || !slot.isEnabled()) continue;
            if (lastPriority != slot.getShiftClickPriority() && !tmpSlots.isEmpty()) {
                priorityList.add(tmpSlots);
                tmpSlots = new ArrayList<>();
            }
            lastPriority = slot.getShiftClickPriority();
            tmpSlots.add(slot);
        }
        if (!tmpSlots.isEmpty()) {
            priorityList.add(tmpSlots);
        }

        for (List<BaseSlot> samePrioritySlots : priorityList) {
            // try to merge to existing stacks first
            for (BaseSlot toSlot : samePrioritySlots) {
                ItemStack toStack = toSlot.getStack();
                if (toSlot.isPhantom()) {
                    if (ItemHandlerHelper.canItemStacksStackRelaxed(fromStack, toStack)) {
                        ItemStack toPush = fromStack.copy();
                        if (!((SlotWidget) toSlot.getParentWidget()).canControlAmount()) {
                            toPush.stackSize = 1;
                        } else {
                            toPush.stackSize = Math.min(fromStack.stackSize, toSlot.getItemStackLimit(fromStack));
                        }
                        toSlot.putStack(toPush);
                        return fromStack;
                    }
                } else if (toStack != null) {
                    int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());
                    if (ItemHandlerHelper.canItemStacksStackRelaxed(fromStack, toStack)) {
                        int toMove = Math.max(Math.min(maxSize - toStack.stackSize, fromStack.stackSize), 0);
                        fromStack.stackSize -= toMove;
                        toStack.stackSize += toMove;
                        toSlot.onSlotChanged();
                        if (fromStack.stackSize < 1) {
                            return fromStack;
                        }
                    }
                }
            }

            // push to empty slots
            for (BaseSlot toSlot : samePrioritySlots) {
                ItemStack toStack = toSlot.getStack();
                if (toStack != null) continue;
                if (toSlot.isPhantom()) {
                    ItemStack toPush = fromStack.copy();
                    if (!((SlotWidget) toSlot.getParentWidget()).canControlAmount()) {
                        toPush.stackSize = 1;
                    } else {
                        toPush.stackSize = Math.min(fromStack.stackSize, toSlot.getItemStackLimit(fromStack));
                    }
                    toSlot.putStack(toPush);
                    return fromStack;
                } else {
                    int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());
                    int toMove = Math.min(maxSize, fromStack.stackSize);
                    ItemStack newStack = fromStack.copy();
                    fromStack.stackSize -= toMove;
                    newStack.stackSize = toMove;
                    toSlot.putStack(newStack);
                    if (fromStack.stackSize < 1) {
                        return fromStack;
                    }
                }
            }
        }
        return fromStack;
    }
}
