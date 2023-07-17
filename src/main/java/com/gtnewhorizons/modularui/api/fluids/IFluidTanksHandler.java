package com.gtnewhorizons.modularui.api.fluids;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTanksHandler {

    int getTanks();

    @Nullable
    FluidStack getFluidStackInTank(int tank);

    Fluid getFluidInTank(int tank);

    FluidTankLong getFluidTank(int tank);

    @Nullable
    FluidStack fill(int tank, @Nullable Fluid fluid, long amount, boolean simulate);

    @Nullable
    FluidStack drain(int tank, long amount, boolean simulate);

    long getTankCapacity(int tank);

    long getTankStoredAmount(int tank);

    default boolean isFluidValid(int slot, FluidStack stack) {
        return true;
    }

    default List<FluidStack> getFluids() {
        List<FluidStack> ret = new ArrayList<>();

        for (int i = 0; i < this.getTanks(); ++i) {
            ret.add(this.getFluidStackInTank(i));
        }

        return ret;
    }

    void setFluidInTank(int tank, Fluid fluid, long amount);

    default void setFluidInTank(int tank, Fluid fluid) {
        setFluidInTank(tank, fluid, 0);
    }

    default void setFluidInTank(int tank, IFluidTankLong fluid) {
        setFluidInTank(tank, fluid != null ? fluid.getStoredFluid() : null, fluid != null ? fluid.getFluidAmount() : 0);
    }
}
