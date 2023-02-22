package com.gtnewhorizons.modularui.common.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.network.PacketBuffer;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.shapes.Rectangle;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class DropDownWidget extends ExpandTab implements ISyncedWidget {

    private final ListWidget listContainer;
    private final DrawableWidget selectedWidget;
    private final DrawableWidget arrowWidget;

    private Direction direction = Direction.DOWN;
    private int expandedMaxHeight = 60;
    private final List<IDrawable> labels = new ArrayList<>();
    private int selected = -1;
    private IDrawable textUnselected = Text.localised("modularui.dropdown.select").withOffset(0, 1);

    private boolean syncsToServer = true;
    private boolean syncsToClient = true;
    private boolean needsUpdate;

    private static final IDrawable TEXTURE_ARROW_UP = ModularUITextures.ARROW_GRAY_UP.withFixedSize(10, 10);
    private static final IDrawable TEXTURE_ARROW_DOWN = ModularUITextures.ARROW_GRAY_DOWN.withFixedSize(10, 10);

    public DropDownWidget() {
        setAnimateDuration(150);
        setSize(60, 10);
        setBackground(ModularUITextures.BACKGROUND_BORDER_1PX);

        listContainer = new ListWidget();
        addChild(
                listContainer.setMaxHeight(expandedMaxHeight).setPosProvider(
                        (screenSize, window,
                                parent) -> new Pos2d(0, direction == Direction.DOWN ? normalSize.height : 0)));

        selectedWidget = new DrawableWidget();
        addChild(selectedWidget.setDrawable(() -> {
            if (selected > -1 && selected < labels.size()) {
                return labels.get(selected);
            }
            return textUnselected;
        }).setSizeProvider((screenSize, window, parent) -> normalSize).setPosProvider(
                (screenSize, window, parent) -> new Pos2d(
                        0,
                        (direction == Direction.UP && isExpanded()) ? getActualExpandedHeight() : 0)));

        arrowWidget = new DrawableWidget();
        addChild(
                arrowWidget.setDrawable(() -> getArrowTexture(isExpanded())).setSize(10, 10).setPosProvider(
                        (screenSize, window, parent) -> new Pos2d(
                                getSize().width - 10,
                                (direction == Direction.UP && isExpanded()) ? getActualExpandedHeight() : 0)));
    }

    @Override
    public void onRebuild() {
        super.onRebuild();
        setExpandedSizeAndPos();
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        ClickResult clickResult = super.onClick(buttonId, doubleClick);
        if (clickResult == ClickResult.ACCEPT) {
            Interactable.playButtonClickSound();
        }
        return clickResult;
    }

    @Override
    public boolean shouldDrawChildWidgetWhenCollapsed(Widget child) {
        return child == selectedWidget || child == arrowWidget;
    }

    protected int getActualExpandedHeight() {
        return Math.min(expandedMaxHeight, getChildrenHeight());
    }

    protected void setExpandedSizeAndPos() {
        int expandedHeight = getActualExpandedHeight();
        switch (direction) {
            case UP:
                this.expandedSize = this.normalSize.grow(0, expandedHeight);
                this.expandedPos = this.normalPos.add(0, -expandedHeight);
                break;
            case DOWN:
                this.expandedSize = this.normalSize.grow(0, expandedHeight);
                break;
        }
    }

    protected int getChildrenHeight() {
        int totalHeight = 0;
        for (Widget child : listContainer.getUnnestedChildren()) {
            totalHeight += child.getSize().height;
        }
        return totalHeight;
    }

    protected IDrawable getArrowTexture(boolean flipped) {
        if (flipped) {
            switch (direction) {
                case UP:
                    return TEXTURE_ARROW_DOWN;
                case DOWN:
                    return TEXTURE_ARROW_UP;
                default:
                    throw new RuntimeException("Unknown direction: " + direction);
            }
        } else {
            switch (direction) {
                case UP:
                    return TEXTURE_ARROW_UP;
                case DOWN:
                    return TEXTURE_ARROW_DOWN;
                default:
                    throw new RuntimeException("Unknown direction: " + direction);
            }
        }
    }

    public DropDownWidget addDropDownItem(IDrawable label, Widget widget) {
        labels.add(label);
        listContainer.addChild(widget);
        return this;
    }

    public DropDownWidget addDropDownItem(String label, Widget widget) {
        return addDropDownItem(new Text(label).withOffset(0, 1), widget);
    }

    public DropDownWidget addDropDownItems(List<String> labels, List<Widget> widgets) {
        assert labels.size() == widgets.size();
        for (int i = 0; i < labels.size(); i++) {
            addDropDownItem(labels.get(i), widgets.get(i));
        }
        return this;
    }

    public DropDownWidget addDropDownItems(List<String> labels, BiFunction<Integer, String, Widget> widgetCreator) {
        for (int i = 0; i < labels.size(); i++) {
            addDropDownItem(labels.get(i), widgetCreator.apply(i, labels.get(i)));
        }
        return this;
    }

    public DropDownWidget addDropDownItemsSimple(List<String> labels, ApplyForEachButton applyForEachButton,
            boolean collapseOnSelect) {
        return addDropDownItems(labels, (index, label) -> {
            IDrawable text = new Text(label).withOffset(0, 1);
            ButtonWidget buttonWidget = new ButtonWidget();
            buttonWidget.setHoveredBackground(new Rectangle().setColor(Color.LIGHT_BLUE.bright(2)), text)
                    .setBackground(text).setSizeProvider((screenSize, window, parent) -> normalSize);
            applyForEachButton.apply(buttonWidget, index, label, () -> {
                setSelected(index);
                if (collapseOnSelect) {
                    setExpanded(false);
                }
            });
            return buttonWidget;
        });
    }

    public int getSelected() {
        return selected;
    }

    public List<IDrawable> getLabels() {
        return labels;
    }

    public ListWidget getListContainer() {
        return listContainer;
    }

    public boolean syncsToClient() {
        return syncsToClient;
    }

    public boolean syncsToServer() {
        return syncsToServer;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            setSelected(buf.readVarIntFromBuffer(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 1) {
            setSelected(buf.readVarIntFromBuffer(), false);
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient()) {
            if (init) {
                setSelected(selected, true);
                markForUpdate();
            }
        }
    }

    public DropDownWidget setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public DropDownWidget setExpandedMaxHeight(int expandedMaxHeight) {
        this.expandedMaxHeight = expandedMaxHeight;
        listContainer.setMaxHeight(expandedMaxHeight);
        return this;
    }

    public DropDownWidget setSelected(int selected) {
        return setSelected(selected, false);
    }

    public DropDownWidget setSelected(int selected, boolean sync) {
        this.selected = selected;
        if (sync) {
            if (isClient()) {
                if (syncsToServer()) {
                    syncToServer(1, buffer -> buffer.writeVarIntToBuffer(selected));
                }
            } else {
                if (syncsToClient()) {
                    syncToClient(1, buffer -> buffer.writeVarIntToBuffer(selected));
                }
            }
        }
        return this;
    }

    public DropDownWidget setTextUnselected(IDrawable textUnselected) {
        this.textUnselected = textUnselected;
        return this;
    }

    public DropDownWidget setTextUnselected(String textUnselected) {
        return setTextUnselected(new Text(textUnselected).withOffset(0, 1));
    }

    public DropDownWidget setLabel(int index, IDrawable label) {
        labels.set(index, label);
        Widget child = listContainer.children.get(index);
        child.setBackground(label);
        if (child instanceof ButtonWidget) {
            ((ButtonWidget) child).setHoveredBackground(new Rectangle().setColor(Color.LIGHT_BLUE.bright(2)), label);
        }
        return this;
    }

    public DropDownWidget setLabel(int index, String label) {
        return setLabel(index, new Text(label));
    }

    public DropDownWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
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

    public enum Direction {
        UP,
        DOWN,
    }

    @FunctionalInterface
    public interface ApplyForEachButton {

        void apply(ButtonWidget buttonWidget, int index, String label, Runnable setSelected);
    }
}
