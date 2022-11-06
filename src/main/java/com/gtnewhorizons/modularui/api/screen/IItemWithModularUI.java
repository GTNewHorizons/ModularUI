package com.gtnewhorizons.modularui.api.screen;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface for your {@link net.minecraft.item.Item}
 * to display Modular UI
 */
public interface IItemWithModularUI {

    ModularWindow createWindow(UIBuildContext buildContext, ItemStack heldStack);
}
