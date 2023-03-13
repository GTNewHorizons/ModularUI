package com.gtnewhorizons.modularui.api.forge;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IItemHandler {

    int getSlots();

    ItemStack getStackInSlot(int var1);

    @Nullable
    ItemStack insertItem(int var1, ItemStack var2, boolean var3);

    @Nullable
    ItemStack extractItem(int var1, int var2, boolean var3);

    int getSlotLimit(int var1);

    default boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    default List<ItemStack> getStacks() {
        List<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < getSlots(); i++) {
            ret.add(getStackInSlot(i));
        }
        return ret;
    }

    /**
     * Get the inventory this slot originates from. Only supposed to be an identifier. Avoid callings methods on this
     * inventory directly beyond comparing its identity.
     * 
     * @return the source inventory, or null if not from any {@link IInventory}, or the source inventory information has
     *         been lost.
     */
    @Nullable
    default IInventory getSourceInventory() {
        return null;
    }
}
