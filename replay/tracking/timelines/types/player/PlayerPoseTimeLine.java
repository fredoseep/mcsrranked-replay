package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.entity.EntityPose;
import net.minecraft.server.MinecraftServer;

public class PlayerPoseTimeLine extends TimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();
   private final byte pose;

   protected PlayerPoseTimeLine(byte pose) {
      super(TimeLineType.PLAYER_POSE);
      this.pose = pose;
   }

   public EntityPose getPose() {
      return EntityPose.values()[this.pose];
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
      state.setPose(this.getPose());
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put(this.pose);
   }

   public static class PlayerPoseTimeLineBuilder implements TimeLineBuilder {
      private byte pose;

      public PlayerPoseTimeLine.PlayerPoseTimeLineBuilder setPose(byte pose) {
         this.pose = pose;
         return this;
      }

      public PlayerPoseTimeLine build() {
         return new PlayerPoseTimeLine(this.pose);
      }
   }

   public static class PlayerPoseTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerPoseTimeLine.PlayerPoseTimeLineFactory INSTANCE = new PlayerPoseTimeLine.PlayerPoseTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_POSE};
      }

      public PlayerPoseTimeLine.PlayerPoseTimeLineBuilder getBuilder() {
         return new PlayerPoseTimeLine.PlayerPoseTimeLineBuilder();
      }

      public PlayerPoseTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerPoseTimeLine(buffer.get());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
