package com.mcsrranked.client.anticheat.replay.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.Vector3f;
import com.google.common.collect.Lists;
import net.minecraft.client.render.RenderLayer;
import com.google.common.collect.ImmutableList.Builder;
import com.mcsrranked.client.config.RankedOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;


public class ReplayBoatEntity extends LivingEntity {
   private ReplayPlayerEntity targetPlayer;

   public ReplayBoatEntity(EntityType<? extends LivingEntity> entityType, World world) {
      super(entityType, world);
   }

   public Iterable<ItemStack> getArmorItems() {
      return Lists.newArrayList();
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
   }

   public Arm getMainArm() {
      return Arm.LEFT;
   }

   public double getMountedHeightOffset() {
      return -0.1D;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean collides() {
      return false;
   }

   public ReplayPlayerEntity getTargetPlayer() {
      return this.targetPlayer;
   }

   public void setTargetPlayer(ReplayPlayerEntity targetPlayer) {
      this.targetPlayer = targetPlayer;
   }

   public void tick() {
      if (this.world != null) {
         this.noClip = true;
         super.tick();
      }
   }

   public void remove() {
      super.remove();
   }

   public float getDirection(Vec3d newPos) {
      Vec3d dir = newPos.subtract(this.getPos());
      return dir.equals(Vec3d.ZERO) ? this.yaw : (float)(Math.atan2(-dir.getX(), dir.getZ()) * 180.0D / 3.141592653589793D);
   }

   public static class Model extends CompositeEntityModel<ReplayBoatEntity> {
      private final ModelPart bottom;
      private final ImmutableList<ModelPart> parts;

      public Model() {
         ModelPart[] modelParts = new ModelPart[]{(new ModelPart(this, 0, 0)).setTextureSize(128, 64), (new ModelPart(this, 0, 19)).setTextureSize(128, 64), (new ModelPart(this, 0, 27)).setTextureSize(128, 64), (new ModelPart(this, 0, 35)).setTextureSize(128, 64), (new ModelPart(this, 0, 43)).setTextureSize(128, 64)};
         modelParts[0].addCuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
         modelParts[0].setPivot(0.0F, 3.0F, 1.0F);
         modelParts[1].addCuboid(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, 0.0F);
         modelParts[1].setPivot(-15.0F, 4.0F, 4.0F);
         modelParts[2].addCuboid(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, 0.0F);
         modelParts[2].setPivot(15.0F, 4.0F, 0.0F);
         modelParts[3].addCuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
         modelParts[3].setPivot(0.0F, 4.0F, -9.0F);
         modelParts[4].addCuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
         modelParts[4].setPivot(0.0F, 4.0F, 9.0F);
         modelParts[0].pitch = 1.5707964F;
         modelParts[1].yaw = 4.712389F;
         modelParts[2].yaw = 1.5707964F;
         modelParts[3].yaw = 3.1415927F;
         this.bottom = (new ModelPart(this, 0, 0)).setTextureSize(128, 64);
         this.bottom.addCuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
         this.bottom.setPivot(0.0F, -3.0F, 1.0F);
         this.bottom.pitch = 1.5707964F;
         Builder<ModelPart> builder = ImmutableList.builder();
         builder.addAll(Arrays.asList(modelParts));
         this.parts = builder.build();
      }

      public void setAngles(ReplayBoatEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      }

      public ImmutableList<ModelPart> getParts() {
         return this.parts;
      }

      public ModelPart getBottom() {
         return this.bottom;
      }
   }

   public static class Renderer extends EntityRenderer<ReplayBoatEntity> {
      private final Identifier TEXTURE = new Identifier("textures/entity/boat/oak.png");
      protected final ReplayBoatEntity.Model model = new ReplayBoatEntity.Model();

      public Renderer(EntityRenderDispatcher entityRenderDispatcher) {
         super(entityRenderDispatcher);
         this.shadowRadius = 0.0F;
      }

      public void render(ReplayBoatEntity boatEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
         if (boatEntity.getTargetPlayer().getDisplayType() != ReplayPlayerEntity.DisplayType.GHOST && !boatEntity.isInvisible()) {
            boolean ghostMode = boatEntity.getTargetPlayer().isGhostMode();
            matrixStack.push();
            matrixStack.translate(0.0D, 0.375D, 0.0D);
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - f));
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
            this.model.setAngles(boatEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CustomRenderPhase.getBoatRenderLayer(this.getTexture(boatEntity)));
            this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, ghostMode ? (float)Math.pow((double)(Float)SpeedRunOption.getOption(RankedOptions.GHOST_OPACITY), 2.0D) : 1.0F);
            if (!ghostMode || !boatEntity.isSubmergedInWater()) {
               VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getWaterMask());
               this.model.getBottom().render(matrixStack, vertexConsumer2, i, OverlayTexture.DEFAULT_UV);
            }

            matrixStack.pop();
            super.render(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
         }
      }

      public Identifier getTexture(ReplayBoatEntity boatEntity) {
         return this.TEXTURE;
      }
   }
}
