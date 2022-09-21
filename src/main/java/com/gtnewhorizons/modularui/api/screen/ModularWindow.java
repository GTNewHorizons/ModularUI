package com.gtnewhorizons.modularui.api.screen;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.animation.Eases;
import com.gtnewhorizons.modularui.api.animation.Interpolator;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.Theme;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.config.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * A window in a modular gui. Only the "main" window can exist on both, server and client.
 * All other only exist and needs to be opened on client.
 */
public class ModularWindow implements IWidgetParent {

    /**
     * Build your ModularWindow from here. You can chain methods as you want
     * and finally call {@link Builder#build()}.
     * <br> See also: {@link ITileWithModularUI#createWindow(UIBuildContext)}
     */
    public static Builder builder(int width, int height) {
        return new Builder(new Size(width, height));
    }

    public static Builder builder(Size size) {
        return new Builder(size);
    }

    public static Builder builderFullScreen() {
        return new Builder(Size.ZERO);
    }

    private ModularUIContext context;
    private final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    private final BiMap<Integer, ISyncedWidget> dynamicSyncedWidgets = HashBiMap.create();
    private final List<Interactable> interactionListeners = new ArrayList<>();
    protected boolean initialized = false;
    protected boolean clientOnly = true;

    private final boolean fullScreen;
    private Size size;
    private Pos2d pos = Pos2d.ZERO;
    private PosProvider posProvider;
    private final Alignment alignment = Alignment.Center;
    private final IDrawable[] background;
    protected boolean draggable;
    private boolean enabled = true;
    private boolean needsRebuild = false;
    private boolean initSync = true;
    private int alpha = Color.getAlpha(Theme.INSTANCE.getBackground());
    private float scale = 1f;
    private float rotation = 0;
    private float translateX = 0, translateY = 0;
    private Interpolator openAnimation, closeAnimation;
    private int guiTint = 0xffffff;

