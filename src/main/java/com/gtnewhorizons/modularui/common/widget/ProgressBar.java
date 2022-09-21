package com.gtnewhorizons.modularui.common.widget;

import codechicken.lib.math.MathHelper;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.ITransferRectHandler;
import com.gtnewhorizons.modularui.config.Config;
import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StatCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProgressBar extends SyncedWidget implements ITransferRectHandler {

    private Supplier<Float> progress;
    private UITexture emptyTexture;
    private final UITexture[] fullTexture = new UITexture[4];
    private Direction direction = Direction.RIGHT;
    private int imageSize = -1;

    private String transferRectID;
    private String transferRectTooltip;
    private Object[] transferRectArgs;

    // vanilla furnace sends packet every tick during 200 ticks in the process
    private float packetThreshold = 0.005f;

    private float lastProgress;

    @Override
    public void onInit() {
        if (direction == Direction.CIRCULAR_CW && fullTexture[0] != null) {
            UITexture base = fullTexture[0];
            fullTexture[0] = base.getSubArea(0f, 0.5f, 0.5f, 1f);
            fullTexture[1] = base.getSubArea(0f, 0f, 0.5f, 0.5f);
            fullTexture[2] = base.getSubArea(0.5f, 0f, 1f, 0.5f);
            fullTexture[3] = base.getSubArea(0.5f, 0.5f, 1f, 1f);
        }
    }

    @Override
    public void onRebuild() {
        if (imageSize < 0) {
            imageSize = size.width;
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (emptyTexture != null) {
            emptyTexture.draw(Pos2d.ZERO, getSize(), partialTicks);
        }
        float progress = syncsToClient() ? lastProgress : this.progress.get();
        if (fullTexture[0] != null && progress > 0) {
            if (direction == Direction.CIRCULAR_CW) {
                drawCircular(progress);
                return;
            }
            if (progress >= 1) {
                fullTexture[0].draw(Pos2d.ZERO, getSize(), partialTicks);
            } else {
                progress = getProgressUV(progress);
                float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
                float x = 0, y = 0, width = size.width, height = size.height;
                switch (direction) {
                    case RIGHT:
                        u1 = progress;
                        width *= progress;
                        break;
                    case LEFT:
                        u0 = 1 - progress;
                        width *= progress;
                        x = size.width - width;
                        break;
                    case DOWN:
                        v1 = progress;
                        height *= progress;
                        break;
                    case UP:
                        v0 = 1 - progress;
                        height *= progress;
                        y = size.height - height;
                        break;
                }
                fullTexture[0].drawSubArea(x, y, width, height, u0, v0, u1, v1);
            }
        }
    }

    public float getProgressUV(float uv) {
        if (Config.smoothProgressbar) {
            return uv;
        }
        return (float) (Math.floor(uv * imageSize) / imageSize);
    }

    private void drawCircular(float progress) {
        float[] subAreas = {
            getProgressUV((float) MathHelper.clip(progress / 0.25f, 0, 1)),
            getProgressUV((float) MathHelper.clip((progress - 0.25f) / 0.25f, 0, 1)),
            getProgressUV((float) MathHelper.clip((progress - 0.5f) / 0.25f, 0, 1)),
            getProgressUV((float) MathHelper.clip((progress - 0.75f) / 0.25f, 0, 1))
        };
        float halfWidth = size.width / 2f;
        float halfHeight = size.height / 2f;

        float progressScaled = subAreas[0] * halfHeight;
        fullTexture[0].drawSubArea(
                0,
                size.height - progressScaled,
                halfWidth,
                progressScaled,
                0.0f,
                1.0f - progressScaled / halfHeight,
                1.0f,
                1.0f); // BL, draw UP

        progressScaled = subAreas[1] * halfWidth;
        fullTexture[1].drawSubArea(
                0, 0, progressScaled, halfHeight, 0.0f, 0.0f, progressScaled / (halfWidth), 1.0f); // TL, draw RIGHT

        progressScaled = subAreas[2] * halfHeight;
        fullTexture[2].drawSubArea(
                halfWidth,
                0,
                halfWidth,
                progressScaled,
                0.0f,
                0.0f,
                1.0f,
                progressScaled / halfHeight); // TR, draw DOWN

        progressScaled = subAreas[3] * halfWidth;
        fullTexture[3].drawSubArea(
                size.width - progressScaled,
                halfHeight,
                progressScaled,
                halfHeight,
                1.0f - progressScaled / halfWidth,
                0.0f,
                1.0f,
                1.0f); // BR, draw LEFT
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(20, 20);
    }

    @Nullable
    @Override
    public String getNEITransferRectID() {
        return transferRectID;
    }

    @Override
    public String getNEITransferRectTooltip() {
        return transferRectTooltip;
    }

    @Override
    public Object[] getNEITransferRectArgs() {
        return transferRectArgs;
    }

    @Override
    public boolean syncsToServer() {
        return false;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            lastProgress = buf.readFloat();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}

    @Override
    public void detectAndSendChanges(boolean init) {
        if (!syncsToClient()) return;
        float newProgress = progress.get();
        if (init || Math.abs(newProgress - lastProgress) > packetThreshold) {
            lastProgress = newProgress;
            syncToClient(0, buffer -> buffer.writeFloat(lastProgress));
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

    public ProgressBar setProgress(Supplier<Float> progress) {
        this.progress = progress;
        return this;
    }

    public ProgressBar setProgress(float progress) {
        this.progress = () -> progress;
        return this;
    }

    /**
     * Sets the texture to render
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     * @param imageSize    image size in direction of progress. used for non smooth rendering
     */
    public ProgressBar setTexture(UITexture emptyTexture, UITexture fullTexture, int imageSize) {
        this.emptyTexture = emptyTexture;
        this.fullTexture[0] = fullTexture;
        this.imageSize = imageSize;
        return this;
    }

    /**
     * @param texture a texture where the empty and full bar are stacked on top of each other
     */
    public ProgressBar setTexture(UITexture texture, int imageSize) {
        return setTexture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), imageSize);
    }

    public ProgressBar setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public ProgressBar setNEITransferRect(
            String transferRectID, String transferRectTooltip, Object[] transferRectArgs) {
        this.transferRectID = transferRectID;
        this.transferRectTooltip = transferRectTooltip;
        this.transferRectArgs = transferRectArgs;
        return this;
    }

    public ProgressBar setNEITransferRect(String transferRectID, String transferRectTooltip) {
        return setNEITransferRect(transferRectID, transferRectTooltip, new Object[0]);
    }

    public ProgressBar setNEITransferRect(String transferRectID) {
        return setNEITransferRect(transferRectID, StatCollector.translateToLocal("nei.recipe.tooltip"));
    }

    /**
     * @param threshold Minimum diff required for triggering packet transfer
     */
    public ProgressBar setPacketThreshold(float threshold) {
        this.packetThreshold = threshold;
        return this;
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        CIRCULAR_CW;
    }
}
