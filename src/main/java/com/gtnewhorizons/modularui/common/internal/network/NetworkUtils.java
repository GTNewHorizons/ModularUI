package com.gtnewhorizons.modularui.common.internal.network;

import com.gtnewhorizons.modularui.ModularUI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
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

    // modified version of PacketBuffer#writeNBTTagCompoundToBuffer
    public static void writeNBTBase(PacketBuffer buffer, NBTBase nbt) throws IOException {
        if (nbt == null) {
            buffer.writeShort(-1);
        } else {
            byte[] abyte = compressNBTBase(nbt);
            buffer.writeShort((short) abyte.length);
            buffer.writeBytes(abyte);
        }
    }

    // modified version of PacketBuffer#readNBTTagCompoundFromBuffer
    public static NBTBase readNBTBase(PacketBuffer buffer) throws IOException {
        short short1 = buffer.readShort();
        if (short1 < 0) {
            return null;
        } else {
            byte[] abyte = new byte[short1];
            buffer.readBytes(abyte);
            return read(abyte, new NBTSizeTracker(2097152L));
        }
    }

    // modified version of CompressedStreamTools#compress
    private static byte[] compressNBTBase(NBTBase nbt) throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        try (DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream))) {
            // writeTag
            CompressedStreamTools$func_150663_a.invoke(null, nbt, dataoutputstream);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return bytearrayoutputstream.toByteArray();
    }

    // modified version of CompressedStreamTools#func_152457_a
    private static NBTBase read(byte[] abyte, NBTSizeTracker accounter) throws IOException {
        DataInputStream datainputstream =
                new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));
        NBTBase nbt;
        try {
            nbt = read(datainputstream, accounter);
        } finally {
            datainputstream.close();
        }
        return nbt;
    }

    // modified version of CompressedStreamTools#func_152456_a
    private static NBTBase read(DataInput input, NBTSizeTracker accounter) {
        try {
            // read
            return (NBTBase) CompressedStreamTools$func_152455_a.invoke(null, input, 0, accounter);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Method CompressedStreamTools$func_150663_a;
    private static final Method CompressedStreamTools$func_152455_a;

    static {
        try {
            CompressedStreamTools$func_150663_a =
                    CompressedStreamTools.class.getDeclaredMethod("func_150663_a", NBTBase.class, DataOutput.class);
            CompressedStreamTools$func_150663_a.setAccessible(true);
            CompressedStreamTools$func_152455_a = CompressedStreamTools.class.getDeclaredMethod(
                    "func_152455_a", DataInput.class, int.class, NBTSizeTracker.class);
            CompressedStreamTools$func_152455_a.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
