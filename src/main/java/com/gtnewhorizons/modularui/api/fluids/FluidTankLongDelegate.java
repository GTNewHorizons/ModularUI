package com.gtnewhorizons.modularui.api.fluids;

import static com.google.common.primitives.Ints.saturatedCast;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

public class FluidTankLongDelegate implements IFluidTankLong {

    private final IFluidTank tank;

    public FluidTankLongDelegate(IFluidTank tank) {
        this.tank = tank;
    }

    @Override
    public long fill(Fluid fluid, long amount, boolean doFill) {
        return tank.fill(new FluidStack(fluid, saturatedCast(amount)), doFill);
    }

    @Override
    public FluidStack drain(long amount, boolean doDrain) {
        return tank.drain(saturatedCast(amount), doDrain);
    }

    @Override
    public long getCapacityLong() {
        return tank.getCapacity();
    }

    @Override
    public long getFluidAmountLong() {
        return tank.getFluidAmount();
    }

    @Override
    public FluidStack getFluidStack() {
        return tank.getFluid();
    }

    @Override
    public Fluid getStoredFluid() {
        return tank.getFluid().getFluid();
    }

    @Override
    public void setFluid(Fluid fluid, long amount) {
        tank.drain(Integer.MAX_VALUE, true);
        tank.fill(new FluidStack(fluid, saturatedCast(amount)), true);
    }

    @Override
    public @Nullable IFluidTankLong copy() {
        return new FluidTankLongDelegate(new FluidTank(getFluidStack(), getCapacity()));
    }

    @Override
    public boolean isFluidEqual(@Nullable IFluidTankLong cached) {
        return tank.getFluid().isFluidEqual(cached.getFluidStack());
    }

    @Override
    public void saveToNBT(NBTTagCompound fluidTag) {
        if (tank.getFluid() == null) return;
        tank.getFluid().writeToNBT(fluidTag);
    }

    @Override
    public void loadFromNBT(NBTTagCompound fluidTag) {
        tank.drain(Integer.MAX_VALUE, true);
        tank.fill(FluidStack.loadFluidStackFromNBT(fluidTag), true);
    }
    
}
