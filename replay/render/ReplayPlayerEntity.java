package com.mcsrranked.client.anticheat.replay.render;

import com.google.common.collect.Lists;
import com.mcsrranked.client.anticheat.mixin.render.PlayerEntityModelAccessor;
import com.mcsrranked.client.anticheat.replay.Replay;
import com.mcsrranked.client.anticheat.replay.ReplayEntityManager;
import com.mcsrranked.client.anticheat.replay.tracking.cinematic.CinematicCamera;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.AfterWorldTick;
import com.mcsrranked.client.config.RankedOptions;
import com.mcsrranked.client.utils.TextureUtils;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.text.LiteralText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.RayTraceContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Formatting;

public class ReplayPlayerEntity extends LivingEntity {
   public static final HashMap<UUID, UUID> replayPlayerEntitySkins = new HashMap();
   private final ReplayBoatEntity replayPlayerBoat;
   private ReplayPlayerEntity.DisplayType displayType;
   private boolean ghostMode;
   private boolean pause;
   public ItemStack mainHandStack;
   public ItemStack offHandStack;
   public ItemStack headStack;
   public ItemStack chestStack;
   public ItemStack legsStack;
   public ItemStack feetStack;
   private boolean isOnRender;
   private boolean boatActivate;
   private Vec3d boatPos;
   boolean invisible;
   private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);

   public ReplayPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
      super(entityType, world);
      this.replayPlayerBoat = new ReplayBoatEntity(Replay.REPLAY_BOAT_ENTITY_TYPE, (World)null);
      this.displayType = ReplayPlayerEntity.DisplayType.NORMAL;
      this.ghostMode = false;
      this.pause = false;
      this.mainHandStack = ItemStack.EMPTY;
      this.offHandStack = ItemStack.EMPTY;
      this.headStack = ItemStack.EMPTY;
      this.chestStack = ItemStack.EMPTY;
      this.legsStack = ItemStack.EMPTY;
      this.feetStack = ItemStack.EMPTY;
      this.isOnRender = false;
      this.boatActivate = false;
      this.boatPos = Vec3d.ZERO;
      this.invisible = false;
   }

   public Iterable<ItemStack> method_5661() {
      return new ArrayList();
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      if (slot.equals(EquipmentSlot.MAINHAND)) {
         return this.mainHandStack;
      } else if (slot.equals(EquipmentSlot.OFFHAND)) {
         return this.offHandStack;
      } else if (slot.equals(EquipmentSlot.HEAD)) {
         return this.headStack;
      } else if (slot.equals(EquipmentSlot.CHEST)) {
         return this.chestStack;
      } else if (slot.equals(EquipmentSlot.LEGS)) {
         return this.legsStack;
      } else {
         return slot.equals(EquipmentSlot.FEET) ? this.feetStack : ItemStack.EMPTY;
      }
   }

   public void clearInventory() {
      EquipmentSlot[] var1 = EquipmentSlot.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EquipmentSlot value = var1[var3];
         this.equipStack(value, ItemStack.EMPTY);
      }

   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      if (slot.equals(EquipmentSlot.MAINHAND)) {
         this.mainHandStack = stack;
      }

      if (slot.equals(EquipmentSlot.OFFHAND)) {
         this.offHandStack = stack;
      }

      if (slot.equals(EquipmentSlot.HEAD)) {
         this.headStack = stack;
      }

      if (slot.equals(EquipmentSlot.CHEST)) {
         this.chestStack = stack;
      }

      if (slot.equals(EquipmentSlot.LEGS)) {
         this.legsStack = stack;
      }

      if (slot.equals(EquipmentSlot.FEET)) {
         this.feetStack = stack;
      }

   }

   public void calculateDimensions() {
      if (this.world != null) {
         super.calculateDimensions();
      }

   }

   public Arm getMainArm() {
      return Arm.RIGHT;
   }

   public boolean hasNoGravity() {
      return true;
   }

   public boolean canBreatheInWater() {
      return true;
   }

   public boolean isWooded() {
      int glowDistance = (Integer)SpeedRunOption.getOption(RankedOptions.REPLAY_GLOW_DISTANCE);
      if (glowDistance > 0 && !this.isInvisible() && this.world.isClient && MinecraftClient.getInstance().cameraEntity != null && !this.isGhostMode()) {
         Camera gameCamera = MinecraftClient.getInstance().gameRenderer.getCamera();
         CinematicCamera camera = (CinematicCamera)gameCamera;
         return !camera.ranked$isCinematicActivated() && !this.getBlockPos().isWithinDistance(gameCamera.getPos(), (double)glowDistance);
      } else {
         return false;
      }
   }

   public boolean hasVehicle() {
      return super.hasVehicle() || this.isOnRender && this.boatActivate;
   }

   public void setBoatActivate(boolean boatActivate, Vec3d pos) {
      this.boatActivate = boatActivate;
      this.boatPos = pos;
   }

   public void tick() {
      this.noClip = true;
      this.setVelocity(Vec3d.ZERO);
      this.ridingUpdate();
      super.tick();
      this.tickHandSwing();
   }

   public void ridingUpdate() {
      if (this.world instanceof ClientWorld) {
         if (this.replayPlayerBoat.world != this.world) {
            if (!this.boatActivate) {
               return;
            }

            this.replayPlayerBoat.removed = false;
            this.replayPlayerBoat.world = this.world;
            this.replayPlayerBoat.setTargetPlayer(this);
            if (this.replayPlayerBoat.getEntityId() >= 0) {
               this.replayPlayerBoat.setEntityId(ReplayEntityManager.ID_GENERATOR.decrementAndGet());
            }

            if (this.world.getEntityById(this.replayPlayerBoat.getEntityId()) == null) {
               ((AfterWorldTick)MinecraftClient.getInstance()).ranked$addRunnableAfterWorldTick(() -> {
                  if (this.replayPlayerBoat.world != null) {
                     ((ClientWorld)this.world).addEntity(this.replayPlayerBoat.getEntityId(), this.replayPlayerBoat);
                  }

               });
               return;
            }
         }

         if (!this.hasVehicle() && this.boatActivate) {
            this.replayPlayerBoat.setInvisible(false);
            this.replayPlayerBoat.refreshPositionAndAngles(this.boatPos.getX(), this.boatPos.getY(), this.boatPos.getZ(), this.getHeadYaw(), 0.0F);
            this.startRiding(this.replayPlayerBoat, true);
         } else if (this.hasVehicle() && !this.boatActivate) {
            this.stopRiding();
            this.replayPlayerBoat.setInvisible(true);
         } else if (this.boatActivate) {
            this.replayPlayerBoat.updateTrackedPositionAndAngles(this.boatPos.getX(), this.boatPos.getY(), this.boatPos.getZ(), this.replayPlayerBoat.getDirection(this.boatPos), 0.0F, 3, true);
         } else if (!this.replayPlayerBoat.isInvisible()) {
            this.replayPlayerBoat.setInvisible(true);
         }

      }
   }

   public void setVelocity(Vec3d velocity) {
   }

   public boolean isInvisible() {
      return this.invisible;
   }

   public void setInvisible(boolean invisible) {
      this.invisible = invisible;
   }

   public boolean isInvisibleTo(PlayerEntity player) {
      return false;
   }

   public boolean isInsideWall() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void setTargetSkinUuid(UUID uuid) {
      replayPlayerEntitySkins.put(this.uuid, uuid);
   }

   public void removeBoat() {
      if (this.replayPlayerBoat.world != null) {
         ((ClientWorld)this.replayPlayerBoat.world).removeEntity(this.replayPlayerBoat.getEntityId());
         this.replayPlayerBoat.world = null;
      }

   }

   public void setGhostMode(boolean ghostMode) {
      this.ghostMode = ghostMode;
      this.displayType = ghostMode ? ReplayPlayerEntity.DisplayType.TRANSLUCENT : ReplayPlayerEntity.DisplayType.NORMAL;
      if (this.world != null) {
         this.stopRiding();
      }

   }

   public boolean isGhostMode() {
      return this.ghostMode;
   }

   public void setPause(boolean pause) {
      this.pause = pause;
   }

   public ReplayPlayerEntity.DisplayType getDisplayType() {
      return this.displayType;
   }

   public boolean isSneaking() {
      return this.getPose() == EntityPose.CROUCHING;
   }

   public SoundEvent getHurtSound(DamageSource source) {
      if (source == DamageSource.ON_FIRE) {
         return SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE;
      } else if (source == DamageSource.DROWN) {
         return SoundEvents.ENTITY_PLAYER_HURT_DROWN;
      } else {
         return source == DamageSource.SWEET_BERRY_BUSH ? SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH : SoundEvents.ENTITY_PLAYER_HURT;
      }
   }

   public SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PLAYER_DEATH;
   }

   @Override
   public Iterable<ItemStack> getArmorItems() {
      return this.armorItems;
   }

   public boolean collides() {
      return false;
   }

   public static enum DisplayType {
      GHOST,
      TRANSLUCENT,
      NORMAL;

      // $FF: synthetic method
      private static ReplayPlayerEntity.DisplayType[] $values() {
         return new ReplayPlayerEntity.DisplayType[]{GHOST, TRANSLUCENT, NORMAL};
      }
   }

   public static class Renderer extends LivingEntityRenderer<ReplayPlayerEntity, Model> {
      private final GoldenBootsFeatureRenderer<ReplayPlayerEntity, ReplayPlayerEntity.Model, BipedEntityModel<ReplayPlayerEntity>> goldenBootsFeature;
      private boolean slim = false;
      private boolean throughWall = false;

      public Renderer(EntityRenderDispatcher dispatcher) {
         super(dispatcher, new ReplayPlayerEntity.Model(false), 0.0F);
         this.addFeature(new HeldItemFeatureRenderer(this));
         this.addFeature(new ArmorFeatureRenderer<ReplayPlayerEntity, Model, BipedEntityModel<ReplayPlayerEntity>>(this, new BipedEntityModel(0.5F), new BipedEntityModel(1.0F)) {
            public void renderArmorParts(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ArmorItem armorItem, boolean bl, BipedEntityModel<ReplayPlayerEntity> bipedEntityModel, boolean bl2, float f, float g, float h, @Nullable String string) {
               VertexConsumer vertexConsumer = ItemRenderer.method_27952(vertexConsumerProvider, Renderer.this.throughWall ? CustomRenderPhase.getRenderLayerThroughWall(this.getArmorTexture(armorItem, bl2, string), false) : CustomRenderPhase.getRenderLayer(this.getArmorTexture(armorItem, bl2, string), false, (ReplayPlayerEntity.DisplayType)null), false, bl);
               bipedEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, f, g, h, 1.0F);
            }
         });
         this.addFeature(new HeadFeatureRenderer(this));
         this.addFeature(new ElytraFeatureRenderer(this));
         this.goldenBootsFeature = new GoldenBootsFeatureRenderer(this);
      }

      protected void setupTransforms(ReplayPlayerEntity replayPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
         float i = replayPlayerEntity.getLeaningPitch(h);
         float j;
         float k;
         if (replayPlayerEntity.isFallFlying()) {
            super.setupTransforms(replayPlayerEntity, matrixStack, f, g, h);
            j = (float)replayPlayerEntity.getRoll() + h;
            k = MathHelper.clamp(j * j / 100.0F, 0.0F, 1.0F);
            if (!replayPlayerEntity.isUsingRiptide()) {
               matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0F - replayPlayerEntity.pitch)));
            }

            Vec3d vec3d = replayPlayerEntity.getRotationVec(h);
            Vec3d vec3d2 = replayPlayerEntity.getVelocity();
            double d = Entity.squaredHorizontalLength(vec3d2);
            double e = Entity.squaredHorizontalLength(vec3d);
            if (d > 0.0D && e > 0.0D) {
               double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / (Math.sqrt(d) * Math.sqrt(e));
               double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
               matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion((float)(Math.signum(m) * Math.acos(l))));
            }
         } else if (i > 0.0F) {
            super.setupTransforms(replayPlayerEntity, matrixStack, f, g, h);
            j = replayPlayerEntity.isTouchingWater() ? -90.0F - replayPlayerEntity.pitch : -90.0F;
            k = MathHelper.lerp(i, 0.0F, j);
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(k));
            if (replayPlayerEntity.isInSwimmingPose()) {
               matrixStack.translate(0.0D, -1.0D, 0.30000001192092896D);
            }
         } else {
            super.setupTransforms(replayPlayerEntity, matrixStack, f, g, h);
         }

      }

      public Identifier getTexture(ReplayPlayerEntity entity) {
         if (ReplayPlayerEntity.replayPlayerEntitySkins.containsKey(entity.uuid)) {
            String uuidString = ((UUID)ReplayPlayerEntity.replayPlayerEntitySkins.get(entity.uuid)).toString();
            Identifier skin = TextureUtils.getPlayerSkin(uuidString);
            this.slim = TextureUtils.isPlayerSlim(uuidString);
            return skin;
         } else {
            return DefaultSkinHelper.getTexture();
         }
      }

      @Nullable
      protected RenderLayer getRenderLayer(ReplayPlayerEntity entity, boolean showBody, boolean translucent, boolean bl) {
         Identifier texture = this.getTexture(entity);
         return this.throughWall ? CustomRenderPhase.getRenderLayerThroughWall(texture, entity.hurtTime > 0) : CustomRenderPhase.getRenderLayer(texture, entity.hurtTime > 0, ((ReplayPlayerEntity.Model)this.getModel()).getDisplayType());
      }

      public void render(ReplayPlayerEntity replayEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
         replayEntity.isOnRender = true;
         List<FeatureRenderer<ReplayPlayerEntity, Model>> featuresBackup = Lists.newArrayList(this.features);
         if (((ReplayPlayerEntity.Model)this.getModel()).getDisplayType() != ReplayPlayerEntity.DisplayType.NORMAL) {
            this.features.clear();
         }

         if (((ReplayPlayerEntity.Model)this.getModel()).getDisplayType() == ReplayPlayerEntity.DisplayType.GHOST) {
            this.addFeature(this.goldenBootsFeature);
         }

         this.throughWall = false;
         if (replayEntity.isGhostMode()) {
            this.throughWall = true;
         } else if (!replayEntity.isInvisible() && MinecraftClient.getInstance().cameraEntity != null && replayEntity.world != null) {
            HitResult rayTrace = replayEntity.world.rayTrace(new RayTraceContext(MinecraftClient.getInstance().gameRenderer.getCamera().getPos(), replayEntity.getPos(), RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, replayEntity));
            if (rayTrace.getType() != HitResult.Type.MISS) {
               this.throughWall = true;
            }
         }

         if (((PlayerEntityModelAccessor)this.getModel()).isThinArms() != this.slim) {
            this.model = new ReplayPlayerEntity.Model(this.slim);
         }

         ((ReplayPlayerEntity.Model)this.getModel()).isSneaking = replayEntity.isSneaking();
         ((ReplayPlayerEntity.Model)this.getModel()).riding = replayEntity.hasVehicle();
         PlayerEntity player = replayEntity.world.getClosestPlayer(replayEntity, (double)(Integer)SpeedRunOption.getOption(RankedOptions.GHOST_INVISIBLE_DISTANCE));
         if (!replayEntity.isGhostMode()) {
            ((ReplayPlayerEntity.Model)this.getModel()).setDisplayType(ReplayPlayerEntity.DisplayType.NORMAL);
         } else if (player != null && !player.isSpectator()) {
            ((ReplayPlayerEntity.Model)this.getModel()).setDisplayType(ReplayPlayerEntity.DisplayType.GHOST);
         } else {
            ((ReplayPlayerEntity.Model)this.getModel()).setDisplayType(ReplayPlayerEntity.DisplayType.TRANSLUCENT);
         }

         replayEntity.displayType = ((ReplayPlayerEntity.Model)this.getModel()).getDisplayType();
         super.render(replayEntity, f, g, matrixStack, vertexConsumerProvider, i);
         if (((ReplayPlayerEntity.Model)this.getModel()).getDisplayType() == ReplayPlayerEntity.DisplayType.NORMAL && replayEntity.pause) {
            matrixStack.push();
            matrixStack.translate(0.0D, (double)(-((replayEntity.getHeight() + 0.5F) / 2.0F)) + 0.42D, 0.0D);
            matrixStack.scale(1.5F, 1.5F, 1.5F);
            this.renderLabelIfPresent(replayEntity, (new LiteralText("(\u23f8)")).formatted(Formatting.YELLOW), matrixStack, vertexConsumerProvider, i);
            matrixStack.pop();
         }

         if (((ReplayPlayerEntity.Model)this.getModel()).getDisplayType() != ReplayPlayerEntity.DisplayType.NORMAL) {
            this.features.clear();
            this.features.addAll(featuresBackup);
         }

         replayEntity.isOnRender = false;
      }

      protected boolean hasLabel(ReplayPlayerEntity livingEntity) {
         return livingEntity.isCustomNameVisible();
      }
   }

   public static class Model extends PlayerEntityModel<ReplayPlayerEntity> {
      private ReplayPlayerEntity.DisplayType displayType;

      public Model(boolean slim) {
         super(0.0F, slim);
         this.displayType = ReplayPlayerEntity.DisplayType.NORMAL;
      }

      public ReplayPlayerEntity.DisplayType getDisplayType() {
         return this.displayType;
      }

      public void setDisplayType(ReplayPlayerEntity.DisplayType displayType) {
         this.displayType = displayType;
      }

      public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
         float newAlpha = alpha;
         if (alpha == 1.0F && this.getDisplayType() != ReplayPlayerEntity.DisplayType.NORMAL) {
            newAlpha = this.getDisplayType() == ReplayPlayerEntity.DisplayType.GHOST ? 0.0F : (Float)SpeedRunOption.getOption(RankedOptions.GHOST_OPACITY);
         }

         super.render(matrices, vertices, light, overlay, red, green, blue, newAlpha);
      }
   }
}
