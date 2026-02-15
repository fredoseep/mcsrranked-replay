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

import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BlockProgressTimeLine extends PositionITimeLine<WorldPosIIdentifier> {
   private final byte progress;

   protected BlockProgressTimeLine(WorldTypes world, long position, byte progress) {
      super(TimeLineType.BLOCK_PROGRESS, world, position);
      this.progress = progress;
   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public int getProgress() {
      return this.progress;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!silence) {
         ReplayEntityTracker<ReplayPlayerEntity> playerTracker = tracker.getReplayPlayerTracker().getEntityTracker();
         if (playerTracker.isVisible()) {
            ((ReplayPlayerEntity)playerTracker.getTarget()).swingHand(((ReplayPlayerEntity)playerTracker.getTarget()).getActiveHand());
            Iterator var5 = this.getWorld().toWorld(server).getPlayers().iterator();

            while(var5.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
               serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(serverPlayerEntity.getEntityId(), this.getBlockPos(), this.getProgress()));
            }
         }
      }

   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 1).put(superBuffer).put(this.progress);
   }

   public static class BlockProgressTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;
      private byte progress;

      public BlockProgressTimeLine.BlockProgressTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public BlockProgressTimeLine.BlockProgressTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public BlockProgressTimeLine.BlockProgressTimeLineBuilder setProgress(byte progress) {
         this.progress = progress;
         return this;
      }

      public BlockProgressTimeLine build() {
         return new BlockProgressTimeLine(this.world, this.position, this.progress);
      }
   }

   public static class BlockProgressTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final BlockProgressTimeLine.BlockProgressTimeLineFactory INSTANCE = new BlockProgressTimeLine.BlockProgressTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_PROGRESS};
      }

      public BlockProgressTimeLine.BlockProgressTimeLineBuilder getBuilder() {
         return new BlockProgressTimeLine.BlockProgressTimeLineBuilder();
      }

      public BlockProgressTimeLine getFromBytes(ByteBuffer buffer) {
         return new BlockProgressTimeLine(WorldTypes.values()[buffer.get()], buffer.getLong(), buffer.get());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
      }
   }
}
