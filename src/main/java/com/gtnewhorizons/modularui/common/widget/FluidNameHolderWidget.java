package com.gtnewhorizons.modularui.common.widget;

import static com.gtnewhorizons.modularui.ModularUI.isGT5ULoaded;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.widget.FluidInteractionUtil;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.IHasStackUnderMouse;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import gregtech.api.util.GT_Utility;

/**
 * Holds fluid name and displays corresponding fluid. Can be useful for setting filter or locking fluid.
 */
@SuppressWarnings("unused")
public class FluidNameHolderWidget extends SyncedWidget
        implements Interactable, IDragAndDropHandler, IHasStackUnderMouse, FluidInteractionUtil {

    public static final Size SIZE = new Size(18, 18);
    private static final int PACKET_SYNC = 1, PACKET_CLICK = 2, PACKET_DND = 3;

    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private String lastFluid;
    private boolean lastShift = false;

    public FluidNameHolderWidget(Supplier<String> getter, Consumer<String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void draw(float partialTicks) {
        FluidStack fluid = createFluidStack();
        if (fluid == null) return;
        Size size = getSize();
        GuiHelper.drawFluidTexture(new FluidStack(fluid, 0), 0, 0, size.width, size.height, 0);
    }

    @Override
    public void buildTooltip(List<Text> tooltip) {
        super.buildTooltip(tooltip);
        FluidStack fluid = createFluidStack();
        if (fluid != null) {
            addFluidNameInfo(tooltip, fluid);
            addAdditionalFluidInfo(tooltip, fluid);
            tooltip.add(Text.localised("modularui.phantom.single.clear"));
            if (!Interactable.hasShiftDown()) {
                tooltip.add(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
                tooltip.add(Text.localised("modularui.tooltip.shift"));
            }
        } else {
            tooltip.add(Text.localised("modularui.fluid.none").format(EnumChatFormatting.WHITE));
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return SIZE;
    }

    @Override
    public void onScreenUpdate() {
        if (lastShift != Interactable.hasShiftDown()) {
            lastShift = Interactable.hasShiftDown();
            notifyTooltipChange();
        }
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        ItemStack cursorStack = getContext().getCursor().getItemStack();
        setFluidName(cursorStack);
        syncToServer(PACKET_CLICK, buffer -> {});
        return ClickResult.ACCEPT;
    }

    @Override
    public boolean handleDragAndDrop(ItemStack draggedStack, int button) {
        setFluidName(draggedStack);
        syncToServer(PACKET_DND, buffer -> {
            try {
                buffer.writeItemStackToBuffer(draggedStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        draggedStack.stackSize = 0;
        return true;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == PACKET_SYNC) {
            setter.accept(NetworkUtils.readStringSafe(buf));
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == PACKET_CLICK) {
            setFluidName(getContext().getCursor().getItemStack());
        } else if (id == PACKET_DND) {
            setFluidName(buf.readItemStackFromBuffer());
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        String newFluid = getter.get();
        if (init || !Objects.equals(lastFluid, newFluid)) {
            lastFluid = newFluid;
            syncToClient(PACKET_SYNC, buffer -> NetworkUtils.writeStringSafe(buffer, lastFluid));
        }
    }

    private void setFluidName(ItemStack stack) {
        if (stack == null) {
            setter.accept(null);
        } else {
            FluidStack fluid = getFluidForPhantomItem(stack);
            if (fluid == null || fluid.getFluid() == null) return;
            setter.accept(fluid.getFluid().getName());
        }
        notifyTooltipChange();
    }

    protected FluidStack createFluidStack() {
        String fluidName = getter.get();
        if (fluidName == null) return null;
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) return null;
        return new FluidStack(fluid, 0);
    }

    @Override
    public ItemStack getStackUnderMouse() {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidDisplayStack(createFluidStack(), false);
        }
        return null;
    }
}
