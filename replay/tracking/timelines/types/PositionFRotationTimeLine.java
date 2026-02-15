package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.client.util.math.Vector3f;

import java.nio.ByteBuffer;


public abstract class PositionFRotationTimeLine<T extends Identifier> extends PositionFTimeLine<T> {
   private final short yaw;
   private final short pitch;

   protected PositionFRotationTimeLine(TimeLineType type, WorldTypes world, Vector3f position, short yaw, short pitch) {
      super(type, world, position);
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public PositionFRotationTimeLine(TimeLineType type, WorldTypes world, Vector3f position, float yaw, float pitch) {
      this(type, world, position, RotationTimeLine.convertRotation(yaw), RotationTimeLine.convertRotation(pitch));
   }

   public float getYaw() {
      return RotationTimeLine.convertValue(this.yaw);
   }

   public float getPitch() {
      return RotationTimeLine.convertValue(this.pitch);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putShort(this.yaw).putShort(this.pitch);
   }
}
