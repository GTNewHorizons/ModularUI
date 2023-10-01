package com.gtnewhorizons.modularui.mixins;

import java.awt.Point;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import codechicken.nei.NEIController;

@Mixin(NEIController.class)
public class NEIControllerMixin {

    /**
     * @reason Prevent client-only window from sending slot click packet via NEI fast transfer
     */
    @Inject(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lcodechicken/nei/NEIClientConfig;shouldInvertMouseScrollTransfer()Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            remap = false)
    public void modularui$beforeMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled,
            CallbackInfoReturnable<Boolean> ci, Point mousePos, Slot mouseover) {
        if (gui instanceof ModularGui) {
            Widget hovered = ((ModularGui) gui).getCursor().getHovered();
            if (!(hovered instanceof SlotWidget) || mouseover != ((SlotWidget) hovered).getMcSlot()) {
                ci.setReturnValue(false);
                return;
            }
            if (((BaseSlot) mouseover).getParentWidget().getWindow().isClientOnly()) {
                ci.setReturnValue(false);
            }
        }
    }
}
