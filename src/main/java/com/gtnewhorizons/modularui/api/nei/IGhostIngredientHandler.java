package com.gtnewhorizons.modularui.api.nei;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

/**
 * Lets mods accept ghost ingredients from NEI.
 * These ingredients are dragged from the ingredient list on to your gui, and are useful
 * for setting recipes or anything else that does not need the real ingredient to exist.
 */
public interface IGhostIngredientHandler<T extends GuiScreen> {
    /**
     * Called when a player wants to drag an ingredient on to your gui.
     * Return the targets that can accept the ingredient.
     *
     * This is called when a player hovers over an ingredient with doStart=false,
     * and called again when they pick up the ingredient with doStart=true.
     */
    List<Target> getTargets(T gui, ItemStack ingredient, boolean doStart);

    /**
     * Called when the player is done dragging an ingredient.
     * If the drag succeeded, {@link Target#accept(ItemStack)} was called before this.
     * Otherwise, the player failed to drag an ingredient to a {@link Target}.
     */
    void onComplete();

    /**
     * @return true if NEI should highlight the targets for the player.
     * false to handle highlighting yourself.
     */
    default boolean shouldHighlightTargets() {
        return true;
    }

    interface Target extends Consumer<ItemStack> {
        /**
         * @return the area (in screen coordinates) where the ingredient can be dropped.
         */
        Rectangle getArea();

        /**
         * Called with the ingredient when it is dropped on the target.
         */
        @Override
        void accept(ItemStack ingredient);
    }
}
