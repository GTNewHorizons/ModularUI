package com.gtnewhorizons.modularui.common.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.common.internal.JsonHelper;
import com.gtnewhorizons.modularui.common.internal.Theme;

public class CycleButtonWidget extends SyncedWidget implements Interactable {

    protected int state = 0;
    protected int length = 1;
    protected Consumer<Integer> setter;
    protected Supplier<Integer> getter;
    protected Function<Integer, IDrawable> textureGetter;
    protected Function<Integer, IDrawable[]> backgroundGetter;
    protected IDrawable texture = IDrawable.EMPTY;
    private final List<List<Text>> stateTooltip = new ArrayList<>();
    private boolean playClickSound = true;

    public CycleButtonWidget() {}

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        this.length = JsonHelper.getInt(json, 1, "length", "size");
        this.state = JsonHelper.getInt(json, 0, "defaultState");
        if (json.has("texture")) {
            JsonElement element = json.get("texture");
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                this.length = array.size();
                IDrawable[] textures = new IDrawable[this.length];
                for (int i = 0; i < array.size(); i++) {
                    JsonElement element1 = array.get(i);
                    if (element1.isJsonObject()) {
                        textures[i] = IDrawable.ofJson(element1.getAsJsonObject());
                    } else {
                        textures[i] = IDrawable.EMPTY;
                        ModularUI.logger.error("Texture needs to be a json object");
                    }
                }
                this.textureGetter = val -> textures[val];
            } else if (element.isJsonObject()) {
                IDrawable drawable = IDrawable.ofJson(element.getAsJsonObject());
                if (drawable instanceof UITexture) {
                    setTexture((UITexture) drawable);
                } else {
                    this.textureGetter = val -> drawable;
                }
            }
        }
    }

    @Override
    public void onInit() {
        if (setter == null || getter == null) {
            ModularUI.logger.warn("{} was not properly initialised!", this);
            return;
        }
        if (textureGetter == null) {
            ModularUI.logger.warn("Texture Getter of {} was not set!", this);
            textureGetter = val -> IDrawable.EMPTY;
        }
        setState(getter.get(), false, false);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_BUTTON;
    }

    public void next() {
        if (++state == length) {
            state = 0;
        }
        setState(state, true, true);
    }

    public void prev() {
        if (--state == -1) {
            state = length - 1;
        }
        setState(state, true, true);
    }

    public void setState(int state, boolean sync, boolean setSource) {
        if (state >= length) {
            throw new IndexOutOfBoundsException("CycleButton state out of bounds");
        }
        this.state = state;
        if (sync) {
            if (isClient()) {
                if (syncsToServer()) {
                    syncToServer(1, buffer -> buffer.writeVarIntToBuffer(state));
                }
            } else {
                syncToClient(1, buffer -> buffer.writeVarIntToBuffer(state));
            }
        }
        if (setSource) {
            setter.accept(this.state);
        }
        if (isClient()) {
            this.texture = textureGetter.apply(this.state);
            if (backgroundGetter != null) {
                setBackground(backgroundGetter.apply(this.state));
            }
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        switch (buttonId) {
            case 0:
                next();
                if (playClickSound) {
                    Interactable.playButtonClickSound();
                }
                return ClickResult.ACCEPT;
            case 1:
                prev();
                if (playClickSound) {
                    Interactable.playButtonClickSound();
                }
                return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient()) {
            int actualValue = getter.get();
            if (init || actualValue != state) {
                setState(actualValue, true, false);
                markForUpdate();
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        texture.applyThemeColor();
        texture.draw(Pos2d.ZERO, getSize(), partialTicks);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            setState(buf.readVarIntFromBuffer(), false, true);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            setState(buf.readVarIntFromBuffer(), false, true);
            markForUpdate();
        }
    }

    @Override
    public boolean hasTooltip() {
        return super.hasTooltip()
                || (this.stateTooltip.size() > this.state && !this.stateTooltip.get(this.state).isEmpty());
    }

    @Override
    public List<Text> getTooltip() {
        List<Text> texts = super.getTooltip();
        if (texts.isEmpty()) {
            return this.stateTooltip.get(this.state);
        }
        texts.addAll(this.stateTooltip.get(this.state));
        return texts;
    }

    public int getState() {
        return state;
    }

    public CycleButtonWidget setSetter(Consumer<Integer> setter) {
        this.setter = setter;
        return this;
    }

    public CycleButtonWidget setGetter(Supplier<Integer> getter) {
        this.getter = getter;
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget setForEnum(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        setSetter(val -> setter.accept(clazz.getEnumConstants()[val]));
        setGetter(() -> getter.get().ordinal());
        setLength(clazz.getEnumConstants().length);
        return this;
    }

    public CycleButtonWidget setToggle(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        setSetter(val -> setter.accept(val == 1));
        setGetter(() -> getter.get() ? 1 : 0);
        setLength(2);
        return this;
    }

    /**
     * Sets texture that will be changed depending on the value stored and drawn on top of background.
     *
     * @param textureGetter state of button -> texture
     */
    public CycleButtonWidget setTextureGetter(Function<Integer, IDrawable> textureGetter) {
        this.textureGetter = textureGetter;
        return this;
    }

    /**
     * Sets texture that will be changed depending on the value stored and drawn on top of background. Argument is
     * automatically split by the number of state length.
     */
    public CycleButtonWidget setTexture(UITexture texture) {
        return setTextureGetter(val -> {
            float a = 1f / length;
            return texture.getSubArea(0, val * a, 1, val * a + a);
        });
    }

    /**
     * Sets static texture that is drawn on top of background. Probably use along with {@link #setVariableBackground}.
     */
    public CycleButtonWidget setStaticTexture(IDrawable texture) {
        return setTextureGetter(val -> texture);
    }

    /**
     * Sets background that will be changed depending on the value stored.
     *
     * @param backgroundGetter state of button -> background
     */
    public CycleButtonWidget setVariableBackgroundGetter(Function<Integer, IDrawable[]> backgroundGetter) {
        this.backgroundGetter = backgroundGetter;
        return this;
    }

    /**
     * Sets background that will be changed depending on the value stored. Each element of arguments is automatically
     * split by the number of state length.
     */
    public CycleButtonWidget setVariableBackground(UITexture... textures) {
        return setVariableBackgroundGetter(val -> Arrays.stream(textures).map(texture -> {
            float a = 1f / length;
            return texture.getSubArea(0, val * a, 1, val * a + a);
        }).collect(Collectors.toList()).toArray(new IDrawable[] {}));
    }

    public CycleButtonWidget addTooltip(int state, Text tooltip) {
        if (state >= this.stateTooltip.size() || state < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.stateTooltip.get(state).add(tooltip);
        return this;
    }

    /**
     * Adds a line to the tooltip that can be changed depending on the value stored. Should be called after
     * {@link #setLength}.
     */
    public CycleButtonWidget addTooltip(int state, String tooltip) {
        return addTooltip(state, new Text(tooltip).color(Color.WHITE.normal));
    }

    /**
     * Adds tooltips with given function. Should be called after {@link #setLength}.
     *
     * @param tooltipFunc state -> tooltip
     */
    public CycleButtonWidget addTooltip(Function<Integer, String> tooltipFunc) {
        for (int i = 0; i < length; i++) {
            addTooltip(i, tooltipFunc.apply(i));
        }
        return this;
    }

    public CycleButtonWidget setLength(int length) {
        this.length = length;
        setupTooltip();
        return this;
    }

    public CycleButtonWidget setPlayClickSound(boolean playClickSound) {
        this.playClickSound = playClickSound;
        return this;
    }

    protected void setupTooltip() {
        while (this.stateTooltip.size() < this.length) {
            this.stateTooltip.add(new ArrayList<>());
        }
        while (this.stateTooltip.size() > this.length) {
            this.stateTooltip.remove(this.stateTooltip.size() - 1);
        }
    }
}
