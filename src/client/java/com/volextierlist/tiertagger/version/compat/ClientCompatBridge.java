package com.volextierlist.tiertagger.version.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public interface ClientCompatBridge {
    PlayerSkinWidget createPlayerSkinWidget(MinecraftClient client, Identifier textureId, String skinUrl, int width, int height);

    void drawSeeThroughText(TextRenderer textRenderer, Text text, float x, Matrix4f matrix4f,
                            VertexConsumerProvider vertexConsumers, int backgroundColor, int light);

    KeyBinding createKeyBinding(String translationKey, int keyCode, String category);
}
