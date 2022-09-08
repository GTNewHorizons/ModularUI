package com.gtnewhorizons.modularui.common.internal.wrapper;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public class FluidTankHandler implements IFluidHandler {

    public static IFluidHandler getTankFluidHandler(IFluidTank tank) {
        if (tank instanceof IFluidHandler) {
            return (IFluidHandler) tank;
        }
        return new FluidTankHandler(tank);
    }

    private final IFluidTank fluidTank;

    public FluidTankHandler(IFluidTank tank) {
        this.fluidTank = tank;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{
                fluidTank.getInfo()
        };
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null || currentFluid.amount <= 0 || !currentFluid.isFluidEqual(resource)) {
            return null;
        }
        return fluidTank.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }
}
