package com.gtnewhorizons.modularui.integration.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.guihook.IContainerObjectHandler;

import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;

public class ModularUIContainerObjectHandler implements IContainerObjectHandler {

    @Override
    public void guiTick(GuiContainer gui) {}

    @Override
    public void refresh(GuiContainer gui) {}

    @Override
    public void load(GuiContainer gui) {}

    @Override
    public ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (gui instanceof ModularGui) {
            Widget hovered = ((ModularGui) gui).getCursor().getHovered();
            if (hovered instanceof IHasStackUnderMouse) {
                return ((IHasStackUnderMouse) hovered).getStackUnderMouse();
            }
        }
        return null;
    }

    @Override
    public boolean objectUnderMouse(GuiContainer gui, int mousex, int mousey) {
        return false;
    }

    @Override
    public boolean shouldShowTooltip(GuiContainer gui) {
        return true;
    }
}
