package com.gtnewhorizons.modularui.common.widget;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.math.CrossAxisAlignment;
import com.gtnewhorizons.modularui.api.math.MainAxisAlignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class Row extends MultiChildWidget implements IWidgetBuilder<Row> {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;
    private int maxWidth = -1, maxHeight = 0;
    private Integer space;

    protected boolean skipDisabledChild = false;

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (maa == MainAxisAlignment.START) {
            return IWidgetParent.getSizeOf(children);
        }
        return new Size(this.maxWidth, this.maxHeight);
    }

    @Override
    public void layoutChildren(int maxWidthC, int maxHeightC) {
        if (maxWidth < 0 && maa != MainAxisAlignment.START) {
            if (isAutoSized()) {
                maxWidth = maxWidthC - getPos().x;
            } else {
                maxWidth = getSize().width;
            }
        }

        this.maxHeight = 0;
        int totalWidth = 0;

        for (Widget widget : getChildren()) {
            if (skipDisabledChild && !widget.isEnabled()) continue;
            totalWidth += widget.getSize().width;
            maxHeight = Math.max(maxHeight, widget.getSize().height);
        }

        int lastX = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastX = (int) (maxWidth / 2f - totalWidth / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastX = maxWidth - totalWidth;
        }

        for (Widget widget : getChildren()) {
            if (skipDisabledChild && !widget.isEnabled()) continue;
            int y = widget.getPos().y;
            if (caa == CrossAxisAlignment.CENTER) {
                y = (int) (maxHeight / 2f - widget.getSize().height / 2f);
            } else if (caa == CrossAxisAlignment.END) {
                y = maxHeight - widget.getSize().height;
            }
            widget.setPosSilent(new Pos2d(lastX, y));
            lastX += widget.getSize().width;
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                if (space != null) {
                    lastX += space;
                } else {
                    lastX += (maxWidth - totalWidth) / (getChildren().size() - 1);
                }
            }
        }
    }

    public Row setAlignment(MainAxisAlignment maa) {
        return setAlignment(maa, caa);
    }

    public Row setAlignment(CrossAxisAlignment caa) {
        return setAlignment(maa, caa);
    }

    public Row setAlignment(MainAxisAlignment maa, CrossAxisAlignment caa) {
        this.maa = maa;
        this.caa = caa;
        return this;
    }

    public Row setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * Specifies space between children. Use together with {@link MainAxisAlignment#SPACE_BETWEEN}. If you don't call
     * this, this widget will automatically calculate space based on {@link #maxHeight}.
     */
    public Row setSpace(int space) {
        this.space = space;
        return this;
    }
}
