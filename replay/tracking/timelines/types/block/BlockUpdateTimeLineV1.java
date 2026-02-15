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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.fluid.Fluids;

/** @deprecated */
@Deprecated
public class BlockUpdateTimeLineV1 extends PositionITimeLine<WorldPosIIdentifier> {
   private final int rawBlock;
   private final BlockState block;
   private final BlockUpdateTimeLineV1.EventType eventType;
   private final byte stateValue;
   private final byte dataValue;

   protected BlockUpdateTimeLineV1(WorldTypes world, long position, int block, byte eventData) {
      super(TimeLineType.BLOCK_UPDATE_V1, world, position);
      this.rawBlock = block;
      this.block = Block.getStateFromRawId(block);
      this.eventType = BlockUpdateTimeLineV1.EventType.values()[eventData % 2];
      this.stateValue = (byte)(eventData / 2);
      this.dataValue = eventData;
   }

   public BlockState getBlock() {
      return this.block;
   }

   public WorldPosIIdentifier getIdentifier() {
      return new WorldPosIIdentifier(this.getWorld(), this.getBlockPos());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ServerWorld world = this.getWorld().toWorld(server);
      BlockState blockState = (BlockState)Optional.ofNullable(getUpdateType(this.getEventType(), this.getBlock(), (BlockPos)null, (LivingEntity)null)).map((updateEvent) -> {
         return updateEvent.setter.update(this.getStateValue(), this.getBlock());
      }).orElse(this.getBlock());
      boolean result = tracker.updateBlockState(server, this.getIdentifier(), blockState);
      if (result) {
         ReplayEntityTracker<ReplayPlayerEntity> playerTracker = tracker.getReplayPlayerTracker().getEntityTracker();
         if (!silence && playerTracker.isVisible()) {
            if (this.getEventType() == BlockUpdateTimeLineV1.EventType.PLACE) {
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
      return ByteBuffer.allocate(superBuffer.remaining() + 5).put(superBuffer).putInt(this.rawBlock).put(this.getDataValue());
   }

   public BlockUpdateTimeLineV1.EventType getEventType() {
      return this.eventType;
   }

   public byte getStateValue() {
      return this.stateValue;
   }

   public byte getDataValue() {
      return this.dataValue;
   }

   /** @deprecated */
   @Deprecated
   public static BlockUpdateTimeLineV1.UpdateEvent getUpdateType(BlockUpdateTimeLineV1.EventType eventType, BlockState state, @Nullable BlockPos blockPos, @Nullable LivingEntity entity) {
      Supplier<Boolean> shouldUpdate = () -> {
         return entity == null && blockPos == null || entity != null && blockPos != null && entity.getBlockPos().isWithinDistance(blockPos, 32.0D);
      };
      if (state.getBlock() == Blocks.AIR && eventType == BlockUpdateTimeLineV1.EventType.UPDATE) {
         return BlockUpdateTimeLineV1.UpdateEvent.AIR;
      } else if (state.getBlock() instanceof AbstractFireBlock && eventType == BlockUpdateTimeLineV1.EventType.UPDATE) {
         return BlockUpdateTimeLineV1.UpdateEvent.FIRE;
      } else if (state.getBlock() == Blocks.END_PORTAL && eventType == BlockUpdateTimeLineV1.EventType.UPDATE) {
         return BlockUpdateTimeLineV1.UpdateEvent.END_PORTAL;
      } else if (state.getBlock() == Blocks.END_PORTAL_FRAME && eventType == BlockUpdateTimeLineV1.EventType.UPDATE) {
         return BlockUpdateTimeLineV1.UpdateEvent.END_PORTAL_FRAME;
      } else if (state.getBlock() == Blocks.RESPAWN_ANCHOR && eventType == BlockUpdateTimeLineV1.EventType.UPDATE) {
         return BlockUpdateTimeLineV1.UpdateEvent.RESPAWN_ANCHOR_UPDATE;
      } else if (state.getBlock() instanceof BedBlock) {
         return BlockUpdateTimeLineV1.UpdateEvent.BED_UPDATE;
      } else if (state.getBlock() instanceof DoorBlock && (Boolean)shouldUpdate.get()) {
         return BlockUpdateTimeLineV1.UpdateEvent.DOOR_UPDATE;
      } else if (state.getBlock() instanceof FluidBlock && eventType == BlockUpdateTimeLineV1.EventType.PLACE) {
         return BlockUpdateTimeLineV1.UpdateEvent.FLUID;
      } else if (state.getBlock() == Blocks.OBSIDIAN && eventType == BlockUpdateTimeLineV1.EventType.PLACE) {
         return BlockUpdateTimeLineV1.UpdateEvent.OBSIDIAN;
      } else {
         return state.getBlock() == Blocks.OBSIDIAN && eventType == BlockUpdateTimeLineV1.EventType.UPDATE && (Boolean)shouldUpdate.get() ? BlockUpdateTimeLineV1.UpdateEvent.OBSIDIAN : null;
      }
   }

   /** @deprecated */
   @Deprecated
   public static enum EventType {
      PLACE,
      UPDATE;

      // $FF: synthetic method
      private static BlockUpdateTimeLineV1.EventType[] $values() {
         return new BlockUpdateTimeLineV1.EventType[]{PLACE, UPDATE};
      }
   }

   /** @deprecated */
   @Deprecated
   public static enum UpdateEvent {
      FIRE((state) -> {
         return 0;
      }, (value, state) -> {
         return state.getBlock() == Blocks.NETHER_PORTAL ? state : Blocks.FIRE.getDefaultState();
      }),
      BED_UPDATE((state) -> {
         return (byte)(((BedPart)state.get(BedBlock.PART)).ordinal() + ((Direction)state.get(BedBlock.FACING)).getHorizontal() * 2);
      }, (value, state) -> {
         return (BlockState)((BlockState)Blocks.WHITE_BED.getDefaultState().with(BedBlock.PART, BedPart.values()[value % 2])).with(BedBlock.FACING, Direction.fromHorizontal(value / 2));
      }),
      DOOR_UPDATE((state) -> {
         return (byte)(((Direction)state.get(DoorBlock.FACING)).getHorizontal() + ((DoorHinge)state.get(DoorBlock.HINGE)).ordinal() * 4 + ((DoubleBlockHalf)state.get(DoorBlock.HALF)).ordinal() * 8 + ((Boolean)state.get(DoorBlock.OPEN) ? 0 : 16));
      }, (value, state) -> {
         int intValue = value;
         BlockState result = state == null ? Blocks.OAK_DOOR.getDefaultState() : state;
         result = (BlockState)result.with(DoorBlock.OPEN, intValue >= 16);
         if (intValue >= 16) {
            intValue -= 16;
         }

         result = (BlockState)result.with(DoorBlock.HALF, intValue >= 8 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
         if (intValue >= 8) {
            intValue -= 8;
         }

         result = (BlockState)result.with(DoorBlock.HINGE, intValue >= 4 ? DoorHinge.RIGHT : DoorHinge.LEFT);
         if (intValue >= 4) {
            intValue -= 4;
         }

         return (BlockState)result.with(DoorBlock.FACING, Direction.fromHorizontal(intValue));
      }),
      END_PORTAL_FRAME((state) -> {
         return (byte)((Boolean)state.get(EndPortalFrameBlock.EYE) ? 1 : 0);
      }, (value, state) -> {
         return (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.EYE, value != 0);
      }),
      END_PORTAL((state) -> {
         return 0;
      }, (value, state) -> {
         return Blocks.END_PORTAL.getDefaultState();
      }),
      RESPAWN_ANCHOR_UPDATE((state) -> {
         return ((Integer)state.get(RespawnAnchorBlock.CHARGES)).byteValue();
      }, (value, state) -> {
         return (BlockState)Blocks.RESPAWN_ANCHOR.getDefaultState().with(RespawnAnchorBlock.CHARGES, value.intValue());
      }),
      AIR((state) -> {
         return 0;
      }, (value, state) -> {
         return Blocks.AIR.getDefaultState();
      }),
      OBSIDIAN((state) -> {
         return 0;
      }, (value, state) -> {
         return Blocks.OBSIDIAN.getDefaultState();
      }),
      FLUID((state) -> {
         return (byte)(state.getBlock() == Blocks.WATER ? 0 : 1);
      }, (value, state) -> {
         return value == 0 ? Fluids.WATER.getDefaultState().getBlockState() : Fluids.LAVA.getDefaultState().getBlockState();
      });

      private final Function<BlockState, Byte> getter;
      private final BlockUpdateTimeLineV1.UpdateEvent.StateUpdater setter;

      private UpdateEvent(Function<BlockState, Byte> getter, BlockUpdateTimeLineV1.UpdateEvent.StateUpdater setter) {
         this.getter = getter;
         this.setter = setter;
      }

      public byte getByte(BlockUpdateTimeLineV1.EventType eventType, BlockState blockState) {
         return (byte)((Byte)this.getter.apply(blockState) * 2 + eventType.ordinal());
      }

      // $FF: synthetic method
      private static BlockUpdateTimeLineV1.UpdateEvent[] $values() {
         return new BlockUpdateTimeLineV1.UpdateEvent[]{FIRE, BED_UPDATE, DOOR_UPDATE, END_PORTAL_FRAME, END_PORTAL, RESPAWN_ANCHOR_UPDATE, AIR, OBSIDIAN, FLUID};
      }

      private interface StateUpdater {
         BlockState update(Byte var1, BlockState var2);
      }
   }

   /** @deprecated */
   @Deprecated
   public static class BlockUpdateTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private long position;
      private int block;
      private byte dataValue;

      public BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder setPosition(BlockPos position) {
         this.position = position.asLong();
         return this;
      }

      public BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder setBlock(BlockState block) {
         this.block = Block.getRawIdFromState(block);
         return this;
      }

      public BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder setDataValue(byte dataValue) {
         this.dataValue = dataValue;
         return this;
      }

      public BlockUpdateTimeLineV1 build() {
         return new BlockUpdateTimeLineV1(this.world, this.position, this.block, this.dataValue);
      }
   }

   /** @deprecated */
   @Deprecated
   public static class BlockUpdateTimeLineFactory implements TimeLineFactorySingleton<WorldPosIIdentifier> {
      public static final BlockUpdateTimeLineV1.BlockUpdateTimeLineFactory INSTANCE = new BlockUpdateTimeLineV1.BlockUpdateTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.BLOCK_BREAK, TimeLineType.BLOCK_UPDATE_V1, TimeLineType.BLOCK_UPDATE_V2, TimeLineType.BLOCK_REMOVE};
      }

      public BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder getBuilder() {
         return new BlockUpdateTimeLineV1.BlockUpdateTimeLineBuilder();
      }

      public BlockUpdateTimeLineV1 getFromBytes(ByteBuffer buffer) {
         return new BlockUpdateTimeLineV1(WorldTypes.values()[buffer.get()], buffer.getLong(), buffer.getInt(), buffer.get());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldPosIIdentifier param) {
         if (tracker.blockStateCache.containsKey(param)) {
            tracker.updateBlockState(server, param, (BlockState)tracker.blockStateCache.get(param), false);
         }

      }
   }
}
