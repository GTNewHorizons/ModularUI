package com.gtnewhorizons.modularui.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Shadow
    private Minecraft gameController;

    /**
     * @reason Prevent client-only GUI from accepting slot updates from server, e.g. armor taking damage
     */
    @Inject(
            method = "handleSetSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Container;putStackInSlot(ILnet/minecraft/item/ItemStack;)V",
                    ordinal = 1),
            cancellable = true)
    public void modularui$beforeHandleSetSlot(S2FPacketSetSlot p_147266_1_, CallbackInfo ci) {
        Container container = gameController.thePlayer.openContainer;
        if (container instanceof ModularUIContainer) {
            if (((ModularUIContainer) container).getContext().isClientOnly()) {
                ci.cancel();
            }
        }
    }
}
