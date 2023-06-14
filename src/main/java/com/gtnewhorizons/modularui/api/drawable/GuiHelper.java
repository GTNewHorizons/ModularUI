package com.gtnewhorizons.modularui.api.drawable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.GlStateManager;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHelper {

    private static final Set<Fluid> errorFluids = new HashSet<>();

    // ==== Screen helpers ====

    public static boolean hasScreen() {
        return Minecraft.getMinecraft().currentScreen != null;
    }

    public static GuiScreen getActiveScreen() {
        return Minecraft.getMinecraft().currentScreen;
    }

    /**
     * @return the scaled screen size. (0;0) if no screen is open.
     */
    public static Size getScreenSize() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            return new Size(screen.width, screen.height);
        }
        return Size.ZERO;
    }

    /**
     * @return the current mouse pos. (0;0) if no screen is open.
     */
    public static Pos2d getCurrentMousePos() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            int x = Mouse.getEventX() * screen.width / Minecraft.getMinecraft().displayWidth;
            int y = screen.height - Mouse.getEventY() * screen.height / Minecraft.getMinecraft().displayHeight - 1;
            return new Pos2d(x, y);
        }
        return Pos2d.ZERO;
    }

    // ==== Tooltip helpers ====

    public static void drawHoveringText(List<Text> textLines, Pos2d mousePos, Size screenSize, int maxWidth,
            float scale, boolean forceShadow, Alignment alignment, boolean tooltipHasSpaceAfterFirstLine) {
        if (textLines.isEmpty()) {
            return;
        }
        List<Integer> colors = textLines.stream().map(Text::getColor).collect(Collectors.toList());
        List<String> lines = textLines.stream().map(line -> line.getFormatted()).collect(Collectors.toList());
        drawHoveringTextFormatted(
                lines,
                colors,
                mousePos,
                screenSize,
                maxWidth,
                scale,
                forceShadow,
                alignment,
                tooltipHasSpaceAfterFirstLine);
    }

    public static void drawHoveringTextFormatted(List<String> lines, Pos2d mousePos, Size screenSize, int maxWidth) {
        drawHoveringTextFormatted(
                lines,
                Collections.emptyList(),
                mousePos,
                screenSize,
                maxWidth,
                1f,
                false,
                Alignment.TopLeft,
                true);
    }

    public static void drawHoveringTextFormatted(List<String> lines, List<Integer> colors, Pos2d mousePos,
            Size screenSize, int maxWidth, float scale, boolean forceShadow, Alignment alignment,
            boolean hasSpaceAfterFirstLine) {
        if (lines.isEmpty()) {
            return;
        }
        if (maxWidth < 0) {
            maxWidth = Integer.MAX_VALUE;
        }

        int maxTextWidth = maxWidth;

        boolean mouseOnRightSide = false;
        int screenSpaceRight = screenSize.width - mousePos.x - 16;
        if (mousePos.x > screenSize.width / 2f) {
            mouseOnRightSide = true;
        }
        if (maxTextWidth > screenSpaceRight) {
            maxTextWidth = screenSpaceRight;
        }
        boolean putOnLeft = false;
        int tooltipY = mousePos.y - 12;
        int tooltipX = mousePos.x + 12;
        TextRenderer renderer = new TextRenderer();
        renderer.setPos(mousePos);
        renderer.setAlignment(Alignment.TopLeft, maxTextWidth);
        renderer.setScale(scale);
        renderer.setShadow(forceShadow);
        renderer.setSimulate(true);
        List<Pair<String, Float>> measuredLines = renderer.measureLines(lines);
        if (mouseOnRightSide && measuredLines.size() > lines.size()) {
            putOnLeft = true;
            maxTextWidth = Math.min(maxWidth, mousePos.x - 16);
        }

        renderer.setAlignment(Alignment.TopLeft, maxTextWidth);
        measuredLines = renderer.measureLines(lines);
        renderer.drawMeasuredLines(measuredLines, colors, hasSpaceAfterFirstLine);
        int tooltipTextWidth = (int) renderer.lastWidth;
        int tooltipHeight = (int) renderer.lastHeight;

        if (mouseOnRightSide && putOnLeft) {
            tooltipX += -24 - tooltipTextWidth;
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        final int zLevel = 300;
        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 4,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 3,
                backgroundColor,
                backgroundColor);
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 4,
                backgroundColor,
                backgroundColor);
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
        drawGradientRect(
                zLevel,
                tooltipX - 4,
                tooltipY - 3,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
        drawGradientRect(
                zLevel,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 4,
                tooltipY + tooltipHeight + 3,
                backgroundColor,
                backgroundColor);
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 3 + 1,
                tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1,
                borderColorStart,
                borderColorEnd);
        drawGradientRect(
                zLevel,
                tooltipX + tooltipTextWidth + 2,
                tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3 - 1,
                borderColorStart,
                borderColorEnd);
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1,
                borderColorStart,
                borderColorStart);
        drawGradientRect(
                zLevel,
                tooltipX - 3,
                tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3,
                borderColorEnd,
                borderColorEnd);

        renderer.setSimulate(false);
        renderer.setPos(tooltipX, tooltipY);
        renderer.setAlignment(alignment, maxTextWidth);
        renderer.setColor(0xffffff);
        renderer.drawMeasuredLines(measuredLines, colors, hasSpaceAfterFirstLine);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    // ==== Draw helpers ====

    public static void drawGradientRect(float zLevel, float left, float top, float right, float bottom, int startColor,
            int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(startRed, startGreen, startBlue, startAlpha);
        tessellator.addVertex(right, top, zLevel);
        tessellator.setColorRGBA_F(startRed, startGreen, startBlue, startAlpha);
        tessellator.addVertex(left, top, zLevel);
        tessellator.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
        tessellator.addVertex(left, bottom, zLevel);
        tessellator.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
        tessellator.addVertex(right, bottom, zLevel);
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawFluidTexture(FluidStack content, float x0, float y0, float width, float height, float z) {
        if (content == null) {
            return;
        }
        Fluid fluid = content.getFluid();
        IIcon fluidStill = fluid.getIcon(content);
        int fluidColor = fluid.getColor(content);

        if (fluidStill == null) {
            if (!errorFluids.contains(fluid)) {
                ModularUI.logger.warn("Fluid `{}` does not have icon!", fluid.getName());
                errorFluids.add(fluid);
            }
            return;
        }

        if (ModularUI.isHodgepodgeLoaded && fluidStill instanceof IPatchedTextureAtlasSprite) {
            ((IPatchedTextureAtlasSprite) fluidStill).markNeedsAnimationUpdate();
        }

        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        float u0 = fluidStill.getMinU(), u1 = fluidStill.getMaxU(), v0 = fluidStill.getMinV(),
                v1 = fluidStill.getMaxV();
        float x1 = x0 + width, y1 = y0 + height;
        float r = Color.getRedF(fluidColor), g = Color.getGreenF(fluidColor), b = Color.getBlueF(fluidColor),
                a = Color.getAlphaF(fluidColor);
        a = a == 0f ? 1f : a;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(r, g, b, a);
        tessellator.setTextureUV(u0, v1);
        tessellator.addVertex(x0, y1, z);
        tessellator.setTextureUV(u1, v1);
        tessellator.addVertex(x1, y1, z);
        tessellator.setTextureUV(u1, v0);
        tessellator.addVertex(x1, y0, z);
        tessellator.setTextureUV(u0, v0);
        tessellator.addVertex(x0, y0, z);
        tessellator.draw();
        GlStateManager.disableBlend();
    }

    // ==== Scissor helpers ====

    private static final Stack<int[]> scissorFrameStack = new Stack<>();

    public static void useScissor(int x, int y, int width, int height, Runnable codeBlock) {
        pushScissorFrame(x, y, width, height);
        try {
            codeBlock.run();
        } finally {
            popScissorFrame();
        }
    }

    private static int[] peekFirstScissorOrFullScreen() {
        int[] currentTopFrame = scissorFrameStack.isEmpty() ? null : scissorFrameStack.peek();
        if (currentTopFrame == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            return new int[] { 0, 0, minecraft.displayWidth, minecraft.displayHeight };
        }
        return currentTopFrame;
    }

    public static void pushScissorFrame(int x, int y, int width, int height) {
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];

        boolean pushedFrame = false;
        if (x <= parentX + parentWidth && y <= parentY + parentHeight) {
            int newX = Math.max(x, parentX);
            int newY = Math.max(y, parentY);
            int newWidth = width - (newX - x);
            int newHeight = height - (newY - y);
            if (newWidth > 0 && newHeight > 0) {
                int maxWidth = parentWidth - (x - parentX);
                int maxHeight = parentHeight - (y - parentY);
                newWidth = Math.min(maxWidth, newWidth);
                newHeight = Math.min(maxHeight, newHeight);
                applyScissor(newX, newY, newWidth, newHeight);
                // finally, push applied scissor on top of scissor stack
                if (scissorFrameStack.isEmpty()) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                }
                scissorFrameStack.push(new int[] { newX, newY, newWidth, newHeight });
                pushedFrame = true;
            }
        }
        if (!pushedFrame) {
            if (scissorFrameStack.isEmpty()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            scissorFrameStack.push(new int[] { parentX, parentY, parentWidth, parentHeight });
        }
    }

    public static void popScissorFrame() {
        scissorFrameStack.pop();
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];
        applyScissor(parentX, parentY, parentWidth, parentHeight);
        if (scissorFrameStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    // applies scissor with gui-space coordinates and sizes
    private static void applyScissor(int x, int y, int w, int h) {
        // translate upper-left to bottom-left
        ScaledResolution r = new ScaledResolution(
                Minecraft.getMinecraft(),
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);
        int s = r.getScaleFactor();
        int translatedY = r.getScaledHeight() - y - h;
        GL11.glScissor(x * s, translatedY * s, w * s, h * s);
    }

    public static FontRenderer getFontRenderer(ItemStack item) {
        FontRenderer defaultFont = Minecraft.getMinecraft().fontRenderer;
        if (item == null) return defaultFont;
        // noinspection DataFlowIssue
        FontRenderer font = item.getItem().getFontRenderer(item);
        if (font == null) return defaultFont;
        return font;
    }

    public static void afterRenderItemAndEffectIntoGUI(ItemStack stack) {
        // asked by Forge :shrug:
        // RenderItem#L627
        if (stack.hasEffect(0)) {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    public static List<String> getItemTooltip(ItemStack stack) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof ModularGui)) return Collections.emptyList();
        ModularGui modularGui = (ModularGui) currentScreen;
        ModularUIContext context = modularGui.getContext();
        // noinspection unchecked
        List<String> tooltips = stack.getTooltip(context.getPlayer(), modularGui.mc.gameSettings.advancedItemTooltips);
        for (int i = 0; i < tooltips.size(); i++) {
            if (i == 0) {
                tooltips.set(0, stack.getRarity().rarityColor.toString() + tooltips.get(0));
            } else {
                tooltips.set(i, EnumChatFormatting.GRAY + tooltips.get(i));
            }
        }
        if (tooltips.size() > 0) {
            tooltips.set(0, tooltips.get(0) + GuiDraw.TOOLTIP_LINESPACE); // add space after 'title'
        }
        applyNEITooltipHandler(tooltips, stack, context);
        return tooltips;
    }

    private static void applyNEITooltipHandler(List<String> tooltip, ItemStack stack, ModularUIContext context) {
        GuiContainerManager.applyItemCountDetails(tooltip, stack);

        if (GuiContainerManager.getManager() == null) return;
        if (GuiContainerManager.shouldShowTooltip(context.getScreen())) {
            for (IContainerTooltipHandler handler : GuiContainerManager.getManager().instanceTooltipHandlers)
                tooltip = handler.handleItemTooltip(
                        context.getScreen(),
                        stack,
                        context.getCursor().getX(),
                        context.getCursor().getY(),
                        tooltip);
        }
    }
}
