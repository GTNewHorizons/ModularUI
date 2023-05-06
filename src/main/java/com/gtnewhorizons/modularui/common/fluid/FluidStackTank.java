package com.gtnewhorizons.modularui.common.fluid;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

@SuppressWarnings("unused")
public class FluidStackTank implements IFluidTank, IOverflowableTank {

    private final Supplier<FluidStack> getter;
    private final Consumer<FluidStack> setter;
    private final IntSupplier capacityGetter;
    private boolean allowOverflow;
    private boolean preventDraining;

    public FluidStackTank(Supplier<FluidStack> getter, Consumer<FluidStack> setter, int capacity) {
        this(getter, setter, () -> capacity);
    }

    public FluidStackTank(Supplier<FluidStack> getter, Consumer<FluidStack> setter, IntSupplier capacityGetter) {
        this.getter = getter;
        this.setter = setter;
        this.capacityGetter = capacityGetter;
    }

    public void setAllowOverflow(boolean allowOverflow) {
        this.allowOverflow = allowOverflow;
    }

    public void setPreventDraining(boolean preventDraining) {
        this.preventDraining = preventDraining;
    }

    @Override
    public FluidStack getFluid() {
        return getter.get();
    }

    @Override
    public int getFluidAmount() {
        FluidStack fluidStack = getter.get();
        return fluidStack != null ? fluidStack.amount : 0;
    }

    @Override
    public int getCapacity() {
        return allowOverflow ? Integer.MAX_VALUE : getRealCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }
        FluidStack fluid = getter.get();

        if (!doFill) {
            if (fluid == null) {
                return Math.min(getCapacity(), resource.amount);
            }

            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }

            return Math.min(getCanFillAmount(), resource.amount);
        }

        if (fluid == null) {
            fluid = new FluidStack(resource, Math.min(getCapacity(), resource.amount));
            setter.accept(fluid);
            int retAmount = fluid.amount;
            validateFluid();
            return retAmount;
        }

        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }

        int canFillAmount = getCanFillAmount();

        if (resource.amount < canFillAmount) {
            fluid.amount += resource.amount;
            canFillAmount = resource.amount;
        } else {
            fluid.amount = getRealCapacity();
        }
        validateFluid();

        return canFillAmount;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack fluid = getter.get();
        if (fluid == null) {
            return null;
        }

        int drained = maxDrain;
        if (fluid.amount < drained) {
            drained = fluid.amount;
        }

        FluidStack stack = new FluidStack(fluid, drained);
        if (doDrain) {
            fluid.amount -= drained;
            if (fluid.amount <= 0 && !preventDraining) {
                setter.accept(null);
            }

        }
        return stack;
    }

    @Override
    public int getRealCapacity() {
        return capacityGetter.getAsInt();
    }

    public int getCanFillAmount() {
        FluidStack fluid = getter.get();
        if (fluid == null) return 0;
        return allowOverflow ? Integer.MAX_VALUE : (getRealCapacity() - fluid.amount);
    }

    /**
     * Shrinks fluid amount if it exceeds real capacity
     */
    public void validateFluid() {
        FluidStack fluid = getter.get();
        if (fluid == null) return;
        fluid.amount = Math.min(fluid.amount, getRealCapacity());
    }
}
