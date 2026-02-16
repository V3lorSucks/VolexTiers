package com.volextierlist.tiertagger.version.compat;

import com.volextierlist.tiertagger.VolexTierTagger;
import com.volextierlist.tiertagger.version.MinecraftVersion;
import com.volextierlist.tiertagger.version.VersionSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClientCompatBridge121 implements ClientCompatBridge {
    // 1.21.11+ uses new KeyBinding.Category API
    private static final MinecraftVersion KEYBINDING_CHANGE_VERSION = MinecraftVersion.parse("1.21.11");
    private static final boolean USE_NEW_KEYBINDING_API = VersionSupport.current().compareTo(KEYBINDING_CHANGE_VERSION) >= 0;

    @Override
    public PlayerSkinWidget createPlayerSkinWidget(MinecraftClient client, Identifier textureId, String skinUrl, int width, int height) {
        SkinTextures skinTextures = new SkinTextures(
            textureId,
            skinUrl,
            null,
            null,
            SkinTextures.Model.WIDE,
            false
        );

        return new PlayerSkinWidget(
            width,
            height,
            client.getEntityModelLoader(),
            () -> skinTextures
        );
    }

    @Override
    public void drawSeeThroughText(TextRenderer textRenderer, Text text, float x, Matrix4f matrix4f,
                                   VertexConsumerProvider vertexConsumers, int backgroundColor, int light) {
        textRenderer.draw(
            text,
            x,
            0,
            0xFFFFFF,
            false,
            matrix4f,
            vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            backgroundColor,
            light
        );
    }

    @Override
    public KeyBinding createKeyBinding(String translationKey, int keyCode, String category) {
        if (USE_NEW_KEYBINDING_API) {
            return createKeyBindingReflect(translationKey, keyCode, category, true);
        }
        return createKeyBindingReflect(translationKey, keyCode, category, false);
    }

    // Cached category object for reuse across multiple keybindings
    private Object cachedCategory = null;
    private Class<?> cachedCategoryClass = null;

    private KeyBinding createKeyBindingReflect(String translationKey, int keyCode, String category, boolean useNewApi) {
        try {
            if (useNewApi) {
                if (cachedCategory == null) {
                    for (Class<?> inner : KeyBinding.class.getDeclaredClasses()) {
                        for (Method m : inner.getMethods()) {
                            if (java.lang.reflect.Modifier.isStatic(m.getModifiers())
                                    && m.getParameterCount() == 1
                                    && m.getParameterTypes()[0] == Identifier.class
                                    && m.getReturnType() == inner) {
                                cachedCategoryClass = inner;
                                cachedCategory = m.invoke(null, Identifier.of("ctl-tiertagger", "controls"));
                                break;
                            }
                        }
                        if (cachedCategory != null) break;
                    }
                    if (cachedCategory == null) {
                        throw new RuntimeException("Could not find KeyBinding.Category class or register method");
                    }
                }

                Constructor<?> constructor = KeyBinding.class.getConstructor(
                    String.class, InputUtil.Type.class, int.class, cachedCategoryClass
                );
                return (KeyBinding) constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, keyCode, cachedCategory);
            } else {
                Constructor<?> constructor = KeyBinding.class.getConstructor(
                    String.class, InputUtil.Type.class, int.class, String.class
                );
                return (KeyBinding) constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, keyCode, category);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create KeyBinding via reflection: " + e.getMessage(), e);
        }
    }
}
