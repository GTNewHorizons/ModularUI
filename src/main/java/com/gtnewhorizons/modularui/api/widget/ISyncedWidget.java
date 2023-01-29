package com.gtnewhorizons.modularui.api.widget;

import java.io.IOException;
import java.util.function.Consumer;

import net.minecraft.network.PacketBuffer;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Implement this to let them synchronize data between server and client.
 */
public interface ISyncedWidget {

    /**
     * Receive packet sent from server.
     */
    @SideOnly(Side.CLIENT)
    void readOnClient(int id, PacketBuffer buf) throws IOException;

    /**
     * Receive packet sent from client.
     */
    void readOnServer(int id, PacketBuffer buf) throws IOException;

    /**
     * Called AT LEAST each tick on server. Use it to detect and sync changes
     *
     * @param init true if it is called the first time after init
     */
    default void detectAndSendChanges(boolean init) {}

    /**
     * Mark this widget as "updated". Call this on appropriate timing on {@link #detectAndSendChanges} or
     * {@link #readOnServer} etc. invocation.
     */
    void markForUpdate();

    void unMarkForUpdate();

    boolean isMarkedForUpdate();

    /**
     * Sends the written data to {@link #readOnServer(int, PacketBuffer)}
     *
     * @param id         helper to determine the type. Must not be -1!
     * @param bufBuilder data builder
     */
    @SideOnly(Side.CLIENT)
    default void syncToServer(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (id == -1) {
            throw new IllegalArgumentException("Id -1 is already reserved for syncing!");
        }
        getWindow().getContext().sendClientPacket(id, this, getWindow(), bufBuilder);
    }

    /**
     * Sends the written data to {@link #readOnClient(int, PacketBuffer)}
     *
     * @param id         helper to determine the type. Must not be -1!
     * @param bufBuilder data builder
     */
    default void syncToClient(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (id == -1) {
            throw new IllegalArgumentException("Id -1 is already reserved for syncing!");
        }
        getWindow().getContext().sendServerPacket(id, this, getWindow(), bufBuilder);
    }

    ModularWindow getWindow();
}
