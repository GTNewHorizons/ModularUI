package com.gtnewhorizons.modularui.api.fluids;

import static com.google.common.primitives.Ints.saturatedCast;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidTankLong extends IFluidTank {

    long fill(Fluid fluid, long amount, boolean simulate);

    FluidStack drain(long amount, boolean simulate);

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
        return saturatedCast(fill(resource.getFluid(), resource.amount, !doFill));
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
        return drain(maxDrain, !doDrain);
    }
}
