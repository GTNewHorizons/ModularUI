package com.gtnewhorizons.modularui.api.forge;

import static com.google.common.primitives.Ints.saturatedCast;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

/**
 * A fluid tank with long capacity and stored amount
 * 
 * @author BlueWeabo
 */
public class FluidTankLong implements IFluidTank {

    private Fluid fluid;
    private long storedAmount;
    private long capacity;
    private FluidStack internal;
    private boolean locked;
    private int lastFluidAmountInStack;
    private NBTTagCompound tag;

    public FluidTankLong(Fluid fluid, long capacity, long amount) {
        this.fluid = fluid;
        this.storedAmount = amount;
        this.capacity = capacity;
        if (fluid == null) return;
        internal = new FluidStack(fluid, saturatedCast(amount));
    }

    public FluidTankLong(Fluid fluid, long capacity) {
        this(fluid, capacity, 0);
    }

    public FluidTankLong(long capacity) {
        this((Fluid) null, capacity);
    }

    public FluidTankLong(IFluidTank tank) {
        this(tank.getFluid() != null ? tank.getFluid().getFluid() : null, tank.getCapacity(), tank.getFluidAmount());
    }

    public FluidTankLong(FluidStack fluid, long capacity, long amount) {
        this(fluid != null ? fluid.getFluid() : null, capacity, amount);
    }

    /**
     * Recommended to use {@link FluidTankLong#drain(int, boolean)} and {@link FluidTankLong#fill(Fluid, long, boolean)}
     * for handling the fluids if not for recipes or GUI
     */
    public FluidStack getFluidStack() {
        if (fluid == null) {
            return null;
        }

        if (internal == null) {
            internal = new FluidStack(fluid, 0);
        }

        if (internal.amount != lastFluidAmountInStack) {
            storedAmount -= lastFluidAmountInStack - internal.amount;
        }

        lastFluidAmountInStack = internal.amount;
        internal.amount = saturatedCast(storedAmount);
        return internal;
    }

    public Fluid getFluidStored() {
        return fluid;
    }

    public long getCapacityLong() {
        return capacity;
    }

    public long getFluidAmountLong() {
        return storedAmount;
    }

    /**
     * @param fluid    The fluid we are trying to fill
     * @param amount   Amount of fluid trying to be filled in
     * @param simulate Should it update the stack internally
     * @return Amount of fluid filled into the stack
     */
    public long fill(Fluid fluid, long amount, boolean simulate) {
        if (this.fluid != null && this.fluid != fluid || fluid == null) return 0;

        if (simulate) {
            return Math.min(capacity - storedAmount, amount);
        }

        if (this.fluid == null) {
            this.fluid = fluid;
        }

        long amountFilled = Math.min(capacity - storedAmount, amount);
        this.storedAmount += amountFilled;

        return amountFilled;
    }

    /**
     * 
     * @param amount   Amount of fluid to try and drain
     * @param simulate Should it update the stack internally
     * @return a Fluid stack with the amount drained
     */
    public FluidStack drain(long amount, boolean simulate) {
        if (fluid == null) {
            return null;
        }
        if (simulate) {
            return new FluidStack(fluid, saturatedCast(Math.min(storedAmount, amount)));
        }

        long amountDrained = Math.min(storedAmount, amount);
        storedAmount -= amountDrained;
        FluidStack fluidDrained = new FluidStack(fluid, saturatedCast(amountDrained));
        if (storedAmount <= 0 && !locked) {
            fluid = null;
            internal = null;
        }
        return fluidDrained;
    }

    public void setFluid(Fluid fluid, long amount) {
        this.fluid = fluid;
        storedAmount = amount;
        if (fluid == null) {
            internal = null;
            lastFluidAmountInStack = 0;
            return;
        }
        internal = new FluidStack(this.fluid, saturatedCast(storedAmount));
        lastFluidAmountInStack = saturatedCast(storedAmount);
    }

    public void setFluid(Fluid fluid) {
        setFluid(fluid, 0);
    }

    public static FluidTankLong loadFromNBT(NBTTagCompound nbt) {
        return new FluidTankLong(
                nbt.hasKey("FluidName") ? FluidRegistry.getFluid(nbt.getString("FluidName")) : null,
                nbt.getLong("Capacity"),
                nbt.getLong("StoredAmount"));
    }

    public void saveToNBT(NBTTagCompound nbt) {
        if (fluid != null) nbt.setString("FluidName", FluidRegistry.getFluidName(getFluid()));
        nbt.setLong("StoredAmount", storedAmount);
        nbt.setLong("Capacity", capacity);

        if (tag != null) {
            nbt.setTag("Tag", tag);
        }
    }

    public static void writeToBuffer(PacketBuffer buffer, FluidTankLong fluid) {
        if (fluid == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluid.saveToNBT(fluidTag);

            try {
                buffer.writeNBTTagCompoundToBuffer(fluidTag);
            } catch (IOException ignored) {}
        }
    }

    public static FluidTankLong readFromBuffer(PacketBuffer buffer) throws IOException {
        return buffer.readBoolean() ? null : loadFromNBT(buffer.readNBTTagCompoundFromBuffer());
    }

    public boolean isFluidEqual(FluidTankLong cached) {
        return getFluid() == cached.getFluid();
    }

    public FluidTankLong copy() {
        return new FluidTankLong(fluid, capacity, storedAmount);
    }

    @Override
    public int getFluidAmount() {
        return saturatedCast(getFluidAmountLong());
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return saturatedCast(fill(resource.getFluid(), resource.amount, !doFill));
    }

    @Override
    public FluidStack getFluid() {
        return getFluidStack();
    }

    @Override
    public int getCapacity() {
        return saturatedCast(getCapacityLong());
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain(maxDrain, !doDrain);
    }
}