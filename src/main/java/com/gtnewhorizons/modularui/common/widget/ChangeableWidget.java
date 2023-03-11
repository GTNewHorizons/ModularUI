package com.gtnewhorizons.modularui.common.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

@ApiStatus.Experimental
public class ChangeableWidget extends Widget implements ISyncedWidget, IWidgetParent {

    private boolean needsUpdate;

    private final List<Widget> child = new ArrayList<>();

    @Nullable
    private Widget queuedChild = null;

    private final Supplier<Widget> widgetSupplier;
    private boolean initialised = false;
    private boolean firstTick = true;

    /**
     * Creates a widget which child can be changed dynamically. Call {@link #notifyChangeServer()} to notify the widget
     * for a change.
     *
     * @param widgetSupplier widget to supply. Can return null
     */
    public ChangeableWidget(Supplier<Widget> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (this.child.isEmpty()) {
            return Size.ZERO;
        }
        return this.child.get(0).getSize();
    }

    /**
     * Notifies the widget that the child probably changed. Only executed on server and synced to client. This method is
     * preferred!
     */
    public void notifyChangeServer() {
        if (!isClient()) {
            notifyChange(true);
        }
    }

    /**
     * Notifies the widget that the child probably changed. Only executed on client and NOT synced to server.
     */
    public void notifyChangeClient() {
        notifyChangeNoSync();
    }

    /**
     * Notifies the widget that the child probably changed. Can execute on both sides and NOT synced.
     */
    public void notifyChangeNoSync() {
        notifyChange(false);
    }

    private void notifyChange(boolean sync) {
        if (this.widgetSupplier == null || !isInitialised()) {
            return;
        }
        if (sync && !isClient()) {
            syncToClient(0, NetworkUtils.EMPTY_PACKET);
        }
        removeCurrentChild();
        this.queuedChild = this.widgetSupplier.get();
        this.initialised = false;
    }

    private void initQueuedChild() {
        if (this.queuedChild != null) {
            final Consumer<Widget> initChildrenWrapper = widget -> {
                if (widget instanceof IWidgetParent) {
                    ((IWidgetParent) widget).initChildren();
                }
            };
            initChildrenWrapper.accept(this.queuedChild);
            IWidgetParent.forEachByLayer(this.queuedChild, initChildrenWrapper);
            AtomicInteger syncId = new AtomicInteger(1);
            final Consumer<Widget> initSyncedWidgetWrapper = widget1 -> {
                if (widget1 instanceof ISyncedWidget) {
                    getWindow().addDynamicSyncedWidget(syncId.getAndIncrement(), (ISyncedWidget) widget1, this);
                }
            };
            if (queuedChild instanceof IWidgetParent) initSyncedWidgetWrapper.accept(queuedChild);
            IWidgetParent.forEachByLayer(this.queuedChild, initSyncedWidgetWrapper);
            this.queuedChild.initialize(getWindow(), this, getLayer() + 1);
            this.child.add(this.queuedChild);
            this.initialised = true;
            this.queuedChild = null;
            this.firstTick = true;
        }
        checkNeedsRebuild();
    }

    public void removeCurrentChild() {
        if (!this.child.isEmpty()) {
            Widget widget = this.child.get(0);
            widget.setEnabled(false);
            if (widget instanceof IWidgetParent) {
                widget.onPause();
                widget.onDestroy();
            }
            IWidgetParent.forEachByLayer(widget, Widget::onPause);
            IWidgetParent.forEachByLayer(widget, Widget::onDestroy);
            this.child.clear();
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (init) {
            notifyChangeServer();
        }
        if (this.initialised && !this.child.isEmpty()) {
            final Consumer<Widget> detectAndSendChangesWrapper = widget -> {
                if (widget instanceof ISyncedWidget) {
                    ((ISyncedWidget) widget).detectAndSendChanges(firstTick);
                }
            };
            Widget widget = this.child.get(0);
            if (widget instanceof IWidgetParent) detectAndSendChangesWrapper.accept(widget);
            IWidgetParent.forEachByLayer(widget, detectAndSendChangesWrapper);
            firstTick = false;
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 0) {
            notifyChange(false);
            initQueuedChild();
            syncToServer(1, NetworkUtils.EMPTY_PACKET);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 1) { // unused ?
            initQueuedChild();
        }
    }

    @Override
    public void markForUpdate() {
        needsUpdate = true;
    }

    @Override
    public void unMarkForUpdate() {
        needsUpdate = false;
    }

    @Override
    public boolean isMarkedForUpdate() {
        return needsUpdate;
    }

    @Override
    public List<Widget> getChildren() {
        return this.child;
    }
}
