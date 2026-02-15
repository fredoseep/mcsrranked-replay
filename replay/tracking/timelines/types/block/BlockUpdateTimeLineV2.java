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
import java.util.function.Supplier;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.sound.SoundCategory;

public class BlockUpdateTimeLineV2 extends PositionITimeLine<WorldPosIIdentifier> {
   private final int rawBlock;
   private final BlockState block;
   private final BlockUpdateTimeLineV2.EventType eventType;

   protected BlockUpdateTimeLineV2(WorldTypes world, long position, int block, byte placed) {
      super(TimeLineType.BLOCK_UPDATE_V2, world, position);
      this.rawBlock = block;
      this.block = Block.getStateFromRawId(block);
      this.eventType = BlockUpdateTimeLineV2.EventType.values()[placed];
   }

   public BlockState getBlock() {
      return this.block;
   }

   public BlockUpdateTimeLineV2.EventType getEventType() {
      return this.eventType;
   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ServerWorld world = this.getWorld().toWorld(server);
      BlockState blockState = this.getBlock();
      boolean result = tracker.updateBlockState(server, this.getIdentifier(), blockState);
      if (result) {
         ReplayEntityTracker<ReplayPlayerEntity> playerTracker = tracker.getReplayPlayerTracker().getEntityTracker();
         if (!silence && playerTracker.isVisible()) {
            if (this.getEventType() == BlockUpdateTimeLineV2.EventType.PLACE) {
               ((ReplayPlayerEntity)playerTracker.getTarget()).swingHand(((ReplayPlayerEntity)playerTracker.getTarget()).getActiveHand());
               BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
               world.playSound((PlayerEntity)null, this.getBlockPos(), blockState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
            }

            Iterator var10 = world.getPlayers().iterator();

            while(var10.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var10.next();
               serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(serverPlayerEntity.getEntityId(), this.getBlockPos(), -1));
            }
         }
      }

   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 5).put(superBuffer).putInt(this.rawBlock).put((byte)this.getEventType().ordinal());
   }

   public static BlockUpdateTimeLineV2.UpdateEvent getUpdateType(BlockUpdateTimeLineV2.EventType eventType, BlockState state, @Nullable BlockPos blockPos, @Nullable LivingEntity entity) {
      Supplier<Boolean> shouldUpdate = () -> {
         return entity == null && blockPos == null || entity != null && blockPos != null && entity.getBlockPos().isWithinDistance(blockPos, 32.0D);
      };
      if (state.getBlock() == Blocks.AIR && eventType == BlockUpdateTimeLineV2.EventType.UPDATE) {
         return BlockUpdateTimeLineV2.UpdateEvent.AIR;
      } else if (state.getBlock() instanceof AbstractFireBlock && eventType == BlockUpdateTimeLineV2.EventType.UPDATE) {
         return BlockUpdateTimeLineV2.UpdateEvent.FIRE;
      } else if (state.getBlock() == Blocks.END_PORTAL && eventType == BlockUpdateTimeLineV2.EventType.UPDATE) {
         return BlockUpdateTimeLineV2.UpdateEvent.END_PORTAL;
      } else if (state.getBlock() == Blocks.END_PORTAL_FRAME && eventType == BlockUpdateTimeLineV2.EventType.UPDATE) {
         return BlockUpdateTimeLineV2.UpdateEvent.END_PORTAL_FRAME;
      } else if (state.getBlock() == Blocks.RESPAWN_ANCHOR && eventType == BlockUpdateTimeLineV2.EventType.UPDATE) {
         return BlockUpdateTimeLineV2.UpdateEvent.RESPAWN_ANCHOR_UPDATE;
      } else if (state.getBlock() instanceof BedBlock) {
         return BlockUpdateTimeLineV2.UpdateEvent.BED_UPDATE;
      } else if (state.getBlock() instanceof DoorBlock && (Boolean)shouldUpdate.get()) {
         return BlockUpdateTimeLineV2.UpdateEvent.DOOR_UPDATE;
      } else if (state.getBlock() instanceof FluidBlock && eventType == BlockUpdateTimeLineV2.EventType.PLACE) {
         return BlockUpdateTimeLineV2.UpdateEvent.FLUID;
      } else if (state.getBlock() == Blocks.OBSIDIAN && eventType == BlockUpdateTimeLineV2.EventType.PLACE) {
         return BlockUpdateTimeLineV2.UpdateEvent.OBSIDIAN;
      } else {
         return state.getBlock() == Blocks.OBSIDIAN && eventType == BlockUpdateTimeLineV2.EventType.UPDATE && (Boolean)shouldUpdate.get() ? BlockUpdateTimeLineV2.UpdateEvent.OBSIDIAN : null;
      }
   }

   public static enum EventType {
      PLACE,
      UPDATE;

      // $FF: synthetic method
      private static BlockUpdateTimeLineV2.EventType[] $values() {
         return new BlockUpdateTimeLineV2.EventType[]{PLACE, UPDATE};
      }
   }

   public static enum UpdateEvent {
      FIRE,
      BED_UPDATE,
      DOOR_UPDATE,
      END_PORTAL_FRAME,
      END_PORTAL,
      RESPAWN_ANCHOR_UPDATE,
      AIR,
      OBSIDIAN,
      FLUID;

      // $FF: synthetic method
      private static BlockUpdateTimeLineV2.UpdateEvent[] $values() {
         return new BlockUpdateTimeLineV2.UpdateEvent[]{FIRE, BED_UPDATE, DOOR_UPDATE, END_PORTAL_FRAME, END_PORTAL, RESPAWN_ANCHOR_UPDATE, AIR, OBSIDIAN, FLUID};
      }
   }

   public static class BlockUpdateTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;
      private int block;
      private byte placed;

      public BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder setBlock(BlockState block) {
         this.block = Block.getRawIdFromState(block);
         return this;
      }

      public BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder setPlaced(BlockUpdateTimeLineV2.EventType eventType) {
         this.placed = (byte)eventType.ordinal();
         return this;
      }

      public BlockUpdateTimeLineV2 build() {
         return new BlockUpdateTimeLineV2(this.world, this.position, this.block, this.placed);
      }
   }

   public static class BlockUpdateTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final BlockUpdateTimeLineV2.BlockUpdateTimeLineFactory INSTANCE = new BlockUpdateTimeLineV2.BlockUpdateTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_BREAK, TimeLineType.BLOCK_UPDATE_V1, TimeLineType.BLOCK_UPDATE_V2, TimeLineType.BLOCK_REMOVE};
      }

      public BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder getBuilder() {
         return new BlockUpdateTimeLineV2.BlockUpdateTimeLineBuilder();
      }

      public BlockUpdateTimeLineV2 getFromBytes(ByteBuffer buffer) {
         return new BlockUpdateTimeLineV2(WorldTypes.values()[buffer.get()], buffer.getLong(), buffer.getInt(), buffer.get());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
         if (tracker.blockStateCache.containsKey(param)) {
            tracker.updateBlockState(server, param, (BlockState)tracker.blockStateCache.get(param), false);
         }

      }
   }
}
