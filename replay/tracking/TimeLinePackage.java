package com.mcsrranked.client.anticheat.replay.tracking;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import java.nio.ByteBuffer;
import java.util.Base64;

public class TimeLinePackage {
   private final byte type;
   private final TimeLine<?> timeLine;
   private final int tick;

   public byte getType() {
      return this.type;
   }

   public int getTick() {
      return this.tick;
   }

   public TimeLine<?> getTimeLine() {
      return this.timeLine;
   }

   public TimeLinePackage(byte type, TimeLine<?> timeLine, int tick) {
      this.type = type;
      this.timeLine = timeLine;
      this.tick = tick;
   }

   public String toString() {
      return Base64.getEncoder().encodeToString(this.toBytes().array());
   }

   public ByteBuffer toBytes() {
      ByteBuffer data = (ByteBuffer)this.timeLine.toBytes().rewind();
      return ByteBuffer.allocate(5 + data.capacity()).put(this.type).putInt(this.tick).put(data);
   }

   public static TimeLinePackage fromBytes(OpponentPlayerTracker tracker, ByteBuffer buffer) {
      byte type = buffer.get();
      int tick = buffer.getInt();
      TimeLine<?> data = TimeLineType.values()[type].getTimeLineFactory().getFromBytes(buffer);
      return new TimeLinePackage(type, data, tick);
   }
}
