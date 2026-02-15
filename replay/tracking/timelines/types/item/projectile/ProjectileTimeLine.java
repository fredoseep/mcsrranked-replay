package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.projectile;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;


public abstract class ProjectileTimeLine<T extends Identifier> extends EntityTimeLine<T> {
   private final Vec3d velocity;

   protected ProjectileTimeLine(TimeLineType type, WorldTypes world, Vector3f pos, short yaw, short pitch, int entityId, Vec3d velocity) {
      super(type, world, pos, yaw, pitch, entityId);
      this.velocity = velocity;
   }

   protected ProjectileTimeLine(TimeLineType type, WorldTypes world, Vector3f pos, float yaw, float pitch, int entityId, Vec3d velocity) {
      super(type, world, pos, yaw, pitch, entityId);
      this.velocity = velocity;
   }

   public Vec3d getVelocity() {
      return this.velocity;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 24).put(superBuffer).putDouble(this.velocity.getX()).putDouble(this.velocity.getY()).putDouble(this.velocity.getZ());
   }
}
