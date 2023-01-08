package com.gtnewhorizons.modularui.api.screen;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.common.internal.network.CWidgetUpdate;
import com.gtnewhorizons.modularui.common.internal.network.NetworkHandler;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.network.SWidgetUpdate;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

/**
 * Holds meta info around {@link ModularUIContainer}.
 */
public class ModularUIContext {

    private final ImmutableMap<Integer, IWindowCreator> syncedWindowsCreators;
    private final Deque<ModularWindow> windows = new LinkedList<>();
    private final BiMap<Integer, ModularWindow> syncedWindows = HashBiMap.create(4);
    private final Map<ModularWindow, Pos2d> lastWindowPos = new HashMap<>();
    private ModularWindow mainWindow;

    @SideOnly(Side.CLIENT)
    private ModularGui screen;

    private final EntityPlayer player;
    private final Cursor cursor;
    private final List<Integer> queuedOpenWindow = new ArrayList<>();
    public final boolean clientOnly;
    private boolean isClosing = false;
    private final List<Runnable> closeListeners;
    private final Runnable onWidgetUpdate;
    private final Supplier<Boolean> validator;
    private final boolean showNEI;

    private Size screenSize = NetworkUtils.isDedicatedClient()
            ? new Size(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight)
            : Size.ZERO;

    private ModularUIContainer container;

    public ModularUIContext(UIBuildContext context, Runnable onWidgetUpdate) {
        this(context, onWidgetUpdate, false);
    }

    public ModularUIContext(UIBuildContext context, Runnable onWidgetUpdate, boolean clientOnly) {
        this.player = context.player;
        if (!isClient() && clientOnly) {
            throw new IllegalArgumentException("Client only ModularUI can not be opened on server!");
        }
        this.clientOnly = clientOnly;
        this.syncedWindowsCreators = context.syncedWindows.build();
        this.cursor = new Cursor(this);
        this.closeListeners = context.closeListeners;
        this.onWidgetUpdate = onWidgetUpdate;
        this.validator = context.validator;
        this.showNEI = context.showNEI;
    }

    /**
     * Iterates over {@link #windows} from bottom (main window) to top.
     * This method is protected against ConcurrentModificationException caused by forEach method
     * triggering window closing.
     */
    public void forEachWindowBottomToTop(Consumer<ModularWindow> forEach) {
        for (ModularWindow window : Lists.newArrayList(getOpenWindowsReversed())) {
            forEach.accept(window);
        }
    }

    /**
     * Iterates over {@link #windows} from top to bottom (main window).
     * This method is protected against ConcurrentModificationException caused by forEach method
     * triggering window closing.
     */
    public void forEachWindowTopToBottom(Consumer<ModularWindow> forEach) {
        for (ModularWindow window : Lists.newArrayList(getOpenWindows())) {
            forEach.accept(window);
        }
    }

    public boolean isClient() {
        return NetworkUtils.isClient();
    }

    public void initialize(ModularUIContainer container, ModularWindow mainWindow) {
        this.container = container;
        this.mainWindow = mainWindow;
        pushWindow(mainWindow);
        this.syncedWindows.put(0, mainWindow);
        mainWindow.draggable = false;
        if (isClient()) {
            // if on client, notify the server that the client initialized, to allow syncing to client
            mainWindow.initialized = true;
            mainWindow.clientOnly = false;
            sendClientPacket(DataCodes.SYNC_INIT, null, mainWindow, NetworkUtils.EMPTY_PACKET);
        }
    }

    @SideOnly(Side.CLIENT)
    public void initializeClient(ModularGui screen) {
        this.screen = screen;
    }

    @SideOnly(Side.CLIENT)
    public void buildWindowOnStart() {
        for (ModularWindow window : windows) {
            window.rebuild();
        }
    }

    @SideOnly(Side.CLIENT)
    public void resize(Size scaledSize) {
        this.screenSize = scaledSize;
        for (ModularWindow window : this.windows) {
            window.onResize(scaledSize);
            if (window == this.mainWindow) {
                getScreen().setMainWindowArea(window.getPos(), window.getSize());
            }
        }
    }

