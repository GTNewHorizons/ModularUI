package com.gtnewhorizons.modularui.api.widget;

import com.gtnewhorizons.modularui.api.nei.IGhostIngredientHandler;
import com.gtnewhorizons.modularui.common.internal.wrapper.GhostIngredientWrapper;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for {@link Widget} classes.
 * Implement this, to be able to drag items from NEI onto this widget
 */
public interface IGhostIngredientTarget {

    /**
     * Called when the users tries to drag an ItemStack from NEI.
     *
     * @param ingredient object the cursor is holding. Should be validated for Type
     * @return the NEI target. Usually an instance of {@link GhostIngredientWrapper}
     */
    @Nullable
    IGhostIngredientHandler.Target getTarget(@NotNull ItemStack ingredient);

    /**
     * Called when this widget is clicked with a object
     *
     * @param ingredient object the cursor is holding
     */
    void accept(@NotNull ItemStack ingredient);
}
