package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.drawable.Text;
import java.util.function.Supplier;
import net.minecraft.util.EnumChatFormatting;

public class DynamicTextWidget extends TextWidget {

    private final Supplier<Text> textSupplier;

    private Integer defaultColor;
    private EnumChatFormatting defaultFormat;

    public DynamicTextWidget(Supplier<Text> text) {
        this.textSupplier = text;
    }

    @Override
    public void onScreenUpdate() {
        String l = textSupplier.get().getFormatted();
        if (!l.equals(localised)) {
            checkNeedsRebuild();
            localised = l;
        }
    }

    @Override
    public Text getText() {
        Text ret = textSupplier.get();
        if (defaultColor != null) {
            ret.color(defaultColor);
        }
        if (defaultFormat != null) {
            ret.format(defaultFormat);
        }
        return ret;
    }

    @Override
    public TextWidget setDefaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    @Override
    public TextWidget setDefaultColor(EnumChatFormatting color) {
        this.defaultFormat = color;
        return this;
    }
}
