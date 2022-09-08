package com.gtnewhorizons.modularui.api.screen;

/**
 * Implement this interface for your {@link net.minecraft.tileentity.TileEntity}
 * to display Modular UI
 */
public interface ITileWithModularUI {

    ModularWindow createWindow(UIBuildContext buildContext);
}
