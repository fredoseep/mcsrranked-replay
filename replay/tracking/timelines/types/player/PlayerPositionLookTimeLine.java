package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.RotationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class PlayerPositionLookTimeLine extends RotationTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();

   public PlayerPositionLookTimeLine(WorldTypes world, short yaw, short pitch) {
      super(TimeLineType.PLAYER_POSITION_LOOK, world, yaw, pitch);
   }

   protected PlayerPositionLookTimeLine(WorldTypes world, float yaw, float pitch) {
      super(TimeLineType.PLAYER_POSITION_LOOK, world, yaw, pitch);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public boolean isPlayerMovementTimeline() {
      return true;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
      state.setWorldType(this.getWorld());
      state.setPitch(this.getPitch());
      state.setYaw(this.getYaw());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class PlayerPositionLookTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private float yaw;
      private float pitch;

      public PlayerPositionLookTimeLineBuilder() {
         this.world = WorldTypes.OVERWORLD;
         this.yaw = 0.0F;
         this.pitch = 0.0F;
      }

      public PlayerPositionLookTimeLine.PlayerPositionLookTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public PlayerPositionLookTimeLine.PlayerPositionLookTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public PlayerPositionLookTimeLine.PlayerPositionLookTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public PlayerPositionLookTimeLine build() {
         return new PlayerPositionLookTimeLine(this.world, this.yaw, this.pitch);
      }
   }

   public static class PlayerPositionLookTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerPositionLookTimeLine.PlayerPositionLookTimeLineFactory INSTANCE = new PlayerPositionLookTimeLine.PlayerPositionLookTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_POSITION_LOOK};
      }

      public PlayerPositionLookTimeLine.PlayerPositionLookTimeLineBuilder getBuilder() {
         return new PlayerPositionLookTimeLine.PlayerPositionLookTimeLineBuilder();
      }

      public PlayerPositionLookTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerPositionLookTimeLine(WorldTypes.values()[buffer.get()], buffer.getShort(), buffer.getShort());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
