package com.gtnewhorizons.modularui.common.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.IFluidTank;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.fluids.FluidTankLongDelegate;
import com.gtnewhorizons.modularui.api.fluids.FluidTanksHandler;
import com.gtnewhorizons.modularui.api.fluids.IFluidTanksHandler;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.PlayerMainInvWrapper;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

/**
 * Parent widget that contains multiple slots, like items or fluids. Pass number of slots per row, and it will
 * automatically place slots.
 */
public class SlotGroup extends MultiChildWidget {

    public static final int PLAYER_INVENTORY_HEIGHT = 76;

    public static SlotGroup playerInventoryGroup(EntityPlayer player) {
        return playerInventoryGroup(player, null);
    }

    public static SlotGroup playerInventoryGroup(EntityPlayer player, IDrawable background) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
                        .setPos(new Pos2d(col * 18, row * 18));
                slotGroup.addSlot(slot);
                if (background != null) {
                    slot.setBackground(background);
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, i)).setPos(new Pos2d(i * 18, 58));
            slotGroup.addSlot(slot);
            if (background != null) {
                slot.setBackground(background);
            }
        }
        return slotGroup;
    }

    /**
     * Create SlotGroup from ItemHandler. You need to call {@link ItemGroupBuilder#build()} to retrieve actual widget.
     */
    public static ItemGroupBuilder ofItemHandler(IItemHandlerModifiable itemHandler, int slotsPerRow) {
        return new ItemGroupBuilder(itemHandler, slotsPerRow);
    }

    /**
     * Create SlotGroup from FluidTanks. You need to call {@link FluidGroupBuilder#build()} to retrieve actual widget.
     */
    public static FluidGroupBuilder ofFluidTanks(List<IFluidTank> fluidTanks, int slotsPerRow) {
        return new FluidGroupBuilder(fluidTanks, slotsPerRow);
    }

    public SlotGroup setOnDragAndDropComplete(Consumer<Widget> onDragAndDropComplete) {
        for (Widget child : children) {
            if (child instanceof SlotWidget) {
                ((SlotWidget) child).setOnDragAndDropComplete(onDragAndDropComplete);
            }
            if (child instanceof FluidSlotWidget) {
                ((FluidSlotWidget) child).setOnDragAndDropComplete(onDragAndDropComplete);
            }
        }
        return this;
    }

    @Override
    public void onInit() {}

    public SlotGroup addSlot(SlotWidget slotWidget) {
        addChild(slotWidget);
        return this;
    }

    /**
     * Builder for group of items.
     */
    public static class ItemGroupBuilder {

        private final IItemHandlerModifiable itemHandler;
        private final int slotsPerRow;
        private Integer startFromSlot;
        private Integer endAtSlot;
        private Integer shiftClickPriority;
        private boolean canInsert = true;
        private boolean canTake = true;
        private boolean phantom = false;
        private IDrawable[] background;
        private Function<Integer, BaseSlot> slotCreator;
        private Consumer<SlotWidget> applyForWidget;
        private Function<BaseSlot, SlotWidget> widgetCreator;

        private ItemGroupBuilder(IItemHandlerModifiable itemHandler, int slotsPerRow) {
            this.itemHandler = itemHandler;
            this.slotsPerRow = Math.max(slotsPerRow, 1);
        }

        public SlotGroup build() {
            if (startFromSlot == null) {
                startFromSlot = 0;
            }
            if (endAtSlot == null) {
                endAtSlot = itemHandler.getSlots() - 1;
            }

            SlotGroup slotGroup = new SlotGroup();
            if (startFromSlot > endAtSlot) {
                return slotGroup;
            }
            int x = 0, y = 0;
            for (int i = startFromSlot; i < endAtSlot + 1; i++) {
                BaseSlot baseSlot;
                if (slotCreator != null) {
                    baseSlot = slotCreator.apply(i);
                } else {
                    baseSlot = new BaseSlot(itemHandler, i, phantom);
                }
                baseSlot.setAccess(canTake, canInsert);
                if (shiftClickPriority != null) {
                    baseSlot.setShiftClickPriority(shiftClickPriority);
                }

                SlotWidget toAdd;
                if (widgetCreator != null) {
                    toAdd = widgetCreator.apply(baseSlot);
                } else {
                    toAdd = new SlotWidget(baseSlot);
                }
                toAdd.setBackground(background).setPos(x * 18, y * 18);
                if (applyForWidget != null) {
                    applyForWidget.accept(toAdd);
                }

                slotGroup.addSlot(toAdd);
                if (++x == slotsPerRow) {
                    x = 0;
                    y++;
                }
            }
            return slotGroup;
        }

        public ItemGroupBuilder startFromSlot(int startFromSlot) {
            this.startFromSlot = Math.max(startFromSlot, 0);
            return this;
        }

        public ItemGroupBuilder endAtSlot(int endAtSlot) {
            this.endAtSlot = Math.min(endAtSlot, itemHandler.getSlots() - 1);
            return this;
        }

        public ItemGroupBuilder shiftClickPriority(int shiftClickPriority) {
            this.shiftClickPriority = shiftClickPriority;
            return this;
        }

        public ItemGroupBuilder canInsert(boolean canInsert) {
            this.canInsert = canInsert;
            return this;
        }

        public ItemGroupBuilder canTake(boolean canTake) {
            this.canTake = canTake;
            return this;
        }

        public ItemGroupBuilder phantom(boolean phantom) {
            this.phantom = phantom;
            return this;
        }

        public ItemGroupBuilder background(IDrawable... background) {
            this.background = background;
            return this;
        }

        public ItemGroupBuilder slotCreator(Function<Integer, BaseSlot> slotCreator) {
            this.slotCreator = slotCreator;
            return this;
        }

        /**
         * Apply arbitrary lambda for widget.
         */
        public ItemGroupBuilder applyForWidget(Consumer<SlotWidget> consumer) {
            this.applyForWidget = consumer;
            return this;
        }

        /**
         * You can create custom SlotWidget with anonymous class etc.
         */
        public ItemGroupBuilder widgetCreator(Function<BaseSlot, SlotWidget> widgetCreator) {
            this.widgetCreator = widgetCreator;
            return this;
        }
    }

    /**
     * Builder for group of fluids.
     */
    public static class FluidGroupBuilder {

        private final List<IFluidTank> fluidTanks;
        private final int slotsPerRow;
        private Integer startFromSlot;
        private Integer endAtSlot;
        private Boolean phantom;
        private Boolean controlsAmount;
        private IDrawable[] background;
        private Function<IFluidTank, IFluidTanksHandler> tankHandlerCreator;
        private BiFunction<Integer, IFluidTanksHandler, FluidSlotWidget> widgetCreator;

        private FluidGroupBuilder(List<IFluidTank> fluidTanks, int slotsPerRow) {
            this.fluidTanks = fluidTanks;
            this.slotsPerRow = slotsPerRow;
        }

        public SlotGroup build() {
            if (startFromSlot == null) {
                startFromSlot = 0;
            }
            if (endAtSlot == null) {
                endAtSlot = fluidTanks.size() - 1;
            }
            if (phantom == null) {
                phantom = false;
            }
            if (controlsAmount == null) {
                controlsAmount = true;
            }
            if (tankHandlerCreator == null) {
                tankHandlerCreator = tank -> new FluidTanksHandler(new FluidTankLongDelegate(tank));
            }
            if (widgetCreator == null) {
                widgetCreator = (i, h) -> new FluidSlotWidget(h);
            }

            SlotGroup slotGroup = new SlotGroup();
            if (startFromSlot > endAtSlot) {
                return slotGroup;
            }
            int x = 0, y = 0;
            for (int i = startFromSlot; i < endAtSlot + 1; i++) {
                FluidSlotWidget toAdd = widgetCreator.apply(i, tankHandlerCreator.apply(fluidTanks.get(i)));
                toAdd.setPhantom(phantom);
                toAdd.setControlsAmount(controlsAmount, false);
                toAdd.setBackground(background).setPos(new Pos2d(x * 18, y * 18));
                slotGroup.addChild(toAdd);
                if (++x == slotsPerRow) {
                    x = 0;
                    y++;
                }
            }
            return slotGroup;
        }

        public FluidGroupBuilder startFromSlot(int startFromSlot) {
            this.startFromSlot = Math.max(startFromSlot, 0);
            return this;
        }

        public FluidGroupBuilder endAtSlot(int endAtSlot) {
            this.endAtSlot = Math.min(endAtSlot, fluidTanks.size() - 1);
            return this;
        }

        public FluidGroupBuilder phantom(boolean phantom) {
            this.phantom = phantom;
            return this;
        }

        public FluidGroupBuilder controlsAmount(boolean controlsAmount) {
            this.controlsAmount = controlsAmount;
            return this;
        }

        public FluidGroupBuilder background(IDrawable... background) {
            this.background = background;
            return this;
        }

        public FluidGroupBuilder tankHandlerCreator(Function<IFluidTank, IFluidTanksHandler> tankHandlerCreator) {
            this.tankHandlerCreator = tankHandlerCreator;
            return this;
        }

        public FluidGroupBuilder widgetCreator(BiFunction<Integer, IFluidTanksHandler, FluidSlotWidget> widgetCreator) {
            this.widgetCreator = widgetCreator;
            return this;
        }
    }

    /**
     * Builder but allows more complex slot placement.
     */
    public static class BuilderWithPattern {

        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Function<Integer, Widget>> widgetCreatorMap = new HashMap<>();
        private Size cellSize = new Size(18, 18);
        private Size totalSize;
        private Alignment alignment = Alignment.TopLeft;

        public BuilderWithPattern setCellSize(Size cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        public BuilderWithPattern setSize(Size totalSize, Alignment contentAlignment) {
            this.totalSize = totalSize;
            this.alignment = contentAlignment;
            return this;
        }

        public BuilderWithPattern setSize(Size totalSize) {
            return setSize(totalSize, this.alignment);
        }

        public BuilderWithPattern row(String row) {
            this.rows.add(row);
            return this;
        }

        public BuilderWithPattern where(char c, Function<Integer, Widget> widgetCreator) {
            this.widgetCreatorMap.put(c, widgetCreator);
            return this;
        }

        public BuilderWithPattern where(char c, IItemHandlerModifiable inventory) {
            this.widgetCreatorMap.put(c, i -> new SlotWidget(inventory, i));
            return this;
        }

        public BuilderWithPattern where(char c, IFluidTank[] inventory) {
            this.widgetCreatorMap.put(c, i -> new FluidSlotWidget(inventory[i]));
            return this;
        }

        public BuilderWithPattern where(char c, List<IFluidTank> inventory) {
            this.widgetCreatorMap.put(c, i -> new FluidSlotWidget(inventory.get(i)));
            return this;
        }

        public SlotGroup build() {
            int maxRowWith = 0;
            for (String row : rows) {
                maxRowWith = Math.max(maxRowWith, row.length());
            }
            Size contentSize = new Size(maxRowWith * cellSize.width, rows.size() * cellSize.height);
            Pos2d offsetPos = Pos2d.ZERO;
            if (totalSize != null) {
                offsetPos = alignment.getAlignedPos(totalSize, contentSize);
            }
            Map<Character, AtomicInteger> charCount = new HashMap<>();
            SlotGroup slotGroup = new SlotGroup();

            for (int i = 0; i < rows.size(); i++) {
                String row = rows.get(i);
                for (int j = 0; j < row.length(); j++) {
                    char c = row.charAt(j);
                    if (c == ' ') {
                        continue;
                    }
                    Function<Integer, Widget> widgetCreator = this.widgetCreatorMap.get(c);
                    if (widgetCreator == null) {
                        ModularUI.logger.warn("Key {} was not found in Slot group.", c);
                        continue;
                    }
                    Widget widget = widgetCreator
                            .apply(charCount.computeIfAbsent(c, key -> new AtomicInteger()).getAndIncrement());
                    if (widget != null) {
                        slotGroup.addChild(widget.setPos(offsetPos.add(j * cellSize.width, i * cellSize.height)));
                    }
                }
            }
            return slotGroup;
        }
    }
}
