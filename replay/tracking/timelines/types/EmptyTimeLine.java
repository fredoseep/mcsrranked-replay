package com.mcsrranked.client.anticheat.replay.tracking.timelines.types;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class EmptyTimeLine extends TimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();

   protected EmptyTimeLine() {
      super(TimeLineType.EMPTY);
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)0);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public static class EmptyTimeLineBuilder implements TimeLineBuilder {
      public EmptyTimeLine build() {
         return new EmptyTimeLine();
      }
   }

   public static class EmptyTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final EmptyTimeLine.EmptyTimeLineFactory INSTANCE = new EmptyTimeLine.EmptyTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.EMPTY};
      }

      public EmptyTimeLine.EmptyTimeLineBuilder getBuilder() {
         return new EmptyTimeLine.EmptyTimeLineBuilder();
      }

      public EmptyTimeLine getFromBytes(ByteBuffer buffer) {
         buffer.get();
         return new EmptyTimeLine();
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
