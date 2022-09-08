package com.gtnewhorizons.modularui.common.internal.wrapper;

import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.nei.IGhostIngredientHandler;
import com.gtnewhorizons.modularui.api.widget.IGhostIngredientTarget;
import com.gtnewhorizons.modularui.api.widget.Widget;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GhostIngredientWrapper<W extends Widget & IGhostIngredientTarget, I> implements IGhostIngredientHandler.Target {

    private final W widget;

    public GhostIngredientWrapper(W widget) {
        this.widget = widget;
    }

    @Override
    public @NotNull Rectangle getArea() {
        Pos2d pos = widget.getAbsolutePos();
        return new Rectangle(pos.x, pos.y, widget.getSize().width, widget.getSize().height);
    }

    @Override
    public void accept(@NotNull ItemStack ingredient) {
        widget.accept(ingredient);
    }
}
