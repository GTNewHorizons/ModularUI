package com.gtnewhorizons.modularui.api.drawable;

import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Draws item. Can also be used for {@link com.gtnewhorizons.modularui.api.widget.Widget}
 */
public class ItemDrawable implements IDrawable {

    @NotNull
    private Supplier<ItemStack> item;

    public ItemDrawable() {
        this((ItemStack) null);
    }

    public ItemDrawable(ItemStack item) {
        this.item = () -> item;
    }

    public ItemDrawable(@NotNull Supplier<ItemStack> itemGetter) {
        this.item = itemGetter;
    }

    @Override
    public void applyThemeColor(int color) {}

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        final ItemStack item = this.item.get();
        if (item == null || item.getItem() == null) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(width / 16, height / 16, 1);
        ModularGui.getItemRenderer()
                .renderItemAndEffectIntoGUI(
                        item.getItem().getFontRenderer(item),
                        Minecraft.getMinecraft().getTextureManager(),
                        item,
                        (int) x,
                        (int) y);
        GuiHelper.afterRenderItemAndEffectIntoGUI(item);

        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public DrawableWidget asWidget() {
        return (DrawableWidget) IDrawable.super.asWidget().setSize(16, 16);
    }

    public ItemStack getItem() {
        return item.get();
    }

    public ItemDrawable setItem(ItemStack item) {
        this.item = () -> item;
        return this;
    }

    public ItemDrawable setItem(@NotNull Supplier<ItemStack> itemGetter) {
        this.item = itemGetter;
        return this;
    }
}
