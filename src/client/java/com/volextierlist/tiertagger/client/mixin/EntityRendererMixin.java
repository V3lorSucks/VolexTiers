package com.volextierlist.tiertagger.client.mixin;

import com.volextierlist.tiertagger.VolexTierTagger;
import com.volextierlist.tiertagger.client.render.TierHudRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class EntityRendererMixin extends EntityRenderer<AbstractClientPlayerEntity> {

    protected EntityRendererMixin() {
        super(null);
    }

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", 
            at = @At("TAIL"))
    private void onRenderLabel(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, 
                              VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        try {
            // Render tier above the nametag
            TierHudRenderer.renderTierAboveNametag(player, matrices, vertexConsumers, light);
        } catch (Exception e) {
            VolexTierTagger.LOGGER.error("Error rendering tier label for {}: {}", player.getName().getString(), e.getMessage(), e);
        }
    }
}
