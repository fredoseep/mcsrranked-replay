package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionFRotationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.client.util.math.Vector3f;

import java.nio.ByteBuffer;
import java.util.UUID;


public abstract class EntityTimeLine<T extends Identifier> extends PositionFRotationTimeLine<T> {
   private final int entityId;

   protected EntityTimeLine(TimeLineType type, WorldTypes world, Vector3f position, short yaw, short pitch, int entityId) {
      super(type, world, position, yaw, pitch);
      this.entityId = entityId;
   }

   protected EntityTimeLine(TimeLineType type, WorldTypes world, Vector3f position, float yaw, float pitch, int entityId) {
      super(type, world, position, yaw, pitch);
      this.entityId = entityId;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putInt(this.entityId);
   }

   public UUID getEntityUUID() {
      return getEntityUUIDById(this.entityId);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public static UUID getEntityUUIDById(int entityId) {
      return new UUID(11497110107101100L, (long)entityId);
   }
}
