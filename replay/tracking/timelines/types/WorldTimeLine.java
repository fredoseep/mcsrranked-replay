package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import java.nio.ByteBuffer;

public abstract class WorldTimeLine<T extends Identifier> extends TimeLine<T> {
   private final WorldTypes world;

   public WorldTimeLine(TimeLineType type, WorldTypes world) {
      super(type);
      this.world = world;
   }

   public WorldTypes getWorld() {
      return this.world;
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)this.world.ordinal());
   }
}
