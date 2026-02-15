package com.mcsrranked.client.anticheat.replay.render;

import net.minecraft.client.render.OverlayTexture;
import com.mcsrranked.client.config.RankedOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;


public class GoldenBootsFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
   private static final Identifier GOLDEN_ARMOR_ID = new Identifier("textures/models/armor/gold_layer_1.png");
   private final BipedEntityModel<T> bipedEntityModel = new BipedEntityModel(0.5F);

   public GoldenBootsFeatureRenderer(FeatureRendererContext<T, M> context) {
      super(context);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
      this.bipedEntityModel.setVisible(false);
      this.bipedEntityModel.rightLeg.visible = true;
      this.bipedEntityModel.leftLeg.visible = true;
      ((BipedEntityModel)this.getContextModel()).setAttributes(this.bipedEntityModel);
      VertexConsumer vertexConsumer = ItemRenderer.method_27952(vertexConsumerProvider, CustomRenderPhase.getBootsRenderLayer(GOLDEN_ARMOR_ID), false, false);
      this.bipedEntityModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, (Float)SpeedRunOption.getOption(RankedOptions.GHOST_OPACITY));
   }
}
