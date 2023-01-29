package com.gtnewhorizons.modularui.api.forge;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import javax.annotation.Nullable;

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
}
