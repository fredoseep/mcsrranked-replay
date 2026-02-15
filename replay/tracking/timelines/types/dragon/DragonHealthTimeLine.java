package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.CustomIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class DragonHealthTimeLine extends TimeLine<CustomIdentifier> {
   private static final byte BYTE = 0;
   private final int health;

   protected DragonHealthTimeLine(int health) {
      super(TimeLineType.DRAGON_HEALTH_UPDATE);
      this.health = health;
   }

   public int getHealth() {
      return this.health;
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put((byte)(this.getHealth() - 128));
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public CustomIdentifier getIdentifier() {
      return new CustomIdentifier((byte)0);
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      tracker.getDragonFight().onUpdateHealth(tick, this.getHealth());
   }

   public static class DragonHealthTimeLineBuilder implements TimeLineBuilder {
      private int health;

      public DragonHealthTimeLine.DragonHealthTimeLineBuilder setHealth(int health) {
         this.health = health;
         return this;
      }

      public DragonHealthTimeLine build() {
         return new DragonHealthTimeLine(this.health);
      }
   }

   public static class DragonHealthTimeLineFactory implements TimeLineFactorySingleton<CustomIdentifier> {
      public static final DragonHealthTimeLine.DragonHealthTimeLineFactory INSTANCE = new DragonHealthTimeLine.DragonHealthTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[0];
      }

      public DragonHealthTimeLine.DragonHealthTimeLineBuilder getBuilder() {
         return new DragonHealthTimeLine.DragonHealthTimeLineBuilder();
      }

      public DragonHealthTimeLine getFromBytes(ByteBuffer buffer) {
         return new DragonHealthTimeLine(buffer.get() + 128);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, CustomIdentifier param) {
      }
   }
}
