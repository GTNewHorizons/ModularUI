package com.gtnewhorizons.modularui.test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.FluidDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.CrossAxisAlignment;
import com.gtnewhorizons.modularui.api.math.MainAxisAlignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.ChangeableWidget;
import com.gtnewhorizons.modularui.common.widget.Column;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.DropDownWidget;
import com.gtnewhorizons.modularui.common.widget.ExpandTab;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.ProgressBar;
import com.gtnewhorizons.modularui.common.widget.Row;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SliderWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SortableListWidget;
import com.gtnewhorizons.modularui.common.widget.TabButton;
import com.gtnewhorizons.modularui.common.widget.TabContainer;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.VanillaButtonWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

public class TestTile extends TileEntity implements ITileWithModularUI {

    private int serverValue = 0;
    private final FluidTank fluidTank1 = new FluidTank(10000);
    private final FluidTank fluidTank2 = new FluidTank(Integer.MAX_VALUE);
    private final ItemStackHandler phantomInventory = new ItemStackHandler(2) {

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(9);
    private String textFieldValue = "";
    private final int duration = 60;
    private int progress = 0;
    private int ticks = 0;
    private float sliderValue = 0;
    private long longValue = 50;
    private double doubleValue;
    private int serverCounter = 0;
    private static final AdaptableUITexture DISPLAY = AdaptableUITexture
            .of("modularui:gui/background/display", 143, 75, 2);
    private static final AdaptableUITexture BACKGROUND = AdaptableUITexture
            .of("modularui:gui/background/background", 176, 166, 3);
    @SuppressWarnings("unused")
    private static final UITexture PROGRESS_BAR = UITexture.fullImage("modularui", "gui/widgets/progress_bar_arrow");
    private static final UITexture PROGRESS_BAR_MIXER = UITexture
            .fullImage("modularui", "gui/widgets/progress_bar_mixer");

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        phantomInventory.setStackInSlot(1, new ItemStack(Items.diamond, Integer.MAX_VALUE));
        ModularWindow.Builder builder = ModularWindow.builder(new Size(176, 272));
        // .addFromJson("modularui:test", buildContext);
        /*
         * buildContext.applyToWidget("background", DrawableWidget.class, widget -> { widget.
         * addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
         * ) .addTooltip("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
         * .addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet"
         * ); });
         */
        List<Integer> nums = IntStream.range(1, 101).boxed().collect(Collectors.toList());
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND).bindPlayerInventory(buildContext.getPlayer());
        buildContext.addSyncedWindow(1, this::createAnotherWindow);
        return builder
                .widget(
                        new TabContainer().setButtonSize(new Size(28, 32))
                                .addTabButton(
                                        new TabButton(0)
                                                .setBackground(
                                                        false,
                                                        ModularUITextures.VANILLA_TAB_TOP_START
                                                                .getSubArea(0, 0, 1f, 0.5f))
                                                .setBackground(
                                                        true,
                                                        ModularUITextures.VANILLA_TAB_TOP_START
                                                                .getSubArea(0, 0.5f, 1f, 1f))
                                                .setPos(0, -28))
                                .addTabButton(
                                        new TabButton(1)
                                                .setBackground(
                                                        false,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0, 1f, 0.5f))
                                                .setBackground(
                                                        true,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0.5f, 1f, 1f))
                                                .setPos(28, -28))
                                .addTabButton(
                                        new TabButton(2)
                                                .setBackground(
                                                        false,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0, 1f, 0.5f))
                                                .setBackground(
                                                        true,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0.5f, 1f, 1f))
                                                .setPos(56, -28))
                                .addTabButton(
                                        new TabButton(3)
                                                .setBackground(
                                                        false,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0, 1f, 0.5f))
                                                .setBackground(
                                                        true,
                                                        ModularUITextures.VANILLA_TAB_TOP_MIDDLE
                                                                .getSubArea(0, 0.5f, 1f, 1f))
                                                .setPos(84, -28))
                                .addPage(createPage1()).addPage(createPage2()).addPage(createPage3())
                                .addPage(createPage4()))
                .widget(
                        new ExpandTab().setNormalTexture(ModularUITextures.ICON_INFO.withFixedSize(14, 14, 3, 3))
                                .widget(
                                        new DrawableWidget().setDrawable(ModularUITextures.ICON_INFO).setSize(14, 14)
                                                .setPos(3, 3))
                                .widget(
                                        new SortableListWidget<>(nums)
                                                .setWidgetCreator(
                                                        integer -> new TextWidget(integer.toString()).setSize(20, 20)
                                                                .addTooltip(integer.toString()))
                                                .setSize(50, 135).setPos(5, 20))
                                .setExpandedSize(60, 160).setBackground(BACKGROUND).setSize(20, 20).setPos(177, 5)
                                .setRespectNEIArea(true))
                .build();
    }

    private Widget createPage1() {
        ChangeableWidget changeableWidget = new ChangeableWidget(this::dynamicWidget);
        return new MultiChildWidget().addChild(new TextWidget("Page 1"))
                .addChild(new SlotWidget(phantomInventory, 0).setChangeListener(() -> {
                    serverCounter = 0;
                    changeableWidget.notifyChangeServer();
                }).setShiftClickPriority(0).setPos(10, 30)).addChild(
                        new NumericWidget()//
                                .setMinValue(-1_000_000)//
                                .setMaxValue(5_000_000)//
                                .setDefaultValue(50)//
                                .setGetter(() -> (double) longValue)//
                                .setSetter(val -> longValue = val.longValue())//
                                // .setValidator(val -> Math.round(val / 2) * 2d)//
                                .setScrollValues(2, 10, 1000)//
                                .setTextColor(Color.WHITE.dark(1)).setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                                .setSize(92, 20).setPos(10, 50))
                .addChild(
                        new NumericWidget()//
                                .setIntegerOnly(false)//
                                .setMinValue(-1_000_000)//
                                .setMaxValue(5_000_000)//
                                .setDefaultValue(50)//
                                .setGetter(() -> doubleValue)//
                                .setSetter(val -> doubleValue = val)//
                                .setTextColor(Color.WHITE.dark(1)).setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                                .setSize(92, 20).setPos(100, 50))
                .addChild(
                        SlotWidget.phantom(phantomInventory, 1).setShiftClickPriority(1).setIgnoreStackSizeLimit(true)
                                .setControlsAmount(true).setPos(28, 30))
                .addChild(changeableWidget.setPos(12, 55))
                .addChild(SlotGroup.ofItemHandler(items, 3).build().setPos(12, 80))
                .addChild(
                        new DropDownWidget().addDropDownItemsSimple(
                                IntStream.range(0, 20).boxed().map(i -> "label " + i).collect(Collectors.toList()),
                                (buttonWidget, index, label, setSelected) -> buttonWidget
                                        .setOnClick((clickData, widget) -> {
                                            if (!widget.isClient()) {
                                                widget.getContext().getPlayer()
                                                        .addChatMessage(new ChatComponentText("Selected " + label));
                                            }
                                            setSelected.run();
                                        }),
                                true).setExpandedMaxHeight(60).setDirection(DropDownWidget.Direction.DOWN)
                                .setPos(90, 30).setSize(60, 11))
                .addChild(
                        new VanillaButtonWidget()
                                .setDisplayString(StatCollector.translateToLocal("modularui.config.debug"))
                                .setOnClick((clickData, widget) -> {
                                    if (!widget.isClient()) {
                                        widget.getContext().getPlayer().addChatMessage(
                                                new ChatComponentText("Internal Name: " + widget.getInternalName()));
                                    }
                                }).setPos(70, 80).setSize(32, 16).setInternalName("debug"))
                .addChild(
                        new DrawableWidget()
                                .setDrawable(new FluidDrawable().setFluid(FluidRegistry.LAVA).withFixedSize(32, 16))
                                .setPos(70, 100).setSize(32, 16))
                .setPos(10, 10).setDebugLabel("Page1");
    }

    private Widget createPage2() {
        Column column = new Column();
        addInfo(column);
        return new MultiChildWidget()
                .addChild(
                        new TextWidget("Page 2").setPos(10, 10))
                .addChild(column.setPos(7, 19))
                .addChild(
                        new ButtonWidget()
                                .setOnClick(
                                        (clickData, widget) -> {
                                            if (!widget.isClient()) widget.getContext().openSyncedWindow(1);
                                        })
                                .setBackground(ModularUITextures.VANILLA_BACKGROUND, new Text("Window")).setSize(80, 20)
                                .setPos(20, 100))
                .addChild(
                        new ItemDrawable(new ItemStack(Blocks.command_block)).asWidget().setSize(32, 16).setPos(20, 80))
                .addChild(
                        new SliderWidget().setBounds(0, 15).setGetter(() -> sliderValue)
                                .setSetter(val -> sliderValue = val).setSize(120, 20).setPos(7, 130))
                .addChild(
                        TextWidget.dynamicString(() -> String.valueOf((int) (sliderValue + 0.5f)))
                                .setTextAlignment(Alignment.CenterLeft).setSize(30, 20).setPos(135, 130))
                .setDebugLabel("Page2");
    }

    private Widget createPage3() {
        return new MultiChildWidget().addChild(new TextWidget("Page 3")).addChild(
                new CycleButtonWidget().setLength(3).setGetter(() -> serverValue)
                        .setSetter(val -> this.serverValue = val)
                        .setTexture(UITexture.fullImage("modularui", "gui/widgets/cycle_button_demo"))
                        .addTooltip(
                                0,
                                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.")
                        .addTooltip(
                                1,
                                "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                        .addTooltip(
                                2,
                                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet")
                        .setTooltipHasSpaceAfterFirstLine(false).setPos(new Pos2d(68, 0)))
                .addChild(
                        new TextFieldWidget().setGetter(() -> textFieldValue).setSetter(val -> textFieldValue = val)
                                .setTextColor(Color.WHITE.dark(1)).setTextAlignment(Alignment.Center).setScrollBar()
                                .setBackground(DISPLAY.withOffset(-2, -2, 4, 4)).setSize(92, 20).setPos(20, 25))
                .addChild(
                        new ProgressBar().setProgress(() -> progress * 1f / duration)
                                .setDirection(ProgressBar.Direction.LEFT).setTexture(PROGRESS_BAR_MIXER, 20)
                                .setSynced(false, false).setPos(7, 85))
                .addChild(
                        new ProgressBar().setProgress(() -> progress * 1f / duration)
                                .setDirection(ProgressBar.Direction.RIGHT).setTexture(PROGRESS_BAR_MIXER, 20)
                                .setSynced(false, false).setPos(30, 85))
                .addChild(
                        new ProgressBar().setProgress(() -> progress * 1f / duration)
                                .setDirection(ProgressBar.Direction.UP).setTexture(PROGRESS_BAR_MIXER, 20)
                                .setSynced(false, false).setPos(53, 85))
                .addChild(
                        new ProgressBar().setProgress(() -> progress * 1f / duration)
                                .setDirection(ProgressBar.Direction.DOWN).setTexture(PROGRESS_BAR_MIXER, 20)
                                .setSynced(false, false).setPos(76, 85))
                .addChild(
                        new ProgressBar().setProgress(() -> progress * 1f / duration)
                                .setDirection(ProgressBar.Direction.CIRCULAR_CW).setTexture(PROGRESS_BAR_MIXER, 20)
                                .setSynced(false, false).setPos(99, 85))
                .addChild(
                        SlotGroup.ofFluidTanks(Collections.singletonList(fluidTank2), 1).controlsAmount(true)
                                .phantom(true).widgetCreator((slotIndex, h) -> {
                                    FluidSlotWidget widget = new FluidSlotWidget(h);
                                    widget.dynamicTooltip(
                                            () -> Collections.singletonList("Dynamic tooltip " + slotIndex));
                                    return widget;
                                }).build().setPos(38, 47))
                .addChild(new FluidSlotWidget(fluidTank1).setPos(20, 47))
                .addChild(new ButtonWidget().setOnClick((clickData, widget) -> {
                    if (++serverValue == 3) {
                        serverValue = 0;
                    }
                }).setSynced(true, false).setBackground(DISPLAY, new Text("jTest Textg")).setSize(80, 20)
                        .setPos(10, 65))
                .addChild(new TextWidget(new Text("modularui.test").localise()).setPos(10, 110))
                .addChild(
                        new Row().setAlignment(MainAxisAlignment.SPACE_BETWEEN, CrossAxisAlignment.CENTER)
                                .widget(new TextWidget(new Text("Some Text")))
                                .widget(new ButtonWidget().setBackground(DISPLAY))
                                .widget(new TextWidget(new Text("More Text"))).setMaxWidth(156).setPos(0, 130))
                .setPos(10, 10);
    }

    private Widget createPage4() {
        return new MultiChildWidget().addChild(
                new Scrollable().setHorizontalScroll().setVerticalScroll()
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(0, 0))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(20, 20))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(40, 40))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(60, 60))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(80, 80))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(100, 100))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(120, 120))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(140, 140))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(160, 160))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(180, 180))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(200, 200))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(220, 220))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(240, 240))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(260, 260))
                        .widget(ModularUITextures.ICON_INFO.asWidget().setSize(20, 20).setPos(280, 280))
                        .widget(
                                new TextFieldWidget().setGetter(() -> textFieldValue)
                                        .setSetter(val -> textFieldValue = val).setNumbers(val -> val)
                                        .setTextColor(Color.WHITE.dark(1)).setTextAlignment(Alignment.CenterLeft)
                                        .setScrollBar().setBackground(DISPLAY.withOffset(-2, -2, 4, 4)).setSize(92, 20)
                                        .setPos(20, 25))
                        .setSize(156, 150))
                .setPos(10, 10);
    }

    public ModularWindow createAnotherWindow(EntityPlayer player) {
        return ModularWindow.builder(100, 100).setBackground(ModularUITextures.VANILLA_BACKGROUND)
                .widget(ButtonWidget.closeWindowButton(true).setPos(85, 5))
                .widget(SlotWidget.phantom(phantomInventory, 0).setShiftClickPriority(0).setPos(30, 30)).build();
    }

    public <T extends Widget & IWidgetBuilder<T>> void addInfo(T builder) {
        builder.widget(new TextWidget(new Text("Probably a Machine Name").color(0x13610C))).widget(
                new TextWidget("Invalid Structure or whatever")
                        .addTooltip(new Text("This has a tooltip").color(Color.RED.normal)));
        builder.widget(new TextWidget("Maintanance Problems"));
        builder.widget(
                new Row().widget(new TextWidget("Here you can click a button")).widget(
                        new ButtonWidget().setOnClick(((clickData, widget) -> ModularUI.logger.info("Clicked Button")))
                                .setSize(20, 9).setBackground(new Text("[O]"))));
    }

    public Widget dynamicWidget() {
        ItemStack stack = phantomInventory.getStackInSlot(0);
        if (stack == null) {
            return null;
        }
        MultiChildWidget widget = new MultiChildWidget();
        widget.addChild(new TextWidget(new Text("Item: " + stack.getDisplayName()).format(EnumChatFormatting.BLUE)))
                .addChild(
                        new CycleButtonWidget().setGetter(() -> serverCounter).setSetter(value -> serverCounter = value)
                                .setLength(10).setTextureGetter(value -> new Text(value + "")).setPos(5, 11));

        return widget;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Val", serverValue);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.serverValue = nbt.getInteger("Val");
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote) {
            ticks++;
            if (ticks % 20 == 0) {
                if (++serverCounter == 10) {
                    serverCounter = 0;
                }
            }
        } else {
            if (++progress == duration) {
                progress = 0;
            }
        }
    }
}
