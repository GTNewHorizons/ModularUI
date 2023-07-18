package com.gtnewhorizons.modularui.api.fluids;

import static com.google.common.primitives.Ints.saturatedCast;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

public interface IFluidTankLong extends IFluidTank {

    long fill(Fluid fluid, long amount, boolean doFill);

    FluidStack drain(long amount, boolean doFill);

    long getCapacityLong();

    long getFluidAmountLong();

    FluidStack getFluidStack();

    Fluid getStoredFluid();

    @Override
    default int getFluidAmount() {
        return saturatedCast(getFluidAmountLong());
    }

    @Override
    default FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    default int fill(FluidStack resource, boolean doFill) {
        return saturatedCast(fill(resource.getFluid(), resource.amount, doFill));
    }

    @Override
    default FluidStack getFluid() {
        return getFluidStack();
    }

    @Override
    default int getCapacity() {
        return saturatedCast(getCapacityLong());
    }

    @Override
    default FluidStack drain(int maxDrain, boolean doDrain) {
        return drain((long) maxDrain, doDrain);
    }

    void setFluid(Fluid fluid, long amount);

    @Nullable
    IFluidTankLong copy();

    boolean isFluidEqual(@Nullable IFluidTankLong cached);

    void saveToNBT(NBTTagCompound fluidTag);

    void loadFromNBT(NBTTagCompound fluidTag);

    public static void writeToBuffer(PacketBuffer buffer, IFluidTankLong currentFluid) {
        if (currentFluid == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidTag = new NBTTagCompound();
            currentFluid.saveToNBT(fluidTag);

            try {
                buffer.writeNBTTagCompoundToBuffer(fluidTag);
            } catch (IOException ignored) {}
        }
    }

    public static void readFromBuffer(PacketBuffer buffer, IFluidTankLong currentTank) throws IOException {
        currentTank.loadFromNBT(buffer.readBoolean() ? null : buffer.readNBTTagCompoundFromBuffer());
    }
}
