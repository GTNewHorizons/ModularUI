package com.gtnewhorizons.modularui.common.keybind;

public class KeyBindHandler {

//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    private static void checkKeyState(int key, boolean state) {
//        if (key != 0) {
//            // imitates KeyBinding.setKeyBindState()
//            for (KeyBinding keyBinding : getKeyBindingMap().lookupAll(key)) {
//                if (KeyBindAPI.doForceCheckKeyBind(keyBinding)) {
//                    ((KeyBindAccess) keyBinding).setPressed(state);
//                }
//            }
//            // imitates KeyBinding.onTick()
//            if (state) {
//                KeyBinding keyBinding = getKeyBindingMap().lookupActive(key);
//                if (keyBinding != null) {
//                    if (KeyBindAPI.doForceCheckKeyBind(keyBinding)) {
//                        incrementPressTime(keyBinding);
//                    }
//
//                    Collection<KeyBinding> compatibles = KeyBindAPI.getCompatibles(keyBinding);
//                    if (compatibles.isEmpty()) return;
//                    for (KeyBinding keyBinding1 : compatibles) {
//                        if (keyBinding1.isActiveAndMatches(key) && KeyBindAPI.doForceCheckKeyBind(keyBinding1)) {
//                            incrementPressTime(keyBinding1);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onGuiKeyInput(GuiScreenEvent.ActionPerformedEvent.Pre event) {
//        if (!event.gui.mc.inGameHasFocus) {
//            int key = Keyboard.getEventKey();
//            boolean state = Keyboard.getEventKeyState();
//            checkKeyState(key, state);
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onMouseInput(GuiScreenEvent.ActionPerformedEvent.Pre event) {
//        if (!event.gui.mc.inGameHasFocus) {
//            int key = Mouse.getEventButton() - 100;
//            boolean state = Mouse.getEventButtonState();
//            checkKeyState(key, state);
//        }
//    }
//
//    public static IntHashMap getKeyBindingMap() {
//        return ((KeyBindAccess) Minecraft.getMinecraft().gameSettings.keyBindPickBlock).getHash();
//    }
}
