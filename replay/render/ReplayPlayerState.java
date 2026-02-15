package com.mcsrranked.client.anticheat.replay.render;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;


public class ReplayPlayerState {
   private WorldTypes worldType;
   private Vec3d pos;
   private float pitch;
   private float yaw;
   private EntityPose pose;
   private boolean death;
   private boolean visible;
   private boolean ridingBoat;
   private boolean inanimate;
   private boolean pause;

   public ReplayPlayerState() {
      this(WorldTypes.OVERWORLD, Vec3d.ZERO, 0.0F, 0.0F, EntityPose.STANDING, false, false, false, false, false);
   }

   public ReplayPlayerState(WorldTypes worldType, Vec3d pos, float pitch, float yaw, EntityPose pose, boolean death, boolean visible, boolean ridingBoat, boolean inanimate, boolean pause) {
      this.worldType = worldType;
      this.pos = pos;
      this.pitch = pitch;
      this.yaw = yaw;
      this.pose = pose;
      this.death = death;
      this.visible = visible;
      this.ridingBoat = ridingBoat;
      this.inanimate = inanimate;
      this.pause = pause;
   }

   public ReplayPlayerState duplicate() {
      return new ReplayPlayerState(this.worldType, this.pos.add(0.0D, 0.0D, 0.0D), this.pitch, this.yaw, this.pose, this.death, this.visible, this.ridingBoat, false, this.pause);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ReplayPlayerState)) {
         return false;
      } else {
         ReplayPlayerState that = (ReplayPlayerState)o;
         return Float.compare(this.pitch, that.pitch) == 0 && Float.compare(this.yaw, that.yaw) == 0 && this.ridingBoat == that.ridingBoat && this.death == that.death && this.visible == that.visible && this.worldType == that.worldType && Objects.equals(this.pos, that.pos) && this.pose == that.pose && this.pause == that.pause;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.worldType, this.pos, this.pitch, this.yaw, this.pose, this.death, this.pause});
   }

   public WorldTypes getWorldType() {
      return this.worldType;
   }

   public void setWorldType(WorldTypes worldType) {
      if (worldType != this.worldType) {
         this.ridingBoat = false;
         this.inanimate = true;
      }

      this.worldType = worldType;
      this.visible = true;
      this.death = false;
   }

   public void setInanimate() {
      this.inanimate = true;
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public void setPos(Vec3d pos) {
      this.pos = pos;
   }

   public EntityPose getPose() {
      return this.pose;
   }

   public void setPose(EntityPose pose) {
      this.pose = pose;
   }

   public boolean isDeath() {
      return this.death;
   }

   public void setDeath(boolean death) {
      this.ridingBoat = false;
      this.death = death;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.ridingBoat = false;
      this.visible = visible;
   }

   public boolean isRidingBoat() {
      return this.ridingBoat;
   }

   public void setRidingBoat(boolean ridingBoat) {
      this.ridingBoat = ridingBoat;
   }

   public boolean isPause() {
      return this.pause;
   }

   public void setPause(boolean pause) {
      this.pause = pause;
   }

   public void apply(ReplayEntityTracker<ReplayPlayerEntity> entityTracker) {
      if (entityTracker.getSpawnPos() == Vec3d.ZERO || entityTracker.isDead() && !this.isDeath()) {
         entityTracker.setSpawnPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getYaw(), this.getPitch());
      }

      entityTracker.setDimension(this.getWorldType());
      entityTracker.setPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getYaw(), this.getPitch());
      entityTracker.setVisible(this.isVisible());
      ((ReplayPlayerEntity)entityTracker.getTarget()).setBoatActivate(this.isRidingBoat(), this.getPos().add(0.0D, 0.48D, 0.0D));
      ((ReplayPlayerEntity)entityTracker.getTarget()).setPose(this.getPose());
      ((ReplayPlayerEntity)entityTracker.getTarget()).setPause(this.isPause());
      if (this.isDeath()) {
         entityTracker.death();
      }

      if (this.inanimate) {
         entityTracker.inanimateNextTick();
      }

   }
}
