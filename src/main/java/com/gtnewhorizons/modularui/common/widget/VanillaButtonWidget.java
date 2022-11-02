package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;
import com.gtnewhorizons.modularui.api.math.Alignment;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * {@link net.minecraft.client.gui.GuiButton}-alike button widget.
 */
public class VanillaButtonWidget extends ButtonWidget {

    private String displayString;
    private Supplier<Boolean> clickableGetter;
    private final TextRenderer textRenderer = new TextRenderer();

    @Override
    public @Nullable IDrawable[] getBackground() {
        if (!isClickable()) return new IDrawable[] {ModularUITextures.VANILLA_BUTTON_DISABLED};
        if (isHovering()) return new IDrawable[] {ModularUITextures.VANILLA_BUTTON_HOVERED};
        return new IDrawable[] {ModularUITextures.VANILLA_BUTTON_NORMAL};
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (!isClickable()) return ClickResult.REJECT;
        return super.onClick(buttonId, doubleClick);
    }

    @Override
    public void draw(float partialTicks) {
        textRenderer.setPos(0, 0);
        textRenderer.setShadow(true);
        textRenderer.setAlignment(Alignment.Center, size.width, size.height);
        textRenderer.setColor(!isClickable() ? 0xa0a0a0 : isHovering() ? 0xffffa0 : 0xe0e0e0);
        textRenderer.draw(displayString);
    }

    public boolean isClickable() {
        return clickableGetter != null ? clickableGetter.get() : true;
    }

    public VanillaButtonWidget setDisplayString(String displayString) {
        this.displayString = displayString;
        return this;
    }

    public VanillaButtonWidget setClickableGetter(Supplier<Boolean> clickableGetter) {
        this.clickableGetter = clickableGetter;
        return this;
    }
}
