package com.gtnewhorizons.modularui.common.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.PacketBuffer;

import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class DynamicPositionedRow extends Row implements ISyncedWidget {

    private final List<Boolean> childrenEnabledFlags = new ArrayList<>();

    private boolean needsUpdate;
    private boolean syncsToClient = true;

    public DynamicPositionedRow() {
        this.skipDisabledChild = true;
    }

    @Override
    public void initChildren() {
        for (Widget child : getChildren()) {
            childrenEnabledFlags.add(child.isEnabled());
        }
    }

    @Override
    public void onScreenUpdate() {
        boolean needsReposition = false;
        for (int i = 0; i < getChildren().size(); i++) {
            if (childrenEnabledFlags.get(i) != getChildren().get(i).isEnabled()) {
                needsReposition = true;
                childrenEnabledFlags.set(i, getChildren().get(i).isEnabled());
            }
        }
        if (needsReposition) {
            layoutChildren(getWindow().getSize().width, getWindow().getSize().height);
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient) {
            boolean needsReposition = false;
            for (int i = 0; i < getChildren().size(); i++) {
                if (childrenEnabledFlags.get(i) != getChildren().get(i).isEnabled()) {
                    needsReposition = true;
                    childrenEnabledFlags.set(i, getChildren().get(i).isEnabled());
                }
            }
            if (init || needsReposition) {
                syncToClient(0, buffer -> {
                    buffer.writeVarIntToBuffer(getChildren().size());
                    for (boolean flag : childrenEnabledFlags) {
                        buffer.writeBoolean(flag);
                    }
                });
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            int length = buf.readVarIntFromBuffer();
            for (int i = 0; i < length && i < childrenEnabledFlags.size(); i++) {
                boolean flag = buf.readBoolean();
                childrenEnabledFlags.set(i, flag);
                getChildren().get(i).setEnabled(flag);
            }
            layoutChildren(getWindow().getSize().width, getWindow().getSize().height);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}

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

    public DynamicPositionedRow setSynced(boolean syncsToClient) {
        this.syncsToClient = syncsToClient;
        return this;
    }
}
