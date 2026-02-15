package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public abstract class TimeLine<T extends Identifier> {
   private final TimeLineType type;

   protected TimeLine(TimeLineType type) {
      this.type = type;
   }

   public abstract ByteBuffer toBytes();

   public abstract T getIdentifier();

   public final TimeLineType getType() {
      return this.type;
   }

   public abstract void runTimeLine(OpponentPlayerTracker var1, MinecraftServer var2, boolean var3);

   public void onInit(OpponentPlayerTracker tracker, int tick) {
   }

   public boolean isPlayerMovementTimeline() {
      return false;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
   }
}
