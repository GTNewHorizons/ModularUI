package com.gtnewhorizons.modularui.common.internal.network;

import java.io.IOException;

import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

public class CWidgetUpdate implements IPacket {

    public int widgetId;
    public PacketBuffer packet;

    public CWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public CWidgetUpdate() {}

    @Override
    public void decode(PacketBuffer buf) {
        this.widgetId = buf.readVarIntFromBuffer();
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarIntToBuffer(widgetId);
        NetworkUtils.writePacketBuffer(buf, packet);
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.playerEntity.openContainer;
        if (container instanceof ModularUIContainer) {
            ModularUIContext context = ((ModularUIContainer) container).getContext();
            try {
                context.readClientPacket(packet, widgetId);
            } catch (IOException e) {
                ModularUI.logger.error("Error reading client packet: ");
                e.printStackTrace();
            }
        } else {
            // hopefully harmless error, caused by some kind of network lag
            // ModularUI.logger.error("Expected ModularUIContainer on server, but got {}", container);
        }
        return null;
    }
}
