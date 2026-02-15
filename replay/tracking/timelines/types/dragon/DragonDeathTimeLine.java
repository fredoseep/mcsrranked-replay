package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.CustomIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class DragonDeathTimeLine extends TimeLine<CustomIdentifier> {
   private static final byte BYTE = 3;

   protected DragonDeathTimeLine() {
      super(TimeLineType.DRAGON_DEATH);
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)0);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public CustomIdentifier getIdentifier() {
      return new CustomIdentifier((byte)3);
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      tracker.getDragonFight().onUpdateHealth(tick, -1);
   }

   public static class DragonHealthTimeLineBuilder implements TimeLineBuilder {
      public DragonDeathTimeLine build() {
         return new DragonDeathTimeLine();
      }
   }

   public static class DragonDeathTimeLineFactory implements TimeLineFactorySingleton<CustomIdentifier> {
      public static final DragonDeathTimeLine.DragonDeathTimeLineFactory INSTANCE = new DragonDeathTimeLine.DragonDeathTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[0];
      }

      public DragonDeathTimeLine.DragonHealthTimeLineBuilder getBuilder() {
         return new DragonDeathTimeLine.DragonHealthTimeLineBuilder();
      }

      public DragonDeathTimeLine getFromBytes(ByteBuffer buffer) {
         buffer.get();
         return new DragonDeathTimeLine();
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, CustomIdentifier param) {
      }
   }
}
