package com.gtnewhorizons.modularui.api.fluids;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;

public class ListFluidHandler implements IFluidTanksHandler {

    protected final Iterable<? extends IFluidTanksHandler> fluidHandlers;

    public ListFluidHandler(Iterable<? extends IFluidTanksHandler> fluidHandlers) {
        this.fluidHandlers = fluidHandlers;
    }

    @Override
    public int getTanks() {
        int tanks = 0;

        for (IFluidTanksHandler fluidHandler : fluidHandlers) {
            tanks += fluidHandler.getTanks();
        }
        return tanks;
    }

    @Override
    public FluidStack getFluidStackInTank(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getFluidStackInTank(result.getRight());
    }

    @Override
    public FluidStack fill(int tank, Fluid fluid, long amount, boolean simulate) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().fill(result.getRight(), fluid, amount, simulate);
    }

    @Override
    public FluidStack drain(int tank, long amount, boolean simulate) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().drain(result.getRight(), amount, simulate);
    }

    @Override
    public long getTankCapacity(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getTankCapacity(result.getRight());
    }

    @Override
    public long getRealTankCapacity(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getRealTankCapacity(result.getRight());
    }

    @Override
    public long getTankStoredAmount(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getTankStoredAmount(result.getRight());
    }

    protected Pair<? extends IFluidTanksHandler, Integer> findFluidHandler(int tank) {
        int searching = 0;
        int amountOfTanks;

        for (IFluidTanksHandler fluidHandler : fluidHandlers) {
            amountOfTanks = fluidHandler.getTanks();
            if (tank >= searching && tank < searching + amountOfTanks) {
                return Pair.of(fluidHandler, tank - searching);
            }
        }

        throw new RuntimeException("Tank " + tank + " not in valid range - [0," + this.getTanks() + ")");
    }

    @Override
    public void setFluidInTank(int tank, Fluid fluid, long amount) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        result.getLeft().setFluidInTank(result.getRight(), fluid, amount);
    }

    @Override
    public IFluidTankLong getFluidTank(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getFluidTank(tank);
    }

    @Override
    public Fluid getFluidInTank(int tank) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tank);
        return result.getLeft().getFluidInTank(tank);
    }
}