    public ModularWindow(Size size, List<Widget> children, IDrawable... background) {
        this.fullScreen = size.isZero();
        this.size = size;
        this.children = children;
        this.background = background;
        // latest point at which synced widgets can be added
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof IWidgetParent) {
                ((IWidgetParent) widget).initChildren();
            }
        });

        ImmutableBiMap.Builder<Integer, ISyncedWidget> syncedWidgetBuilder = ImmutableBiMap.builder();
        AtomicInteger i = new AtomicInteger();
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof ISyncedWidget) {
                syncedWidgetBuilder.put(i.getAndIncrement(), (ISyncedWidget) widget);
            }
            if (i.get() == 0x10000) {
                throw new IndexOutOfBoundsException("Too many synced widgets!");
            }
            return false;
        });
        this.syncedWidgets = syncedWidgetBuilder.build();
        this.posProvider = ((screenSize, mainWindow) -> Alignment.Center.getAlignedPos(screenSize, this.size));
    }

    protected void initialize(ModularUIContext context) {
        this.context = context;
        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }
    }

    public void onResize(Size screenSize) {
        if (this.fullScreen) {
            this.size = screenSize;
            this.pos = Pos2d.ZERO;
        } else if (!this.context.tryApplyStoredPos(this)) {
            setPos(this.posProvider.getPos(screenSize, this.context.getMainWindow()));
        }
        markNeedsRebuild();
    }

    public static boolean anyAnimation() {
        return Config.openCloseDurationMs > 0
                && (Config.openCloseFade
                        || Config.openCloseTranslateFromBottom
                        || Config.openCloseScale
                        || Config.openCloseRotateFast);
    }

    /**
     * The final call after the window is initialized & positioned
     */
    public void onOpen() {
        if (openAnimation == null && anyAnimation()) {
            final int startY = context.getScaledScreenSize().height - pos.y;
            openAnimation = new Interpolator(
                    0,
                    1,
                    Config.openCloseDurationMs,
                    Eases.EaseQuadOut,
                    value -> {
                        float val = (float) value;
                        if (Config.openCloseFade) {
                            alpha = (int) (val * Color.getAlpha(Theme.INSTANCE.getBackground()));
                        }
                        if (Config.openCloseTranslateFromBottom) {
                            translateY = startY * (1 - val);
                        }
                        if (Config.openCloseScale) {
                            scale = val;
                        }
                        if (Config.openCloseRotateFast) {
                            rotation = val * 360;
                        }
                    },
                    val -> {
                        alpha = Color.getAlpha(Theme.INSTANCE.getBackground());
                        translateX = 0;
                        translateY = 0;
                        scale = 1f;
                        rotation = 360;
                    });
            closeAnimation = openAnimation.getReversed(Config.openCloseDurationMs, Eases.EaseQuadIn);
            openAnimation.forward();
            closeAnimation.setCallback(val -> {
                closeWindow();
                openAnimation = null;
                closeAnimation = null;
            });
        }
        // this.pos = new Pos2d(pos.x, getContext().getScaledScreenSize().height);
    }

    /**
     * Called when the player tries to close the ui. Starts animation or closes directly.
     */
    public boolean tryClose() {
        return tryClose(true);
    }

    public boolean tryClose(boolean doRemoveWindow) {
        if (closeAnimation == null) {
            closeWindow(doRemoveWindow);
        } else if (!closeAnimation.isRunning()) {
            closeAnimation.forward();
            return true;
        }
        return false;
    }

    public boolean isClosing() {
        return closeAnimation != null && closeAnimation.isRunning();
    }

    public void update() {
        for (IDrawable drawable : background) {
            drawable.tick();
        }
        IWidgetParent.forEachByLayer(this, widget -> {
            widget.onScreenUpdate();
            Consumer<Widget> ticker = widget.getTicker();
            if (ticker != null) {
                ticker.accept(widget);
            }
            IDrawable[] background = widget.getBackground();
            if (background != null) {
                for (IDrawable drawable : background) {
                    if (drawable != null) {
                        drawable.tick();
                    }
                }
            }
        });
        if (needsRebuild) {
            rebuild();
        }
    }

    public void frameUpdate(float partialTicks) {
        if (openAnimation != null) {
            openAnimation.update(partialTicks);
        }
        if (closeAnimation != null) {
            closeAnimation.update(partialTicks);
        }
    }

    public void serverUpdate() {
        boolean needsUpdate = false;
        for (ISyncedWidget syncedWidget : syncedWidgets.values()) {
            syncedWidget.detectAndSendChanges(this.initSync);
            if (syncedWidget.isMarkedForUpdate()) {
                needsUpdate = true;
                syncedWidget.unMarkForUpdate();
            }
        }
        if (needsUpdate) {
            getContext().onWidgetUpdate();
        }
        this.initSync = false;
    }

    @SideOnly(Side.CLIENT)
    protected void rebuild() {
        // check auto size of each child from top to bottom
        for (Widget child : getChildren()) {
            child.buildTopToBottom(size.asDimension());
        }
        // position widgets from bottom to top
        for (Widget child : getChildren()) {
            child.buildBottomToTop();
        }
        needsRebuild = false;
    }

    /**
     * Close window instantly. For animated closing, use {@link #tryClose}
     */
    public void closeWindow() {
        closeWindow(true);
    }

    public void closeWindow(boolean doRemoveWindow) {
        context.closeWindow(this, doRemoveWindow);
    }

    protected void destroyWindow() {
        IWidgetParent.forEachByLayer(this, widget -> {
            if (isEnabled()) {
                widget.onPause();
            }
            widget.onDestroy();
        });
    }

    public void drawWidgets(float partialTicks, boolean foreground) {
        if (foreground) {
            IWidgetParent.forEachByLayer(this, widget -> {
                widget.drawInForeground(partialTicks);
                return false;
            });

        } else {
            GlStateManager.pushMatrix();
            // rotate around center
            if (Config.openCloseRotateFast) {
                GlStateManager.translate(pos.x + size.width / 2f, pos.y + size.height / 2f, 0);
                GlStateManager.rotate(rotation, 0, 0, 1);
                GlStateManager.translate(-(pos.x + size.width / 2f), -(pos.y + size.height / 2f), 0);
            }
            GlStateManager.translate(translateX, translateY, 0);
            GlStateManager.scale(scale, scale, 1);

            GlStateManager.pushMatrix();
            float x = (pos.x + size.width / 2f * (1 - scale)) / scale;
            float y = (pos.y + size.height / 2f * (1 - scale)) / scale;
            GlStateManager.translate(x, y, 0);
            int color = Color.withAlpha(Theme.INSTANCE.getBackground(), alpha);
            for (IDrawable drawable : background) {
                drawable.applyThemeColor(color);
                IDrawable.applyTintColor(getGuiTint());
                drawable.draw(Pos2d.ZERO, size, partialTicks);
            }
            GlStateManager.popMatrix();

            for (Widget widget : getChildren()) {
                widget.drawInternal(partialTicks);
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Pos2d getAbsolutePos() {
        return pos;
    }

    @Override
    public Pos2d getPos() {
        return pos;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public int getGuiTint() {
        return guiTint;
    }

    public void setGuiTint(int guiTint) {
        this.guiTint = guiTint;
    }

    public ModularUIContext getContext() {
        return context;
    }

    public void markNeedsRebuild() {
        this.needsRebuild = true;
    }

    public void setPos(Pos2d pos) {
        this.pos = pos;
        this.context.storeWindowPos(this, pos);
    }

    public boolean doesNeedRebuild() {
        return needsRebuild;
    }

    public float getScale() {
        return scale;
    }

    public int getAlpha() {
        return alpha;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                IWidgetParent.forEachByLayer(this, Widget::onResume);
            } else {
                IWidgetParent.forEachByLayer(this, Widget::onPause);
            }
        }
    }

    public Rectangle getRectangle() {
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }

    public IDrawable[] getBackground() {
        return background;
    }

    /**
     * The events of the added listeners are always called.
     */
    public void addInteractionListener(Interactable interactable) {
        interactionListeners.add(interactable);
    }

    public List<Interactable> getInteractionListeners() {
        return interactionListeners;
    }

    /**
     * Adds a dynamic synced widget.
     *
     * @param id           a id for the synced widget. Must be 32768 > id > 0
     * @param syncedWidget dynamic synced widget
     * @param parent       the synced widget that added the dynamic widget
     * @throws IllegalArgumentException if id is > 32767 or < 1
     * @throws NullPointerException     if dynamic widget or parent is null
     * @throws IllegalStateException    if parent is a dynamic widget
     */
    public void addDynamicSyncedWidget(int id, ISyncedWidget syncedWidget, ISyncedWidget parent) {
        if (id <= 0 || id > 0xFFFF) {
            throw new IllegalArgumentException(
                    "Dynamic Synced widget id must be greater than 0 and smaller than 65535 (0xFFFF)");
        }
        if (syncedWidget == null || parent == null) {
            throw new NullPointerException("Can't add dynamic null widget or with null parent!");
        }
        if (dynamicSyncedWidgets.containsValue(syncedWidget)) {
            dynamicSyncedWidgets.inverse().remove(syncedWidget);
        }
        int parentId = getSyncedWidgetId(parent);
        if ((parentId & ~0xFFFF) != 0) {
            throw new IllegalStateException(
                    "Dynamic synced widgets can't have other dynamic widgets as parent! It's possible with some trickery tho.");
        }
        // generate unique id
        // first 2 bytes is passed id, last 2 bytes is parent id
        id = ((id << 16) & ~0xFFFF) | parentId;
        dynamicSyncedWidgets.put(id, syncedWidget);
    }

    public int getSyncedWidgetId(ISyncedWidget syncedWidget) {
        Integer id = syncedWidgets.inverse().get(syncedWidget);
        if (id == null) {
            id = dynamicSyncedWidgets.inverse().get(syncedWidget);
            if (id == null) {
                throw new NoSuchElementException("Can't find id for ISyncedWidget " + syncedWidget);
            }
        }
        return id;
    }

    public ISyncedWidget getSyncedWidget(int id) {
        ISyncedWidget syncedWidget = syncedWidgets.get(id);
        if (syncedWidget == null) {
            syncedWidget = dynamicSyncedWidgets.get(id);
            if (syncedWidget == null) {
                throw new NoSuchElementException("Can't find ISyncedWidget for id " + id);
            }
        }
        return syncedWidget;
    }

    public static class Builder implements IWidgetBuilder<Builder> {

        private final List<Widget> widgets = new ArrayList<>();
        private IDrawable[] background = {};
        private Size size;
        private PosProvider pos = null;
        private boolean draggable = true;
        private Integer guiTint;

        private Builder(Size size) {
            this.size = size;
        }

        /**
         * Set background, but not limited to png files.
         * See {@link ModularUITextures} for default examples.
         */
        public Builder setBackground(IDrawable... background) {
            this.background = background;
            return this;
        }

        public Builder setSize(Size size) {
            this.size = size;
            return this;
        }

        public Builder setSize(int width, int height) {
            return setSize(new Size(width, height));
        }

        /**
         * Set position of this Window displayed.
         * {@link Alignment#getAlignedPos(Size, Size)} is useful for specifying rough location.
         * Center is selected as default.
         * @param pos BiFunction providing {@link Pos2d}
         *            out of sizes of game screen and this window
         */
        public Builder setPos(PosProvider pos) {
            this.pos = pos;
            return this;
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        public Builder bindPlayerInventory(EntityPlayer player, int marginBottom, IDrawable background) {
            return bindPlayerInventory(
                    player, new Pos2d(size.width / 2 - 81, size.height - marginBottom - 76), background);
        }

        /**
         * Bind player inventory to window.
         */
        public Builder bindPlayerInventory(EntityPlayer player) {
            return bindPlayerInventory(player, 7, null);
        }

        public Builder addPlayerInventoryLabel(int x, int y) {
            return widget(new TextWidget(Text.localised("container.inventory")).setPos(x, y));
        }

        @Override
        public void addWidgetInternal(Widget widget) {
            widgets.add(widget);
        }

        public Builder setGuiTint(int guiTint) {
            this.guiTint = guiTint;
            return this;
        }

        /**
         * Build this window.
         */
        public ModularWindow build() {
            ModularWindow window = new ModularWindow(size, widgets, background);
            window.draggable = draggable;
            if (pos != null) {
                window.posProvider = pos;
            }
            if (guiTint != null) {
                window.guiTint = guiTint;
            }
            return window;
        }
    }

    public interface PosProvider {
        Pos2d getPos(Size screenSize, ModularWindow mainWindow);
    }
}
