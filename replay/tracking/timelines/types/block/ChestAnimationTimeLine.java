package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block;

import com.mcsrranked.client.anticheat.replay.render.ChestAnimationModifier;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionITimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldPosIIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ChestAnimationTimeLine extends PositionITimeLine<WorldPosIIdentifier> {
   private final boolean open;

   protected ChestAnimationTimeLine(WorldTypes world, long position, boolean open) {
      super(TimeLineType.CHEST_ANIMATION, world, position);
      this.open = open;
   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!silence) {
         ServerWorld world = this.getWorld().toWorld(server);
         if (world.isChunkLoaded(this.getBlockPos())) {
            BlockEntity blockEntity = world.getBlockEntity(this.getBlockPos());
            if (blockEntity instanceof ChestBlockEntity) {
               ((ChestAnimationModifier)blockEntity).ranked$setOpen(this.open);
            }
         }
      }

   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 1).put(superBuffer).put((byte)(this.open ? 1 : 0));
   }

   public static class ChestAnimationTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;
      private boolean open;

      public ChestAnimationTimeLine.ChestAnimationTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public ChestAnimationTimeLine.ChestAnimationTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public ChestAnimationTimeLine.ChestAnimationTimeLineBuilder setOpen(boolean open) {
         this.open = open;
         return this;
      }

      public ChestAnimationTimeLine build() {
         return new ChestAnimationTimeLine(this.world, this.position, this.open);
      }
   }

   public static class ChestAnimationTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final ChestAnimationTimeLine.ChestAnimationTimeLineFactory INSTANCE = new ChestAnimationTimeLine.ChestAnimationTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_UPDATE_V1, TimeLineType.BLOCK_UPDATE_V2, TimeLineType.BLOCK_BREAK, TimeLineType.BLOCK_REMOVE};
      }

      public ChestAnimationTimeLine.ChestAnimationTimeLineBuilder getBuilder() {
         return new ChestAnimationTimeLine.ChestAnimationTimeLineBuilder();
      }

      public ChestAnimationTimeLine getFromBytes(ByteBuffer buffer) {
         return new ChestAnimationTimeLine(WorldTypes.values()[buffer.get()], buffer.getLong(), buffer.get() != 0);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
         ServerWorld world = param.getWorld().toWorld(server);
         if (world.isChunkLoaded(new BlockPos(param.getPos()))) {
            BlockEntity blockEntity = world.getBlockEntity(new BlockPos(param.getPos()));
            if (blockEntity instanceof ChestBlockEntity) {
               ((ChestAnimationModifier)blockEntity).ranked$setOpen(false);
            }
         }

      }
   }
}
