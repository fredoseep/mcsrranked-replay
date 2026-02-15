package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class PlayerPauseTimeLine extends TimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();
   private final boolean pause;

   public PlayerPauseTimeLine(boolean pause) {
      super(TimeLineType.PLAYER_PAUSE);
      this.pause = pause;
   }

   public boolean isPause() {
      return this.pause;
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)(this.isPause() ? 1 : 0));
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public boolean isPlayerMovementTimeline() {
      return true;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
      state.setPause(this.isPause());
   }

   public static class PlayerPauseTimeLineBuilder implements TimeLineBuilder {
      private boolean pause;

      public PlayerPauseTimeLine.PlayerPauseTimeLineBuilder setPause(boolean pause) {
         this.pause = pause;
         return this;
      }

      public PlayerPauseTimeLine build() {
         return new PlayerPauseTimeLine(this.pause);
      }
   }

   public static class PlayerPauseTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerPauseTimeLine.PlayerPauseTimeLineFactory INSTANCE = new PlayerPauseTimeLine.PlayerPauseTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_PAUSE};
      }

      public PlayerPauseTimeLine.PlayerPauseTimeLineBuilder getBuilder() {
         return new PlayerPauseTimeLine.PlayerPauseTimeLineBuilder();
      }

      public PlayerPauseTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerPauseTimeLine(buffer.get() != 0);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
