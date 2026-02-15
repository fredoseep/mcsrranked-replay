package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.CustomIdentifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public class CrystalDestroyTimeLine extends TimeLine<CustomIdentifier> {
   private static final byte BYTE = 1;
   private final byte index;

   protected CrystalDestroyTimeLine(byte index) {
      super(TimeLineType.DRAGON_CRYSTAL_UPDATE);
      this.index = index;
   }

   public byte getIndex() {
      return this.index;
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(1).put(this.getIndex());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public CustomIdentifier getIdentifier() {
      return new CustomIdentifier((byte)1);
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      tracker.getDragonFight().onDestroyCrystal(tick, this.getIndex());
   }

   public static class CrystalDestroyTimeLineBuilder implements TimeLineBuilder {
      private byte index;

      public CrystalDestroyTimeLine.CrystalDestroyTimeLineBuilder setDestroyIndex(byte index) {
         this.index = index;
         return this;
      }

      public CrystalDestroyTimeLine build() {
         return new CrystalDestroyTimeLine(this.index);
      }
   }

   public static class CrystalDestroyTimeLineFactory implements TimeLineFactorySingleton<CustomIdentifier> {
      public static final CrystalDestroyTimeLine.CrystalDestroyTimeLineFactory INSTANCE = new CrystalDestroyTimeLine.CrystalDestroyTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[0];
      }

      public CrystalDestroyTimeLine.CrystalDestroyTimeLineBuilder getBuilder() {
         return new CrystalDestroyTimeLine.CrystalDestroyTimeLineBuilder();
      }

      public CrystalDestroyTimeLine getFromBytes(ByteBuffer buffer) {
         return new CrystalDestroyTimeLine(buffer.get());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, CustomIdentifier param) {
      }
   }
}
