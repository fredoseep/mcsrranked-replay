package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionFTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.MinecraftServer;

public class PlayerPositionPosTimeLine extends PositionFTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();

   protected PlayerPositionPosTimeLine(WorldTypes world, Vector3f position) {
      super(TimeLineType.PLAYER_POSITION_POS, world, position);
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
      state.setPos(this.getPosition());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class PlayerPositionPosTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;

      public PlayerPositionPosTimeLineBuilder() {
         this.world = WorldTypes.OVERWORLD;
         this.position = new Vector3f(0.0F, 0.0F, 0.0F);
      }

      public PlayerPositionPosTimeLine.PlayerPositionPosTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public PlayerPositionPosTimeLine.PlayerPositionPosTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public PlayerPositionPosTimeLine.PlayerPositionPosTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public PlayerPositionPosTimeLine build() {
         return new PlayerPositionPosTimeLine(this.world, this.position);
      }
   }

   public static class PlayerPositionPosTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerPositionPosTimeLine.PlayerPositionPosTimeLineFactory INSTANCE = new PlayerPositionPosTimeLine.PlayerPositionPosTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_POSITION_POS};
      }

      public PlayerPositionPosTimeLine.PlayerPositionPosTimeLineBuilder getBuilder() {
         return new PlayerPositionPosTimeLine.PlayerPositionPosTimeLineBuilder();
      }

      public PlayerPositionPosTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerPositionPosTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
