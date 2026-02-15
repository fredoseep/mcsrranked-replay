package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.google.common.collect.Maps;
import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.WorldTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;
import java.util.Map;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class PlayerRemoveTimeLine extends WorldTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();
   private final boolean death;

   protected PlayerRemoveTimeLine(WorldTypes world, boolean death) {
      super(TimeLineType.PLAYER_REMOVE, world);
      this.death = death;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.capacity() + 1).put(superBuffer).put((byte)(this.death ? 1 : 0));
   }

   public boolean isDeath() {
      return this.death;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ReplayEntityTracker<ReplayPlayerEntity> playerTracker = tracker.getReplayPlayerTracker().getEntityTracker();
      ((ReplayPlayerEntity)playerTracker.getTarget()).clearInventory();
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      if (this.isDeath()) {
         tracker.playerInventoryCache.putIfAbsent(tick, Maps.newHashMap());

         for(int i = 0; i < EquipmentSlot.values().length; ++i) {
            ((Map)tracker.playerInventoryCache.get(tick)).put((byte)i, ItemStack.EMPTY);
         }
      }

   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public boolean isPlayerMovementTimeline() {
      return true;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
      if (this.isDeath()) {
         state.setDeath(true);
      } else {
         state.setVisible(false);
      }

   }

   public static class PlayerRemoveTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private boolean death;

      public PlayerRemoveTimeLine.PlayerRemoveTimeLineBuilder setDeath(boolean death) {
         this.death = death;
         return this;
      }

      public PlayerRemoveTimeLine.PlayerRemoveTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public PlayerRemoveTimeLine build() {
         return new PlayerRemoveTimeLine(this.world, this.death);
      }
   }

   public static class PlayerRemoveTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerRemoveTimeLine.PlayerRemoveTimeLineFactory INSTANCE = new PlayerRemoveTimeLine.PlayerRemoveTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_SPAWN, TimeLineType.PLAYER_REMOVE, TimeLineType.PLAYER_POSITION, TimeLineType.PLAYER_POSITION_LOOK, TimeLineType.PLAYER_POSITION_POS};
      }

      public PlayerRemoveTimeLine.PlayerRemoveTimeLineBuilder getBuilder() {
         return new PlayerRemoveTimeLine.PlayerRemoveTimeLineBuilder();
      }

      public PlayerRemoveTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerRemoveTimeLine(WorldTypes.values()[buffer.get()], buffer.get() == 1);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
