package com.gtnewhorizons.modularui.api.screen;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class UIBuildContext {

    protected final EntityPlayer player;
    private final Map<String, Widget> jsonWidgets = new HashMap<>();
    protected final ImmutableMap.Builder<Integer, IWindowCreator> syncedWindows = new ImmutableMap.Builder<>();
    protected final List<Runnable> closeListeners = new ArrayList<>();
    protected Supplier<Boolean> validator;
    protected boolean showNEI = true;

    public UIBuildContext(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    /**
     * Sets validator that will be called on every tick on server. If validator returns false, it means GUI is in
     * invalid state, and GUI will be closed.
     */
    public void setValidator(Supplier<Boolean> validator) {
        this.validator = validator;
    }

    public void addJsonWidgets(String name, Widget widget) {
        if (jsonWidgets.containsKey(name)) {
            ModularUI.logger.warn("Widget {} is already registered from json", name);
        }
        jsonWidgets.put(name, widget);
    }

    public void addCloseListener(Runnable runnable) {
        closeListeners.add(runnable);
    }

    @Nullable
    public Widget getJsonWidget(String name) {
        return jsonWidgets.get(name);
    }

    @Nullable
    public <T extends Widget> T getJsonWidget(String name, Class<T> clazz) {
        Widget widget = getJsonWidget(name);
        if (widget != null && widget.getClass().isAssignableFrom(clazz)) {
            return (T) widget;
        }
        return null;
    }

    public <T extends Widget> void applyToWidget(String name, Class<T> clazz, Consumer<T> consumer) {
        T t = getJsonWidget(name, clazz);
        if (t != null) {
            consumer.accept(t);
        } else {
            ModularUI.logger
                    .error("Expected Widget with name {}, of class {}, but was not found!", name, clazz.getName());
        }
    }

    /**
     * Registers synced window that can be displayed on top of main window. Call
     * {@link ModularUIContext#openSyncedWindow} to actually open the window.
     */
    public void addSyncedWindow(int id, IWindowCreator windowCreator) {
        if (id <= 0) {
            ModularUI.logger.error("Window id must be > 0");
            return;
        }
        syncedWindows.put(id, Objects.requireNonNull(windowCreator));
    }

    public void setShowNEI(boolean showNEI) {
        this.showNEI = showNEI;
    }
}
