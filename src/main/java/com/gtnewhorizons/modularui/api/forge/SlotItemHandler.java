package com.gtnewhorizons.modularui.api.forge;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotItemHandler extends Slot {
    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    public boolean isItemValid(ItemStack stack) {
        if (stack != null && this.itemHandler.isItemValid(this.index, stack)) {
            IItemHandler handler = this.getItemHandler();
            ItemStack remainder;
            if (handler instanceof IItemHandlerModifiable) {
                IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;
                ItemStack currentStack = handlerModifiable.getStackInSlot(this.index);
                handlerModifiable.setStackInSlot(this.index, null);
                remainder = handlerModifiable.insertItem(this.index, stack, true);
                handlerModifiable.setStackInSlot(this.index, currentStack);
            } else {
                remainder = handler.insertItem(this.index, stack, true);
            }

            return remainder != null ? remainder.stackSize < stack.stackSize : stack.stackSize > 0;
        } else {
            return false;
        }
    }

    public ItemStack getStack() {
        return this.getItemHandler().getStackInSlot(this.index);
    }

    public void putStack(ItemStack stack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(this.index, stack);
        this.onSlotChanged();
    }

    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {}

    public int getSlotStackLimit() {
        return this.itemHandler.getSlotLimit(this.index);
    }

    public int getItemStackLimit(ItemStack stack) {
        ItemStack maxAdd = stack.copy();
        int maxInput = stack.getMaxStackSize();
        maxAdd.stackSize = maxInput;
        IItemHandler handler = this.getItemHandler();
        ItemStack currentStack = handler.getStackInSlot(this.index);
        if (handler instanceof IItemHandlerModifiable) {
            IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;
            handlerModifiable.setStackInSlot(this.index, null);
            ItemStack remainder = handlerModifiable.insertItem(this.index, maxAdd, true);
            handlerModifiable.setStackInSlot(this.index, currentStack);
            return remainder != null ? maxInput - remainder.stackSize : maxInput;
        } else {
            ItemStack remainder = handler.insertItem(this.index, maxAdd, true);
            int current = currentStack.stackSize;
            int added = remainder != null ? maxInput - remainder.stackSize : maxInput;
            return current + added;
        }
    }

    public boolean canTakeStack(EntityPlayer playerIn) {
        return this.getItemHandler().extractItem(this.index, 1, true) != null;
    }

    @Nullable
    public ItemStack decrStackSize(int amount) {
        return this.getItemHandler().extractItem(this.index, amount, false);
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
    }
}
