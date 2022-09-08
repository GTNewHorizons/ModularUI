package com.gtnewhorizons.modularui.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class SyncedTileEntityBase extends TileEntity {

    public abstract void writeInitialSyncData(PacketBuffer buf);

    public abstract void receiveInitialSyncData(PacketBuffer buf);

    public abstract void receiveCustomData(int discriminator, PacketBuffer buf);

    private static class UpdateEntry {
        private final int discriminator;
        private final byte[] updateData;

        public UpdateEntry(int discriminator, byte[] updateData) {
            this.discriminator = discriminator;
            this.updateData = updateData;
        }
    }

    protected final List<UpdateEntry> updateEntries = new ArrayList<>();

    public void writeCustomData(int discriminator, Consumer<PacketBuffer> dataWriter) {
        ByteBuf backedBuffer = Unpooled.buffer();
        dataWriter.accept(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateEntries.add(new UpdateEntry(discriminator, updateData));
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata(), 0);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound updateTag = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        for (UpdateEntry updateEntry : updateEntries) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setInteger("i", updateEntry.discriminator);
            entryTag.setByteArray("d", updateEntry.updateData);
            tagList.appendTag(entryTag);
        }
        this.updateEntries.clear();
        updateTag.setTag("d", tagList);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, updateTag);
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, S35PacketUpdateTileEntity pkt) {
        // #getNbtCompound
        NBTTagCompound updateTag = pkt.func_148857_g();
        NBTTagList tagList = updateTag.getTagList("d", NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound entryTag = tagList.getCompoundTagAt(i);
            int discriminator = entryTag.getInteger("i");
            byte[] updateData = entryTag.getByteArray("d");
            ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
            receiveCustomData(discriminator, new PacketBuffer(backedBuffer));
        }
    }

    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = new NBTTagCompound();
        this.writeToNBT(updateTag);
        ByteBuf backedBuffer = Unpooled.buffer();
        writeInitialSyncData(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateTag.setByteArray("d", updateData);
        return updateTag;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag); // deserializes Forge data and capabilities
        byte[] updateData = tag.getByteArray("d");
        ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
        receiveInitialSyncData(new PacketBuffer(backedBuffer));
    }

}
