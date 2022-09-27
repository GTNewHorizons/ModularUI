package com.gtnewhorizons.modularui.api.widget;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus;

public interface ITransferRectHandler extends Interactable {

    @Nullable
    String getNEITransferRectID();

    String getNEITransferRectTooltip();

    Object[] getNEITransferRectArgs();

    @Override
    default ClickResult onClick(int buttonId, boolean doubleClick) {
        if (getNEITransferRectID() != null) {
            if (buttonId == 0) {
                if (handleTransferRectMouseClick(false)) {
                    return ClickResult.SUCCESS;
                } else {
                    return ClickResult.ACCEPT;
                }
            } else if (buttonId == 1) {
                if (handleTransferRectMouseClick(true)) {
                    return ClickResult.SUCCESS;
                } else {
                    return ClickResult.ACCEPT;
                }
            }
        }
        return ClickResult.IGNORE;
    }

    @ApiStatus.Internal
    default boolean handleTransferRectMouseClick(boolean usage) {
        String id = getNEITransferRectID();
        Object[] args = getNEITransferRectArgs();
        Interactable.playButtonClickSound();
        return usage ? GuiUsageRecipe.openRecipeGui(id) : GuiCraftingRecipe.openRecipeGui(id, args);
    }
}
