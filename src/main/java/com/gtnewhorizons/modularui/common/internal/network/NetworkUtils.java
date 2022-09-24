package com.gtnewhorizons.modularui.common.internal.network;

import com.gtnewhorizons.modularui.ModularUI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class NetworkUtils {

    public static final Consumer<PacketBuffer> EMPTY_PACKET = buffer -> {};

    /**
     * @return If this is physical client. Does not return false in SP on logical server.
     */
    public static boolean isDedicatedClient() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    /**
     * @return If this is logical client. Returns false in SP on logical server.
     */
    public static boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    public static void writePacketBuffer(PacketBuffer writeTo, PacketBuffer writeFrom) {
        writeTo.writeVarIntToBuffer(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarIntFromBuffer());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
    }

    public static void writeFluidStack(PacketBuffer buffer, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
            try {
                buffer.writeNBTTagCompoundToBuffer(fluidStackTag);
            } catch (IOException e) {

            }
        }
    }

    @Nullable
    public static FluidStack readFluidStack(PacketBuffer buffer) throws IOException {
        if (buffer.readBoolean()) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(buffer.readNBTTagCompoundFromBuffer());
    }

    public static void writeStringSafe(PacketBuffer buffer, String string) {
        byte[] bytesTest = string.getBytes(StandardCharsets.UTF_8);
        byte[] bytes;

        if (bytesTest.length > 32767) {
            bytes = new byte[32767];
            System.arraycopy(bytesTest, 0, bytes, 0, 32767);
            ModularUI.logger.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        buffer.writeVarIntToBuffer(bytes.length);
        buffer.writeBytes(bytes);
    }
}
