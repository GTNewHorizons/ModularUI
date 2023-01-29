package com.gtnewhorizons.modularui.api;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyboardUtil {

    /**
     * Returns true if either windows ctrl key is down or if either mac meta key is down
     */
    public static boolean isCtrlKeyDown() {
        return GuiScreen.isCtrlKeyDown();
    }

    /**
     * Returns true if either shift key is down
     */
    public static boolean isShiftKeyDown() {
        return GuiScreen.isShiftKeyDown();
    }

    /**
     * Returns true if either alt key is down
     */
    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static boolean isKeyComboCtrlX(int keyID) {
        return keyID == Keyboard.KEY_X && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlV(int keyID) {
        return keyID == Keyboard.KEY_V && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlC(int keyID) {
        return keyID == Keyboard.KEY_C && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    public static boolean isKeyComboCtrlA(int keyID) {
        return keyID == Keyboard.KEY_A && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }
}
