package com.gtnewhorizons.modularui.api.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.modularui.api.KeyboardUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An interface that handles user interactions. These methods get called on the client. Can also be used as a listener.
 */
public interface Interactable {

    /**
     * called when clicked on the Interactable
     *
     * @param buttonId    the button id (Left == 0, right == 1)
     * @param doubleClick if it is the second click within 400ms
     * @return if further operations should abort
     */
    @ApiStatus.OverrideOnly
    default ClickResult onClick(int buttonId, boolean doubleClick) {
        return ClickResult.IGNORE;
    }

    /**
     * called when released a click on the Interactable
     *
     * @param buttonId the button id (Left == 0, right == 1)
     * @return if further operations should abort
     */
    @ApiStatus.OverrideOnly
    default boolean onClickReleased(int buttonId) {
        // This prevents mouse release on widgets behind if this widget was clicked;
        // Mouse click and release should be handled as one action in most of the cases.
        return true;
    }

    /**
     * called when the interactable is focused and the mouse gets dragged
     *
     * @param buttonId  the button id (Left == 0, right == 1)
     * @param deltaTime milliseconds since last mouse event
     */
    @ApiStatus.OverrideOnly
    default void onMouseDragged(int buttonId, long deltaTime) {}

    /**
     * Called the mouse wheel moved
     *
     * @param direction -1 for down, 1 for up
     */
    @ApiStatus.OverrideOnly
    default boolean onMouseScroll(int direction) {
        return false;
    }

    /**
     * called when the interactable is focused and a key is pressed
     *
     * @param character the typed character. Is equal to {@link Character#MIN_VALUE} if it's not a char
     * @param keyCode   code of the typed key. See {@link Keyboard}
     * @return if further operations should abort
     */
    @ApiStatus.OverrideOnly
    default boolean onKeyPressed(char character, int keyCode) {
        return false;
    }

    /**
     * @return if left or right ctrl/cmd is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean hasControlDown() {
        return KeyboardUtil.isCtrlKeyDown();
    }

    /**
     * @return if left or right shift is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean hasShiftDown() {
        return KeyboardUtil.isShiftKeyDown();
    }

    /**
     * @return if alt or alt gr is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean hasAltDown() {
        return KeyboardUtil.isAltKeyDown();
    }

    /**
     * @param key key id, see {@link Keyboard}
     * @return if the key is pressed
     */
    @SideOnly(Side.CLIENT)
    static boolean isKeyPressed(int key) {
        return Keyboard.isKeyDown(key);
    }

    /**
     * Plays the default button click sound
     */
    @SideOnly(Side.CLIENT)
    static void playButtonClickSound() {
        playButtonClickSound(new ResourceLocation("gui.button.press"));
    }

    /**
     * Plays the input click sound
     */
    @SideOnly(Side.CLIENT)
    static void playButtonClickSound(ResourceLocation soundResource) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(soundResource, 1.0F));
    }

    enum ClickResult {
        /**
         * Nothing happened on this click
         */
        IGNORE,
        /**
         * Nothing happened, but it was clicked
         */
        ACKNOWLEDGED,
        /**
         * Nothing happened. No other hovered widgets nor vanilla slots should get interacted
         */
        REJECT,
        /**
         * Clicked, but it should be handled by {@link net.minecraft.client.gui.inventory.GuiContainer#mouseClicked}
         */
        DELEGATE,
        /**
         * Success, but don't try to get focus
         */
        ACCEPT,
        /**
         * Successfully clicked. Should be returned if it should try to receive focus
         */
        SUCCESS
    }
}
