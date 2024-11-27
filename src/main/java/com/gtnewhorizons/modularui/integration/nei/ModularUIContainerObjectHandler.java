package com.gtnewhorizons.modularui.integration.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import codechicken.nei.guihook.IContainerObjectHandler;

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
        if (gui instanceof ModularGui) {
            ModularGui modularGui = (ModularGui) gui;
            ModularWindow hoveredWindow = modularGui.getCursor().findHoveredWindow();

            boolean foundLowerSlot = false;
            for (Object hovered : modularGui.getCursor().getAllHovered()) {
                if (hovered instanceof SlotWidget) {
                    SlotWidget slot = (SlotWidget) hovered;
                    if (slot.getWindow() == hoveredWindow) {
                        return false;
                    } else {
                        foundLowerSlot = true;
                    }
                }
            }
            return foundLowerSlot;
        }
        return false;
    }

    @Override
    public boolean shouldShowTooltip(GuiContainer gui) {
        return true;
    }
}
