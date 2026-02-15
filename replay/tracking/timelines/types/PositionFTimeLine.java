package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;


public abstract class PositionFTimeLine<T extends Identifier> extends WorldTimeLine<T> {
   private final Vector3f position;

   protected PositionFTimeLine(TimeLineType type, WorldTypes world, Vector3f position) {
      super(type, world);
      this.position = position;
   }

   public float getX() {
      return this.position.getX();
   }

   public float getY() {
      return this.position.getY();
   }

   public float getZ() {
      return this.position.getZ();
   }

   public Vec3d getPosition() {
      return new Vec3d(this.position);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.capacity() + 12).put(superBuffer).putFloat(this.position.getX()).putFloat(this.position.getY()).putFloat(this.position.getZ());
   }
}
