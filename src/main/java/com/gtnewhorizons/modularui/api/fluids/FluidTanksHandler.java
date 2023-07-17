package com.gtnewhorizons.modularui.api.fluids;

import static com.google.common.primitives.Ints.saturatedCast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidTanksHandler implements IFluidTanksHandler {

    protected final List<IFluidTankLong> fluids;

    public FluidTanksHandler(FluidTankLong tank) {
        this.fluids = Collections.singletonList(tank);
    }

    public FluidTanksHandler(int tankAmount, long capacity) {
        FluidTankLong[] fluids = new FluidTankLong[tankAmount];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = new FluidTankLong(capacity);
        }
        this.fluids = Arrays.asList(fluids);
    }

    @Override
    public int getTanks() {
        return fluids.size();
    }

    @Override
    public FluidStack getFluidStackInTank(int tank) {
        return fluids.get(tank).getFluidStack();
    }

    @Override
    public FluidStack fill(int tank, Fluid fluid, long amount, boolean simulate) {
        return fluid == null ? null
                : new FluidStack(fluid, saturatedCast(fluids.get(tank).fill(fluid, amount, !simulate)));
    }

    @Override
    public FluidStack drain(int tank, long amount, boolean simulate) {
        return fluids.get(tank).drain(amount, !simulate);
    }

    @Override
    public long getTankCapacity(int tank) {
        return fluids.get(tank).getCapacity();
    }

    @Override
    public long getTankStoredAmount(int tank) {
        return fluids.get(tank).getFluidAmountLong();
    }

    @Override
    public void setFluidInTank(int tank, Fluid fluid, long amount) {
        fluids.get(tank).setFluid(fluid, amount);
    }

    @Override
    public IFluidTankLong getFluidTank(int tank) {
        return fluids.get(tank);
    }

    @Override
    public Fluid getFluidInTank(int tank) {
        return fluids.get(tank).getStoredFluid();
    }

}
