package com.gtnewhorizons.modularui.common.peripheral;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.guihook.IContainerInputHandler;

import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;

public class ModularUIPeripheralInputHandler implements IContainerInputHandler {

    @Override
    public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode) {
        return false;
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {}

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyID) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
        return false;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
        if (gui instanceof ModularGui && scrolled != 0) {
            return ((ModularGui) gui).mouseScrolled(scrolled);
        }
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
        if (gui instanceof ModularGui && scrolled != 0) {
            ((ModularGui) gui).onMouseScrolled(scrolled);
        }
    }

    @Override
    public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime) {}
}
