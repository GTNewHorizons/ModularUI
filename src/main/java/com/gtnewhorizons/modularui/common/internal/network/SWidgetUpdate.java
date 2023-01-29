package com.gtnewhorizons.modularui.common.internal.network;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;

public class SWidgetUpdate implements IPacket {

    public int widgetId;
    public PacketBuffer packet;

    public SWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public SWidgetUpdate() {}

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
    public IPacket executeClient(NetHandlerPlayClient handler) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof ModularGui) {
            ModularUIContext context = ((ModularGui) screen).getContext();
            try {
                context.readServerPacket(packet, widgetId);
            } catch (IOException e) {
                ModularUI.logger.error("Error reading server packet: ");
                e.printStackTrace();
            }
        } else {
            // no-op
            // This can legitimately happen when:
            // - client opens NEI GuiRecipe
            // - client closes GUI (until server receives packet of closing GUI server-side)
        }
        return null;
    }
}
