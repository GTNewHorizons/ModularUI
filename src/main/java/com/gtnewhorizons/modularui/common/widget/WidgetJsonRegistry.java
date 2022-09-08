package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.widget.Widget;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;

public class WidgetJsonRegistry {

    public static void init() {
        registerWidget("text", TextWidget::new);
        registerWidget("image", DrawableWidget::new);
        registerWidget("cycle_button", CycleButtonWidget::new);
        registerWidget("button", ButtonWidget::new);
        registerWidgetSpecial("player_inventory", SlotGroup::playerInventoryGroup);

        IDrawable.JSON_DRAWABLE_MAP.put("text", Text::ofJson);
        IDrawable.JSON_DRAWABLE_MAP.put("image", UITexture::ofJson);
    }

    private static final Map<String, WidgetFactory> REGISTRY = new HashMap<>();

    public static void registerWidgetSpecial(String id, WidgetFactory factory) {
        ModularUI.logger.info("Register type {}", id);
        REGISTRY.put(id, factory);
    }

    public static void registerWidget(String id, Supplier<Widget> factory) {
        ModularUI.logger.info("Register type {}", id);
        REGISTRY.put(id, player -> factory.get());
    }

    @Nullable
    public static WidgetFactory getFactory(String id) {
        return REGISTRY.get(id);
    }

    public interface WidgetFactory {
        Widget create(EntityPlayer player);
    }
}
