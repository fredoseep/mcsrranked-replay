package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import java.nio.ByteBuffer;
import net.minecraft.util.math.MathHelper;


public abstract class RotationTimeLine<T extends Identifier> extends WorldTimeLine<T> {
   private final short yaw;
   private final short pitch;

   public RotationTimeLine(TimeLineType type, WorldTypes world, short yaw, short pitch) {
      super(type, world);
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public RotationTimeLine(TimeLineType type, WorldTypes world, float yaw, float pitch) {
      this(type, world, convertRotation(yaw), convertRotation(pitch));
   }

   public static short convertRotation(float value) {
      return (short)((int)(MathHelper.wrapDegrees(value) / 0.01F));
   }

   public static float convertValue(short rotation) {
      return MathHelper.wrapDegrees((float)rotation * 0.01F);
   }

   public float getYaw() {
      return convertValue(this.yaw);
   }

   public float getPitch() {
      return convertValue(this.pitch);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.capacity() + 4).put(superBuffer).putShort(this.yaw).putShort(this.pitch);
   }
}
