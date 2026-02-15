package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;


public abstract class PositionITimeLine<T extends Identifier> extends WorldTimeLine<T> {
   private final long rawPosition;
   private final BlockPos position;

   protected PositionITimeLine(TimeLineType type, WorldTypes world, long position) {
      super(type, world);
      this.rawPosition = position;
      this.position = BlockPos.fromLong(position);
   }

   public double getX() {
      return (double)this.getBlockPos().getX();
   }

   public double getY() {
      return (double)this.getBlockPos().getY();
   }

   public double getZ() {
      return (double)this.getBlockPos().getZ();
   }

   public BlockPos getBlockPos() {
      return this.position;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 8).put(superBuffer).putLong(this.rawPosition);
   }
}
