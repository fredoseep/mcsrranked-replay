package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionITimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldPosIIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class BlockRemoveTimeLine extends PositionITimeLine<WorldPosIIdentifier> {
   protected BlockRemoveTimeLine(WorldTypes world, long position) {
      super(TimeLineType.BLOCK_REMOVE, world, position);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      tracker.updateBlockState(server, this.getIdentifier(), Blocks.AIR.getDefaultState());
   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class BlockRemoveTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;

      public BlockRemoveTimeLine.BlockRemoveTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public BlockRemoveTimeLine.BlockRemoveTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public BlockRemoveTimeLine build() {
         return new BlockRemoveTimeLine(this.world, this.position);
      }
   }

   public static class BlockRemoveTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final BlockRemoveTimeLine.BlockRemoveTimeLineFactory INSTANCE = new BlockRemoveTimeLine.BlockRemoveTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_UPDATE_V1, TimeLineType.BLOCK_UPDATE_V2, TimeLineType.BLOCK_BREAK, TimeLineType.BLOCK_REMOVE};
      }

      public BlockRemoveTimeLine.BlockRemoveTimeLineBuilder getBuilder() {
         return new BlockRemoveTimeLine.BlockRemoveTimeLineBuilder();
      }

      public BlockRemoveTimeLine getFromBytes(ByteBuffer buffer) {
         return new BlockRemoveTimeLine(WorldTypes.values()[buffer.get()], buffer.getLong());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
         if (tracker.blockStateCache.containsKey(param)) {
            tracker.updateBlockState(server, param, (BlockState)tracker.blockStateCache.get(param), false);
         }

      }
   }
}
