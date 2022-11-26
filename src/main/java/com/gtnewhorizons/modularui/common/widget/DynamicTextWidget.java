package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;

/**
 * Allows changing text dynamically.
 * Syncs text from server to client.
 */
public class DynamicTextWidget extends TextWidget implements ISyncedWidget {

    private final Supplier<Text> textSupplier;

    private boolean syncsToClient = true;
    private Text lastText;

    private Integer defaultColor;
    private EnumChatFormatting defaultFormat;

    private static final int MAX_PACKET_LENGTH = Short.MAX_VALUE;

    public DynamicTextWidget(Supplier<Text> text) {
        this.textSupplier = text;
        this.isDynamic = true;
    }

    @Override
    public void onInit() {
        lastText = new Text("");
    }

    @Override
    public Text getText() {
        return syncsToClient() ? lastText : updateText();
    }

    /**
     * Executed only on server
     */
    private Text updateText() {
        Text ret = textSupplier.get();
        if (defaultColor != null) {
            ret.color(defaultColor);
        }
        if (defaultFormat != null) {
            ret.format(defaultFormat);
        }
        return ret;
    }

    @Override
    public TextWidget setDefaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    @Override
    public TextWidget setDefaultColor(EnumChatFormatting color) {
        this.defaultFormat = color;
        return this;
    }

    public DynamicTextWidget setSynced(boolean synced) {
        this.syncsToClient = synced;
        return this;
    }

    /**
     * @return if this widget should operate on the server side.
     * For example detecting and sending changes to client.
     */
    protected boolean syncsToClient() {
        return syncsToClient;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            Text newText = new Text(NetworkUtils.readStringSafe(buf));
            newText.color(buf.readVarIntFromBuffer());
            newText.setFormatting(NetworkUtils.readStringSafe(buf));
            newText.shadow(buf.readBoolean());
            lastText = newText;
            checkNeedsRebuild();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}

    @Override
    public void detectAndSendChanges(boolean init) {
        if (!syncsToClient()) return;
        Text newText = updateText();
        if (init || needsSync(newText)) {
            this.lastText = newText;
            syncToClient(0, buffer -> {
                NetworkUtils.writeStringSafe(buffer, newText.getRawText());
                buffer.writeVarIntToBuffer(newText.getColor());
                NetworkUtils.writeStringSafe(buffer, newText.getFormatting());
                buffer.writeBoolean(newText.hasShadow());
            });
        }
    }

    @Override
    public void markForUpdate() {}

    @Override
    public void unMarkForUpdate() {}

    @Override
    public boolean isMarkedForUpdate() {
        // assume update was handled somewhere else
        return false;
    }

    private boolean needsSync(Text newText) {
        if (lastText == null && newText == null) return false;
        else if (lastText == null) return false;
        return !lastText.getRawText().equals(newText.getRawText())
                || lastText.getColor() != newText.getColor()
                || !lastText.getFormatting().equals(newText.getFormatting());
    }
}
