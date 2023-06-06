package com.gtnewhorizons.modularui.api.widget;

import static com.gtnewhorizons.modularui.ModularUI.isGT5ULoaded;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.common.fluid.IOverflowableTank;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.StackInfo;
import gregtech.api.util.GT_Utility;
import gregtech.common.items.GT_FluidDisplayItem;

/**
 * You can override these methods to have your own behavior for fluid manipulation with widgets.
 */
public interface FluidInteractionUtil {

    /**
     * Gets fluid actually stored in item. Used for transferring fluid.
     */
    default FluidStack getFluidForRealItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidForFilledItem(itemStack, true);
        } else {
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
            if (fluidStack == null && itemStack.getItem() instanceof IFluidContainerItem) {
                fluidStack = ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack);
            }
            return fluidStack;
        }
    }

    /**
     * Gets fluid for use in phantom slot.
     */
    default FluidStack getFluidForPhantomItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidFromContainerOrFluidDisplay(itemStack);
        } else {
            return StackInfo.getFluid(itemStack);
        }
    }

    default ItemStack fillFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        ItemStack filledContainer = fillFluidContainerWithoutIFluidContainerItem(fluidStack, itemStack);
        if (filledContainer == null) {
            filledContainer = fillFluidContainerWithIFluidContainerItem(fluidStack, itemStack);
        }
        return filledContainer;
    }

    default ItemStack fillFluidContainerWithoutIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.fillFluidContainer(fluidStack, itemStack, true, false);
        }
        return null;
    }

    default ItemStack fillFluidContainerWithIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (itemStack.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem tContainerItem = (IFluidContainerItem) itemStack.getItem();
            int tFilledAmount = tContainerItem.fill(itemStack, fluidStack, true);
            if (tFilledAmount > 0) {
                fluidStack.amount -= tFilledAmount;
                return itemStack;
            }
        }
        return null;
    }

    default ItemStack getContainerForFilledItemWithoutIFluidContainerItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getContainerForFilledItem(itemStack, false);
        }
        return null;
    }

    default void addFluidNameInfo(List<Text> tooltip, @NotNull FluidStack fluid) {
        tooltip.add(new Text(fluid.getLocalizedName()).format(EnumChatFormatting.WHITE));
        if (isGT5ULoaded) {
            String formula = GT_FluidDisplayItem.getChemicalFormula(fluid);
            if (!formula.isEmpty()) {
                tooltip.add(new Text(formula).format(EnumChatFormatting.YELLOW));
            }
        }
        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            tooltip.add(Text.localised("modularui.fluid.registry", fluid.getFluid().getName()));
            if (Interactable.hasShiftDown()) {
                tooltip.add(
                        Text.localised(
                                "modularui.fluid.unique_registry",
                                FluidRegistry.getDefaultFluidName(fluid.getFluid())));
            }
        }
    }

    /**
     * Mods can override this to add custom tooltips for the fluid
     *
     * @param tooltipContainer add lines here
     * @param fluid            the nonnull fluid
     */
    default void addAdditionalFluidInfo(List<Text> tooltipContainer, @NotNull FluidStack fluid) {
        if (Interactable.hasShiftDown()) {
            tooltipContainer.add(Text.localised("modularui.fluid.temperature", fluid.getFluid().getTemperature(fluid)));
            tooltipContainer.add(
                    Text.localised(
                            "modularui.fluid.state",
                            fluid.getFluid().isGaseous(fluid) ? StatCollector.translateToLocal("modularui.fluid.gas")
                                    : StatCollector.translateToLocal("modularui.fluid.liquid")));
            String amountDetail = GuiContainerManager.fluidAmountDetails(fluid);
            if (amountDetail != null) {
                tooltipContainer.add(new Text(amountDetail).format(EnumChatFormatting.GRAY));
            }
        }
    }

    default int getRealCapacity(IFluidTank fluidTank) {
        if (fluidTank instanceof IOverflowableTank) {
            return ((IOverflowableTank) fluidTank).getRealCapacity();
        }
        return fluidTank.getCapacity();
    }
}
