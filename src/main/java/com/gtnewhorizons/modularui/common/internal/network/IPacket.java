package com.gtnewhorizons.modularui.common.internal.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/**
 * Joinked from Multiblocked
 */
public interface IPacket extends IMessage {

    void encode(PacketBuffer buf);

    void decode(PacketBuffer buf);

    @Override
    default void fromBytes(ByteBuf buf) {
        decode(new PacketBuffer(buf));
    }

    @Override
    default void toBytes(ByteBuf buf) {
        encode(new PacketBuffer(buf));
    }

    @SideOnly(Side.CLIENT)
    default IPacket executeClient(NetHandlerPlayClient handler) {
        return null;
    }

    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }
}
