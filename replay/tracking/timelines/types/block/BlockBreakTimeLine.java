package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionITimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldPosIIdentifier;
import java.nio.ByteBuffer;
import java.util.Iterator;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class BlockBreakTimeLine extends PositionITimeLine<WorldPosIIdentifier> {
   protected BlockBreakTimeLine(WorldTypes world, long position) {
      super(TimeLineType.BLOCK_BREAK, world, position);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ServerWorld serverWorld = this.getWorld().toWorld(server);
      if (serverWorld.isChunkLoaded(this.getBlockPos())) {
         tracker.blockStateCache.putIfAbsent(this.getIdentifier(), serverWorld.getBlockState(this.getBlockPos()));
         ReplayEntityTracker<ReplayPlayerEntity> playerTracker = tracker.getReplayPlayerTracker().getEntityTracker();
         if (!silence && playerTracker.isVisible()) {
            serverWorld.breakBlock(this.getBlockPos(), false);
            ((ReplayPlayerEntity)playerTracker.getTarget()).swingHand(((ReplayPlayerEntity)playerTracker.getTarget()).getActiveHand());
            Iterator var6 = this.getWorld().toWorld(server).getPlayers().iterator();

            while(var6.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
               serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(serverPlayerEntity.getEntityId(), this.getBlockPos(), -1));
            }
         } else {
            serverWorld.removeBlock(this.getBlockPos(), false);
         }
      } else {
         tracker.updateBlockState(server, this.getIdentifier(), Blocks.BARRIER.getDefaultState());
      }

   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class BlockBreakTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;

      public BlockBreakTimeLine.BlockBreakTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public BlockBreakTimeLine.BlockBreakTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public BlockBreakTimeLine build() {
         return new BlockBreakTimeLine(this.world, this.position);
      }
   }

   public static class BlockBreakTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final BlockBreakTimeLine.BlockBreakTimeLineFactory INSTANCE = new BlockBreakTimeLine.BlockBreakTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_UPDATE_V1, TimeLineType.BLOCK_UPDATE_V2, TimeLineType.BLOCK_BREAK, TimeLineType.BLOCK_REMOVE};
      }

      public BlockBreakTimeLine.BlockBreakTimeLineBuilder getBuilder() {
         return new BlockBreakTimeLine.BlockBreakTimeLineBuilder();
      }

      public BlockBreakTimeLine getFromBytes(ByteBuffer buffer) {
         return new BlockBreakTimeLine(WorldTypes.values()[buffer.get()], buffer.getLong());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
         if (tracker.blockStateCache.containsKey(param)) {
            tracker.updateBlockState(server, param, (BlockState)tracker.blockStateCache.get(param), false);
         }

      }
   }
}
