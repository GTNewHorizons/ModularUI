package com.gtnewhorizons.modularui.common.widget;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.math.CrossAxisAlignment;
import com.gtnewhorizons.modularui.api.math.MainAxisAlignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;

public class Column extends MultiChildWidget implements IWidgetBuilder<Column> {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;
    private int maxHeight = -1, maxWidth = 0;
    private Integer space;

    protected boolean skipDisabledChild = false;

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (maa == MainAxisAlignment.START) {
            return getSizeOf(children);
        }
        return new Size(this.maxWidth, this.maxHeight);
    }

    @Override
    public void layoutChildren(int maxWidthC, int maxHeightC) {
        if (maxHeight < 0 && maa != MainAxisAlignment.START) {
            if (isAutoSized()) {
                maxHeight = maxHeightC - getPos().x;
            } else {
                maxHeight = getSize().height;
            }
        }

        this.maxWidth = 0;
        int totalHeight = 0;

        for (Widget widget : getChildren()) {
            if (skipDisabledChild && !widget.isEnabled()) continue;
            totalHeight += widget.getSize().height;
            maxWidth = Math.max(maxWidth, widget.getSize().width);
        }

        int lastY = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastY = (int) (maxHeight / 2f - totalHeight / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastY = maxHeight - totalHeight;
        }

        for (Widget widget : getChildren()) {
            if (skipDisabledChild && !widget.isEnabled()) continue;
            int x = widget.getPos().x;
            if (caa == CrossAxisAlignment.CENTER) {
                x = (int) (maxWidth / 2f - widget.getSize().width / 2f);
            } else if (caa == CrossAxisAlignment.END) {
                x = maxWidth - widget.getSize().width;
            }
            widget.setPosSilent(new Pos2d(x, lastY));
            lastY += widget.getSize().height;
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                if (space != null) {
                    lastY += space;
                } else {
                    lastY += (maxHeight - totalHeight) / (getChildren().size() - 1);
                }
            }
        }
    }

    public Column setAlignment(MainAxisAlignment maa) {
        return setAlignment(maa, caa);
    }

    public Column setAlignment(CrossAxisAlignment caa) {
        return setAlignment(maa, caa);
    }

    public Column setAlignment(MainAxisAlignment maa, CrossAxisAlignment caa) {
        this.maa = maa;
        this.caa = caa;
        return this;
    }

    public Column setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * Specifies space between children. Use together with {@link MainAxisAlignment#SPACE_BETWEEN}. If you don't call
     * this, this widget will automatically calculate space based on {@link #maxHeight}.
     */
    public Column setSpace(int space) {
        this.space = space;
        return this;
    }
}
