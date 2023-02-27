package com.gtnewhorizons.modularui.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S2FPacketSetSlot.class)
public class S2FPacketSetSlotMixin {

    @Shadow
    private ItemStack field_149178_c;

    /**
     * @reason Encoder {@link PacketBuffer#writeItemStackToBuffer} cannot handle stack size larger than 127
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void modularui$afterReadPacketData(PacketBuffer buf, CallbackInfo ci) {
        if (field_149178_c != null) {
            this.field_149178_c.stackSize = buf.readVarIntFromBuffer();
        }
    }

    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void modularui$afterWritePacketData(PacketBuffer buf, CallbackInfo ci) {
        if (field_149178_c != null) {
            buf.writeVarIntToBuffer(field_149178_c.stackSize);
        }
    }
}