    public void onClientTick() {
        if (!queuedOpenWindow.isEmpty()) {
            queuedOpenWindow.removeIf(windowId -> {
                ModularWindow oldWindow = syncedWindows.get(windowId);
                if (oldWindow != null && oldWindow.isClosing()) return false;
                ModularWindow newWindow = openWindow(syncedWindowsCreators.get(windowId));
                syncedWindows.put(windowId, newWindow);
                newWindow.onResize(screenSize);
                newWindow.rebuild();
                newWindow.onOpen();
                newWindow.initialized = true;
                newWindow.clientOnly = false;
                sendClientPacket(DataCodes.INIT_WINDOW, null, newWindow, NetworkUtils.EMPTY_PACKET);
                return true;
            });
        }
    }

    public boolean isWindowOpen(int id) {
        return this.syncedWindows.containsKey(id);
    }

    public void openSyncedWindow(int id) {
        if (isClient()) {
            ModularUI.logger.error("Synced windows must be opened on server!");
            return;
        }
        if (isWindowOpen(id)) {
            return;
        }
        if (syncedWindowsCreators.containsKey(id)) {
            sendServerPacket(DataCodes.OPEN_WINDOW, null, mainWindow, buf -> buf.writeVarIntToBuffer(id));
            ModularWindow window = openWindow(syncedWindowsCreators.get(id));
            syncedWindows.put(id, window);
        } else {
            ModularUI.logger.error("Could not find window with id {}", id);
        }
    }

    public void openClientWindow(IWindowCreator windowCreator) {
        ModularWindow newWindow = openWindow(windowCreator);
        newWindow.onResize(screenSize);
        newWindow.rebuild();
        newWindow.onOpen();
        newWindow.initialized = true;
    }

    public ModularWindow openWindow(IWindowCreator windowCreator) {
        ModularWindow window = windowCreator.create(player);
        pushWindow(window);
        return window;
    }

    public void closeWindow(int id) {
        if (syncedWindows.containsKey(id)) {
            closeWindow(syncedWindows.get(id));
        } else {
            ModularUI.logger.error("Could not close window with id {}", id);
        }
    }

    public void closeWindow(ModularWindow window) {
        if (window == null) {
            return;
        }
        if (windows.removeLastOccurrence(window)) {
            window.destroyWindow();
        }
        if (isClient()) {
            if (!hasWindows() || window == mainWindow) {
                close();
            }
        } else {
            sendServerPacket(DataCodes.CLOSE_WINDOW, null, window, NetworkUtils.EMPTY_PACKET);
        }
        if (syncedWindows.containsValue(window)) {
            syncedWindows.inverse().remove(window);
        }
    }

    private void pushWindow(ModularWindow window) {
        if (windows.offerLast(window)) {
            window.initialize(this);
        } else {
            ModularUI.logger.error("Failed opening window");
        }
    }

    public ModularWindow getCurrentWindow() {
        return windows.isEmpty() ? mainWindow : windows.peekLast();
    }

    public ModularWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * Closes all windows.
     */
    public void tryClose() {
        if (this.isClosing) {
            forEachWindowTopToBottom(this::closeWindow);
            return;
        }
        forEachWindowTopToBottom(window -> {
            if (window.tryClose() && window == mainWindow) {
                this.isClosing = true;
            }
        });
    }

    public void close() {
        player.closeScreen();
    }

    public void closeAllButMain() {
        forEachWindowTopToBottom(window -> {
            if (window != mainWindow) {
                window.tryClose();
            }
        });
    }

    public void storeWindowPos(ModularWindow window, Pos2d pos) {
        if (windows.contains(window)) {
            this.lastWindowPos.put(window, pos);
        }
    }

