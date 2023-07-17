package com.gtnewhorizons.modularui.api.fluids;

import static com.google.common.primitives.Ints.saturatedCast;

import net.minecraft.nbt.NBTTagCompound;
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
        return drain(maxDrain, doDrain);
    }

    void setFluid(Fluid fluid, long amount);

    @Nullable
    IFluidTankLong copy();

    boolean isFluidEqual(@Nullable IFluidTankLong cached);

    void saveToNBT(NBTTagCompound fluidTag);

    void loadFromNBT(NBTTagCompound fluidTag);
}
