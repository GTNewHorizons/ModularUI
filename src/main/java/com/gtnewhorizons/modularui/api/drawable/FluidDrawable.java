package com.gtnewhorizons.modularui.api.drawable;

import java.util.function.Supplier;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class FluidDrawable implements IDrawable {

    @NotNull
    private Supplier<FluidStack> fluid = () -> null;

    @Override
    public void applyThemeColor(int color) {}

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        GuiHelper.drawFluidTexture(fluid.get(), x, y, width, height, 0);
    }

    public @NotNull Supplier<FluidStack> getFluid() {
        return fluid;
    }

    public FluidDrawable setFluid(@NotNull Fluid fluid) {
        return setFluid(new FluidStack(fluid, 0));
    }

    public FluidDrawable setFluid(@Nullable FluidStack fluid) {
        return setFluid(() -> fluid);
    }

    public FluidDrawable setFluid(@NotNull Supplier<FluidStack> fluid) {
        this.fluid = fluid;
        return this;
    }
}
