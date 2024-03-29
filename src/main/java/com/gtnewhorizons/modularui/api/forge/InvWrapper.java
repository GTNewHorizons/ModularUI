package com.gtnewhorizons.modularui.api.forge;

import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class InvWrapper implements IItemHandlerModifiable {

    private final IInventory inv;

    public InvWrapper(IInventory inv) {
        this.inv = Objects.requireNonNull(inv);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvWrapper that = (InvWrapper) o;

        return getSourceInventory().equals(that.getSourceInventory());
    }

    @Override
    public int hashCode() {
        return getSourceInventory().hashCode();
    }

    @Override
    public int getSlots() {
        return getSourceInventory().getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return getSourceInventory().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null) return null;

        ItemStack stackInSlot = getSourceInventory().getStackInSlot(slot);

        int m;
        if (stackInSlot != null) {
            if (stackInSlot.stackSize >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot))) return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) return stack;

            if (!getSourceInventory().isItemValidForSlot(slot, stack)) return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.stackSize;

            if (stack.stackSize <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.stackSize += stackInSlot.stackSize;
                    getSourceInventory().setInventorySlotContents(slot, copy);
                    getSourceInventory().markDirty();
                }

                return null;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.splitStack(m);
                    copy.stackSize += stackInSlot.stackSize;
                    getSourceInventory().setInventorySlotContents(slot, copy);
                    getSourceInventory().markDirty();
                } else {
                    stack.stackSize -= m;
                }
                return stack;
            }
        } else {
            if (!getSourceInventory().isItemValidForSlot(slot, stack)) return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (m < stack.stackSize) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    getSourceInventory().setInventorySlotContents(slot, stack.splitStack(m));
                    getSourceInventory().markDirty();
                } else {
                    stack.stackSize -= m;
                }
                return stack;
            } else {
                if (!simulate) {
                    getSourceInventory().setInventorySlotContents(slot, stack);
                    getSourceInventory().markDirty();
                }
                return null;
            }
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return null;

        ItemStack stackInSlot = getSourceInventory().getStackInSlot(slot);

        if (stackInSlot == null) return null;

        if (simulate) {
            if (stackInSlot.stackSize < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                copy.stackSize = amount;
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.stackSize, amount);

            ItemStack decrStackSize = getSourceInventory().decrStackSize(slot, m);
            getSourceInventory().markDirty();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        getSourceInventory().setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getSourceInventory().getInventoryStackLimit();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return getSourceInventory().isItemValidForSlot(slot, stack);
    }

    @NotNull
    @Override
    public IInventory getSourceInventory() {
        return inv;
    }

    @Deprecated
    public IInventory getInv() {
        return inv;
    }
}
