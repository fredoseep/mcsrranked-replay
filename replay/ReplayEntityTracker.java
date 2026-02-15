package com.mcsrranked.client.anticheat.replay;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.utils.ClientUtils;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import net.minecraft.entity.damage.DamageSource;


public class ReplayEntityTracker<T extends Entity> {
   private final OpponentPlayerTracker tracker;
   private final int id;
   private final T target;
   private WorldTypes dimension;
   private Vec3d spawnPos;
   private float spawnYaw;
   private float spawnPitch;
   private Vec3d pos;
   private float yaw;
   private float pitch;
   private boolean visible;
   private boolean attacked;
   private boolean damaged;
   private boolean deathNextTick;
   private boolean shouldAlive;
   private boolean firstPos;
   private boolean shouldFollowPos;
   private boolean shouldRespawn;
   private int inanimate;

   public ReplayEntityTracker(OpponentPlayerTracker tracker, int id, T target, WorldTypes dimension) {
      this.spawnPos = Vec3d.ZERO;
      this.pos = Vec3d.ZERO;
      this.visible = false;
      this.attacked = false;
      this.damaged = false;
      this.deathNextTick = false;
      this.shouldAlive = true;
      this.firstPos = false;
      this.shouldFollowPos = true;
      this.shouldRespawn = true;
      this.inanimate = 0;
      this.tracker = tracker;
      this.id = id;
      this.target = target;
      this.target.world = null;
      this.dimension = dimension;
      this.target.setInvulnerable(true);
      if (this.target instanceof MobEntity) {
         ((MobEntity)this.target).setAiDisabled(true);
      }

   }

   public void attacked() {
      this.attacked = true;
   }

   public void damaged() {
      this.damaged = true;
   }

   public boolean isDead() {
      return !this.shouldAlive;
   }

   public void death() {
      if (!this.isDead()) {
         this.deathNextTick = true;
      }
   }

   public void setVisible(boolean visible) {
      if (!this.visible && visible) {
         this.getTarget().lastRenderX = this.getPos().getX();
         this.getTarget().lastRenderY = this.getPos().getY();
         this.getTarget().lastRenderZ = this.getPos().getZ();
      }

      this.visible = visible;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public WorldTypes getDimension() {
      return this.dimension;
   }

   public void setDimension(WorldTypes dimension) {
      this.dimension = dimension;
   }

   public Vec3d getSpawnPos() {
      return this.spawnPos;
   }

   public void setSpawnPos(double x, double y, double z, float yaw, float pitch) {
      this.spawnPos = new Vec3d(x, y, z);
      this.spawnYaw = yaw;
      this.spawnPitch = pitch;
      this.firstPos = false;
      this.shouldAlive = true;
      this.setPos(x, y, z, yaw, pitch);
   }

   public void setPos(double x, double y, double z, float yaw, float pitch) {
      this.pos = new Vec3d(x, y, z);
      this.yaw = yaw;
      this.pitch = pitch;
      this.setVisible(true);
   }

   public void inanimateNextTick() {
      this.inanimate = 5;
      this.target.resetPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
   }

   public T getTarget() {
      return this.target;
   }

   public int getId() {
      return this.id;
   }

   public void backToSpawn() {
      this.pos = new Vec3d(this.spawnPos.getX(), this.spawnPos.getY(), this.spawnPos.getZ());
      this.yaw = this.spawnYaw;
      this.pitch = this.spawnPitch;
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void disableFollowPos() {
      this.shouldFollowPos = false;
   }

   public void disableRespawn() {
      this.shouldRespawn = false;
   }

   private void rollbackEntity() {
      this.target.removed = false;
      if (this.target instanceof LivingEntity) {
         ((LivingEntity)this.target).setHealth(((LivingEntity)this.target).getMaxHealth());
         ((LivingEntity)this.target).deathTime = 0;
      }

   }

   void tick(ClientWorld world) {
      boolean isInWorld = world.getEntityById(this.getId()) != null;
      if (WorldTypes.fromDimension(world.getDimension()) == this.dimension && this.isVisible() && this.tracker.isActive() && (!this.tracker.isGhostMode() || this.getTarget() instanceof ReplayPlayerEntity)) {
         if (!this.target.removed || this.shouldRespawn) {
            if (this.shouldAlive) {
               if (this.target.world != null && isInWorld) {
                  if (!this.firstPos || this.shouldFollowPos) {
                     this.rollbackEntity();
                     if (world.getEntityById(this.id) == null) {
                        world.addEntity(this.id, this.target);
                     }

                     if (this.target instanceof ReplayPlayerEntity) {
                        ((ReplayPlayerEntity)this.target).ridingUpdate();
                     }

                     if (!(this.target.squaredDistanceTo(new Vec3d(this.pos.getX(), this.target.getY(), this.pos.getZ())) > Math.pow(12.0D, 2.0D)) && this.inanimate <= 0) {
                        this.target.updateTrackedPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch, 3, false);
                        this.target.updateTrackedHeadRotation(this.yaw, 3);
                     } else {
                        this.target.updateTrackedPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch, 3, false);
                        this.target.refreshPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch);
                        this.target.setHeadYaw(this.yaw);
                        if (this.inanimate > 0) {
                           --this.inanimate;
                        }
                     }
                  }
               } else {
                  this.rollbackEntity();
                  this.target.world = world;
                  this.target.age = 0;
                  if (this.target instanceof ReplayPlayerEntity) {
                     ((ReplayPlayerEntity)this.target).ridingUpdate();
                  }

                  this.target.updateTrackedPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch, 3, false);
                  this.target.refreshPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch);
                  world.addEntity(this.id, this.target);
               }
            }

            this.firstPos = true;
            if (this.attacked) {
               if (this.target instanceof LivingEntity) {
                  ((LivingEntity)this.target).swingHand(((LivingEntity)this.target).getActiveHand());
               }

               this.attacked = false;
            }

            if (this.damaged) {
               if (this.target instanceof LivingEntity) {
                  ClientUtils.playSound(((LivingEntity)this.target).getHurtSound(DamageSource.GENERIC), this.target.getSoundCategory(), this.target, 0.8F + (new Random()).nextFloat() * 0.5F);
               }

               this.target.animateDamage();
               this.damaged = false;
            }

            if (this.deathNextTick) {
               if (!(this.target instanceof LivingEntity)) {
                  this.target.remove();
               } else {
                  if (!(this.target instanceof ReplayPlayerEntity) || !((ReplayPlayerEntity)this.target).isGhostMode()) {
                     ClientUtils.playSound(((LivingEntity)this.target).getDeathSound(), this.target.getSoundCategory(), this.target, 1.0F);
                  }

                  ((LivingEntity)this.target).setHealth(0.0F);
               }

               this.deathNextTick = false;
               this.shouldAlive = false;
            }

         }
      } else {
         if (isInWorld) {
            if (this.target instanceof ReplayPlayerEntity) {
               ((ReplayPlayerEntity)this.target).removeBoat();
            }

            world.removeEntity(this.id);
         }

         this.target.world = null;
      }
   }

   public void onRender() {
      if (this.inanimate > 0) {
         this.target.refreshPositionAndAngles(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.yaw, this.pitch);
         this.target.setHeadYaw(this.yaw);
      }

   }
}