    public boolean tryApplyStoredPos(ModularWindow window) {
        if (window == this.mainWindow || !this.windows.contains(window)) return false;
        if (this.lastWindowPos.containsKey(window)) {
            window.setPos(this.lastWindowPos.get(window));
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public Pos2d getMousePos() {
        return screen.getMousePos();
    }

    public boolean hasWindows() {
        return !windows.isEmpty();
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ModularUIContainer getContainer() {
        return container;
    }

    public ModularGui getScreen() {
        return screen;
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    public Cursor getCursor() {
        return cursor;
    }

    /**
     * @return {@link ModularWindow}s from top to bottom (main window)
     */
    public Iterable<ModularWindow> getOpenWindows() {
        return windows::descendingIterator;
    }

    /**
     * @return {@link ModularWindow}s from bottom (main window) to top
     */
    public Iterable<ModularWindow> getOpenWindowsReversed() {
        return windows;
    }

    @SideOnly(Side.CLIENT)
    public Size getScaledScreenSize() {
        return screenSize;
    }

    public void onWidgetUpdate() {
        if (onWidgetUpdate != null) {
            onWidgetUpdate.run();
        }
    }

    public Supplier<Boolean> getValidator() {
        return validator;
    }

    public boolean doShowNEI() {
        return showNEI;
    }

    public List<Runnable> getCloseListeners() {
        return closeListeners;
    }

    public void addCloseListener(Runnable runnable) {
        this.closeListeners.add(runnable);
    }

    public void syncSlotContent(BaseSlot slot) {
        if (slot != getContainer().inventorySlots.get(slot.slotNumber)) {
            throw new IllegalStateException("Slot does not have the same index in the container!");
        }
        getContainer().sendSlotChange(slot.getStack(), slot.slotNumber);
    }

    /**
     * Client -> Server
     */
    public void readClientPacket(PacketBuffer buf, int widgetId) throws IOException {
        int id = buf.readVarIntFromBuffer();
        ModularWindow window = syncedWindows.get(buf.readVarIntFromBuffer());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_INIT) {
                mainWindow.initialized = true;
                this.mainWindow.clientOnly = false;
            } else if (id == DataCodes.INIT_WINDOW) {
                window.initialized = true;
            } else if (id == DataCodes.CLOSE_WINDOW) {
                if (windows.removeLastOccurrence(window)) {
                    window.destroyWindow();
                }
                syncedWindows.inverse().remove(window);
            }
        } else if (window != null) {
            ISyncedWidget syncedWidget = window.getSyncedWidget(widgetId);
            syncedWidget.readOnServer(id, buf);
        }
    }

    /**
     * Server -> Client
     */
    @SideOnly(Side.CLIENT)
    public void readServerPacket(PacketBuffer buf, int widgetId) throws IOException {
        int id = buf.readVarIntFromBuffer();
        ModularWindow window = syncedWindows.get(buf.readVarIntFromBuffer());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_CURSOR_STACK) {
                player.inventory.setItemStack(buf.readItemStackFromBuffer());
            } else if (id == DataCodes.OPEN_WINDOW) {
                queuedOpenWindow.add(buf.readVarIntFromBuffer());
            } else if (id == DataCodes.CLOSE_WINDOW) {
                window.tryClose();
            }
        } else if (window != null) {
            ISyncedWidget syncedWidget = window.getSyncedWidget(widgetId);
            syncedWidget.readOnClient(id, buf);
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendClientPacket(
            int discriminator,
            ISyncedWidget syncedWidget,
            ModularWindow window,
            Consumer<PacketBuffer> bufferConsumer) {
        if (isClient() && !isClientOnly()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.logger.error("Window is not synced!");
                ModularUI.logger.warn(
                        "stacktrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarIntToBuffer(discriminator);
            buffer.writeVarIntToBuffer(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            CWidgetUpdate packet = new CWidgetUpdate(buffer, syncId);
            NetworkHandler.sendToServer(packet);
        }
    }

    public void sendServerPacket(
            int discriminator,
            ISyncedWidget syncedWidget,
            ModularWindow window,
            Consumer<PacketBuffer> bufferConsumer) {
        if (!isClient()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.logger.error("Window is not synced!");
                ModularUI.logger.warn(
                        "stacktrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarIntToBuffer(discriminator);
            buffer.writeVarIntToBuffer(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            SWidgetUpdate packet = new SWidgetUpdate(buffer, syncId);
            NetworkHandler.sendToPlayer(packet, (EntityPlayerMP) player);
        }
    }

    public static class DataCodes {
        public static final int INTERNAL_SYNC = -1;
        public static final int SYNC_CURSOR_STACK = 1;
        public static final int SYNC_INIT = 2;
        public static final int OPEN_WINDOW = 3;
        public static final int INIT_WINDOW = 4;
        public static final int CLOSE_WINDOW = 5;
    }
}
