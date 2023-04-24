package com.gtnewhorizons.modularui.api.drawable;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.ApiStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.common.internal.JsonHelper;
import com.gtnewhorizons.modularui.common.internal.Theme;

import cpw.mods.fml.common.FMLCommonHandler;

public class Text implements IDrawable {

    public static final Text EMPTY = new Text("");

    private static final TextRenderer renderer = new TextRenderer();
    private Alignment alignment = Alignment.Center;
    private final String text;
    private String formatting = "";

    @Nullable
    private Supplier<Object[]> localisationData;

    private int color;
    private boolean shadow = false;

    public Text(String text) {
        this.text = Objects.requireNonNull(text, "String in Text can't be null!");
        this.color = Theme.INSTANCE.getText();
    }

    public static Text localised(String key, Object... data) {
        return new Text(key).localise(data);
    }

    public static Text of(ChatComponentText textComponent) {
        return new Text(textComponent.getFormattedText());
    }

    public Text color(int color) {
        this.color = Color.withAlpha(color, 255);
        return this;
    }

    public Text format(EnumChatFormatting color) {
        this.formatting = color.toString() + this.formatting;
        return this;
    }

    public Text shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Text localise(Supplier<Object[]> localisationData) {
        this.localisationData = localisationData;
        return this;
    }

    public Text localise(Object... localisationData) {
        localise(() -> localisationData);
        return this;
    }

    public Text shadow() {
        return shadow(true);
    }

    public Text alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public int getColor() {
        return color;
    }

    public boolean hasColor() {
        return Color.getAlpha(color) > 0;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public String getRawText() {
        return text;
    }

    public String getFormatting() {
        return formatting;
    }

    @ApiStatus.Internal
    public void setFormatting(String formatting) {
        this.formatting = formatting;
    }

    @Override
    public void applyThemeColor(int color) {
        renderer.setColor(hasColor() ? this.color : Theme.INSTANCE.getText());
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        renderer.setPos((int) (x + 0.5), (int) (y + 0.5));
        renderer.setShadow(shadow);
        renderer.setAlignment(alignment, width, height);
        renderer.draw(getFormatted());
    }

    /**
     * @return Text translated and formatted with {@link EnumChatFormatting}
     */
    public String getFormatted() {
        String text = getRawText();
        if (localisationData != null && FMLCommonHandler.instance().getSide().isClient()) {
            text = I18n.format(text, localisationData.get()).replaceAll("\\\\n", "\n");
        }
        if (!this.formatting.isEmpty()) {
            text = formatting + text;
        }
        return text;
    }

    public static String getFormatted(Text... texts) {
        StringBuilder builder = new StringBuilder();
        for (Text text : texts) {
            builder.append(text.getFormatted());
        }
        return builder.toString();
    }

    public static Text ofJson(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            Text text = new Text(JsonHelper.getString(jsonObject, "E:404", "text"));
            text.shadow(JsonHelper.getBoolean(jsonObject, false, "shadow"));
            Integer color = JsonHelper.getElement(jsonObject, null, Color::ofJson, "color");
            if (color != null) {
                text.color(color);
            }
            if (JsonHelper.getBoolean(jsonObject, false, "localise")) {
                text.localise();
            }
            return text;
        }
        if (!json.isJsonArray()) {
            return new Text(json.getAsString());
        }
        return new Text("");
    }
}
