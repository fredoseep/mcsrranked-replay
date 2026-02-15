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

public class PlayerRideTimeLine extends TimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();
   private final boolean riding;

   public boolean isRiding() {
      return this.riding;
   }

   protected PlayerRideTimeLine(boolean riding) {
      super(TimeLineType.PLAYER_RIDE);
      this.riding = riding;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public boolean isPlayerMovementTimeline() {
      return true;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
      state.setRidingBoat(this.isRiding());
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)(this.riding ? 1 : 0));
   }

   public static class PlayerRideTimeLineBuilder implements TimeLineBuilder {
      private boolean riding;

      public PlayerRideTimeLine.PlayerRideTimeLineBuilder setRiding(boolean riding) {
         this.riding = riding;
         return this;
      }

      public PlayerRideTimeLine build() {
         return new PlayerRideTimeLine(this.riding);
      }
   }

   public static class PlayerRideTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerRideTimeLine.PlayerRideTimeLineFactory INSTANCE = new PlayerRideTimeLine.PlayerRideTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_RIDE};
      }

      public PlayerRideTimeLine.PlayerRideTimeLineBuilder getBuilder() {
         return new PlayerRideTimeLine.PlayerRideTimeLineBuilder();
      }

      public PlayerRideTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerRideTimeLine(buffer.get() != 0);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
