package com.gtnewhorizons.modularui.api.widget;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;

import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.JsonHelper;
import com.gtnewhorizons.modularui.common.internal.JsonLoader;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

@SuppressWarnings("unchecked")
public interface IWidgetBuilder<T extends IWidgetBuilder<T>> {

    @ApiStatus.Internal
    void addWidgetInternal(Widget widget);

    /**
     * Add Widget
     */
    default T widget(Widget widget) {
        if (widget != null) {
            addWidgetInternal(widget);
        }
        return (T) this;
    }

    /**
     * Add Widgets
     */
    default T widgets(Widget... widgets) {
        for (Widget widget : widgets) {
            if (widget != null) {
                addWidgetInternal(widget);
            }
        }
        return (T) this;
    }

    /**
     * Add Widgets
     */
    default T widgets(Collection<Widget> widgets) {
        return widgets(widgets.toArray(new Widget[0]));
    }

    /**
     * Add Widget only when {@code doAdd} is true
     */
    default T widgetWhen(boolean doAdd, Widget widget) {
        if (doAdd) widget(widget);
        return (T) this;
    }

    default T drawable(IDrawable drawable) {
        return widget(drawable.asWidget());
    }

    default T slot(BaseSlot slot) {
        return widget(new SlotWidget(slot));
    }

    default T bindPlayerInventory(EntityPlayer player, Pos2d pos, IDrawable background) {
        return widget(SlotGroup.playerInventoryGroup(player, background).setPos(pos));
    }

    default T bindPlayerInventory(EntityPlayer player, int x, int y) {
        return widget(SlotGroup.playerInventoryGroup(player).setPos(new Pos2d(x, y)));
    }

    default T addFromJson(String mod, String location, UIBuildContext buildContext) {
        return addFromJson(new ResourceLocation(mod, location), buildContext);
    }

    default T addFromJson(String location, UIBuildContext buildContext) {
        return addFromJson(new ResourceLocation(location), buildContext);
    }

    default T addFromJson(ResourceLocation location, UIBuildContext buildContext) {
        JsonObject json = JsonLoader.GUIS.get(location);
        if (json == null) {
            ModularUI.logger.error("Couldn't not find json file {}", location);
            return (T) this;
        }
        JsonHelper.parseJson(this, json, buildContext);
        return (T) this;
    }
}
