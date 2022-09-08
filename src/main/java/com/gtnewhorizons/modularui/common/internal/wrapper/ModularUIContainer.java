package com.gtnewhorizons.modularui.common.internal.wrapper;

import com.gtnewhorizons.modularui.api.forge.ItemHandlerHelper;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.*;

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
            ((Slot)(inventorySlots.get(i))).slotNumber = i;
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
        super.detectAndSendChanges();
        for (ModularWindow window : this.context.getOpenWindows()) {
            if (window.isInitialized()) {
                // do not allow syncing before the client is initialized
                window.serverUpdate();
            }
        }
    }

    public void sendSlotChange(ItemStack stack, int index) {
        for (Object listener : this.crafters) {
            ((ICrafting)(listener)).sendSlotContents(this, index, stack);
        }
    }

//    public void sendHeldItemUpdate() {
//        for (Object listener : this.crafters) {
//            if (listener instanceof EntityPlayerMP) {
//                EntityPlayerMP player = (EntityPlayerMP) listener;
//                player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
//            }
//        }
//    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = (Slot)(this.inventorySlots.get(index));
        if (slot instanceof BaseSlot && !((BaseSlot) slot).isPhantom()) {
            ItemStack stack = slot.getStack();
            if (stack != null) {
                ItemStack remainder = transferItem((BaseSlot) slot, stack.copy());
                stack.stackSize = remainder.stackSize;
                if (stack.stackSize < 1) {
                    slot.putStack(null);
                }
                return null;
            }
        }
        return null;
    }

    protected ItemStack transferItem(BaseSlot fromSlot, ItemStack stack) {
        for (BaseSlot slot : this.sortedShiftClickSlots) {
            if (fromSlot.getShiftClickPriority() != slot.getShiftClickPriority() && slot.canTake && slot.isItemValidPhantom(stack)) {
                ItemStack itemstack = slot.getStack();
                if (slot.isPhantom()) {
                    if (itemstack == null || (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack) && itemstack.stackSize < slot.getItemStackLimit(itemstack))) {
                        slot.putStack(stack.copy());
                        return stack;
                    }
                } else {
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (itemstack == null) {
                        int toMove = Math.min(maxSize, stack.stackSize);
                        ItemStack newStack = stack.copy();
                        stack.stackSize -= toMove;
                        newStack.stackSize = toMove;
                        slot.putStack(newStack);
                    } else if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack)) {
                        int toMove = Math.max(Math.min(maxSize - itemstack.stackSize, stack.stackSize), 0);
                        stack.stackSize -= toMove;
                        itemstack.stackSize += toMove;
                        slot.onSlotChanged();
                    }

                    if (stack.stackSize < 1) {
                        return stack;
                    }
                }
            }
        }
        for (Slot slot1 : this.sortedShiftClickSlots) {
            if (!(slot1 instanceof BaseSlot)) {
                continue;
            }
            BaseSlot slot = (BaseSlot) slot1;
            ItemStack itemstack = slot.getStack();
            if (fromSlot.getItemHandler() != slot.getItemHandler() && slot.canInsert && itemstack == null && slot.isItemValid(stack)) {
                if (stack.stackSize > slot1.getSlotStackLimit()) {
                    slot.putStack(stack.splitStack(slot.getSlotStackLimit()));
                } else {
                    slot.putStack(stack.splitStack(stack.stackSize));
                }
                break;
            }
        }
        return stack;
    }
}
