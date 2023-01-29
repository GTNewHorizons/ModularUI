package com.gtnewhorizons.modularui.common.widget;

import java.util.function.BiConsumer;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;

/**
 * Clickable button widget.
 */
public class ButtonWidget extends SyncedWidget implements Interactable {

    private IDrawable[] hoveredBackground;

    public static ButtonWidget openSyncedWindowButton(int id) {
        return (ButtonWidget) new ButtonWidget()
                .setOnClick(
                        (clickData, widget) -> { if (!widget.isClient()) widget.getContext().openSyncedWindow(id); })
                .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("Window"));
    }

    public static ButtonWidget closeWindowButton(boolean syncedWindow) {
        return (ButtonWidget) new ButtonWidget().setOnClick((clickData, widget) -> {
            if (!syncedWindow || !widget.isClient()) {
                widget.getWindow().closeWindow();
            }
        }).setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("x")).setSize(12, 12);
    }

    private BiConsumer<ClickData, Widget> clickAction;
    private boolean playClickSound = true;

    /**
     * Set callback that will be invoked when button is clicked.
     */
    public ButtonWidget setOnClick(BiConsumer<Widget.ClickData, Widget> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public ButtonWidget setPlayClickSound(boolean playClickSound) {
        this.playClickSound = playClickSound;
        return this;
    }

    public ButtonWidget setHoveredBackground(IDrawable... hoveredBackground) {
        this.hoveredBackground = hoveredBackground;
        return this;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Override
    public @Nullable IDrawable[] getBackground() {
        if (hoveredBackground != null && isHovering()) {
            return hoveredBackground;
        }
        return super.getBackground();
    }

    @Override
    public @Nullable String getBackgroundColorKey() {
        return Theme.KEY_BUTTON;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (clickAction != null) {
            Widget.ClickData clickData = Widget.ClickData.create(buttonId, doubleClick);
            clickAction.accept(clickData, this);
            if (syncsToServer()) {
                syncToServer(1, clickData::writeToPacket);
            }
            if (playClickSound) {
                Interactable.playButtonClickSound();
            }
            return ClickResult.ACCEPT;
        }
        return ClickResult.ACKNOWLEDGED;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {}

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            Widget.ClickData data = Widget.ClickData.readPacket(buf);
            clickAction.accept(data, this);
            markForUpdate();
        }
    }
}
