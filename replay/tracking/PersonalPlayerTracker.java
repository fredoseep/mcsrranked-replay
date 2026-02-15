package com.mcsrranked.client.anticheat.replay.tracking;

import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerRemoveTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.info.match.online.MatchStatus;
import com.mcsrranked.client.info.match.online.OnlineMatch;
import com.mcsrranked.client.socket.SocketInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.client.world.ClientWorld;
import org.apache.commons.io.FileUtils;
import org.xerial.snappy.Snappy;
import net.minecraft.client.MinecraftClient;

public class PersonalPlayerTracker {
   private static final File localFile;
   private int range = 0;
   private long lastUploadTime = 0L;
   private long timeLineIndex = 0L;
   private int currentTicks = 0;
   private boolean stopTracking = false;
   private boolean fastBatch = false;
   private boolean optimized = false;
   private PersonalPlayerTracker.ActiveType activeType;
   private final CopyOnWriteArrayList<CopyOnWriteArrayList<TimeLinePackage>> batchedTimeLines;
   private final AtomicInteger timelineSize;

   public PersonalPlayerTracker() {
      this.activeType = PersonalPlayerTracker.ActiveType.NONE;
      this.batchedTimeLines = new CopyOnWriteArrayList();
      this.timelineSize = new AtomicInteger();
      this.reset();
   }

   public int getRange() {
      return this.range;
   }

   public void setRange(int range) {
      this.range = range;
   }

   public boolean isOptimized() {
      return this.optimized;
   }

   public void setOptimized(boolean optimized) {
      this.optimized = optimized;
   }

   public void setFastBatch(boolean fastBatch) {
      this.fastBatch = fastBatch;
   }

   public void setActiveType(PersonalPlayerTracker.ActiveType activeType) {
      MCSRRankedClient.setCurrentReplayTracker(activeType != PersonalPlayerTracker.ActiveType.NONE ? this : null);
      this.activeType = activeType;
   }

   public PersonalPlayerTracker.ActiveType getActiveType() {
      return this.activeType;
   }

   public boolean isActive() {
      return (Boolean)this.getActiveType().activeSupplier.get() && this.getRange() > 0 && !this.stopTracking;
   }

   public void stopTracking() {
      ClientWorld world = MinecraftClient.getInstance().world;
      if (world != null) {
         this.addTimeLine(PlayerRemoveTimeLine.PlayerRemoveTimeLineFactory.INSTANCE.getBuilder().setDeath(false).setWorld(WorldTypes.fromDimension(world.getDimension())).build());
      }

      this.uploadTimeLineBatch(true);
      this.stopTracking = true;
   }

   public void resumeTracking() {
      this.stopTracking = false;
   }

   public void increaseTick() {
      ++this.currentTicks;
   }

   public void tickTracker() {
      if (System.currentTimeMillis() - this.lastUploadTime > (long)(this.fastBatch ? 1000 : 5000) || this.timelineSize.get() >= 40000) {
         this.uploadTimeLineBatch(false);
         this.lastUploadTime = System.currentTimeMillis();
      }

   }

   public synchronized void uploadTimeLineBatch(boolean ignoreActive) {
      if (ignoreActive || this.isActive()) {
         ++this.timeLineIndex;
         CopyOnWriteArrayList<TimeLinePackage> timeLine = (CopyOnWriteArrayList)this.batchedTimeLines.get((int)((this.timeLineIndex - 1L) % 2L));
         if (timeLine.isEmpty()) {
            timeLine.add(new TimeLinePackage((byte)TimeLineType.EMPTY.ordinal(), TimeLineType.EMPTY.getTimeLineFactory().getBuilder().build(), this.currentTicks));
         }

         this.batchedTimeLines.set((int)((this.timeLineIndex - 1L) % 2L), new CopyOnWriteArrayList());
         this.timelineSize.set(0);
         this.uploadTimeLines(timeLine);
      }
   }

   public void uploadTimeLines(List<TimeLinePackage> timeLine) {
      List<ByteBuffer> buffers = (List)timeLine.stream().map(TimeLinePackage::toBytes).map((bufferx) -> {
         return (ByteBuffer)bufferx.rewind();
      }).collect(Collectors.toList());
      ByteBuffer buffer = ByteBuffer.allocate(16 + buffers.stream().mapToInt(Buffer::remaining).sum());
      buffer.putLong(MinecraftClient.getInstance().getSession().getProfile().getId().getMostSignificantBits());
      buffer.putLong(MinecraftClient.getInstance().getSession().getProfile().getId().getLeastSignificantBits());
      Iterator var4 = buffers.iterator();

      while(var4.hasNext()) {
         ByteBuffer byteBuffer = (ByteBuffer)var4.next();
         buffer.put(byteBuffer);
      }

      try {
         byte[] replayData = Snappy.compress(buffer.array());
         MCSRRankedClient.THREAD_EXECUTOR.submit(() -> {
            if ((Boolean)MCSRRankedClient.getOnlineMatch().map(OnlineMatch::isGameplayInteractable).orElse(false)) {
               SocketInstance.getInstance().emit("p$replay", replayData);
            } else {
               ByteBuffer localBuffer = ByteBuffer.allocate(4 + replayData.length);
               localBuffer.putInt(replayData.length);
               localBuffer.put(replayData);

               try {
                  FileUtils.writeByteArrayToFile(localFile, localBuffer.array(), true);
               } catch (IOException var3) {
                  var3.printStackTrace();
               }
            }

         });
      } catch (IOException var6) {
         MCSRRankedClient.LOGGER.error("Failed to compress replay data", var6);
      }

   }

   private void addTimeLine(byte type, TimeLine<?> data) {
      TimeLinePackage timelinePackage = new TimeLinePackage(type, data, this.currentTicks);
      this.timelineSize.addAndGet(timelinePackage.toBytes().array().length);
      if (this.isActive()) {
         ((CopyOnWriteArrayList)this.batchedTimeLines.get((int)(this.timeLineIndex % 2L))).add(timelinePackage);
      }

   }

   public void addTimeLine(TimeLine<?> timeline) {
      if (this.isOptimized()) {
         if (timeline.isPlayerMovementTimeline()) {
            this.addTimeLine((byte)timeline.getType().ordinal(), timeline);
         }
      } else {
         this.addTimeLine((byte)timeline.getType().ordinal(), timeline);
      }

   }

   public void reset() {
      this.stopTracking = false;
      this.lastUploadTime = 0L;
      this.timeLineIndex = 0L;
      this.currentTicks = 0;
      this.batchedTimeLines.add(new CopyOnWriteArrayList());
      this.batchedTimeLines.add(new CopyOnWriteArrayList());

      try {
         FileUtils.writeStringToFile(localFile, "", StandardCharsets.UTF_8);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public void copySettings(PersonalPlayerTracker oldTracker) {
      this.setRange(oldTracker.getRange());
      this.setActiveType(oldTracker.getActiveType());
      this.setOptimized(oldTracker.isOptimized());
      this.fastBatch = oldTracker.fastBatch;
   }

   public File getLocalFile() {
      return localFile;
   }

   static {
      try {
         localFile = File.createTempFile("temp_", ".rpd");
         localFile.deleteOnExit();
      } catch (IOException var1) {
         throw new RuntimeException(var1);
      }
   }

   public static enum ActiveType {
      NONE(() -> {
         return false;
      }),
      MATCH(() -> {
         return !MCSRRankedClient.LOCAL_PLAYER.isSpectator() && (Boolean)MCSRRankedClient.getOnlineMatch().map((match) -> {
            return match.getStatus() == MatchStatus.RUNNING && !match.shouldBlockBehaviors();
         }).orElse(false);
      }),
      RACE(() -> {
         return (Boolean)MCSRRankedClient.getCurrentRace().map((race) -> {
            return race.isRunning() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCompleted();
         }).orElse(false);
      });

      private final Supplier<Boolean> activeSupplier;

      private ActiveType(Supplier<Boolean> activeSupplier) {
         this.activeSupplier = activeSupplier;
      }

      // $FF: synthetic method
      private static PersonalPlayerTracker.ActiveType[] $values() {
         return new PersonalPlayerTracker.ActiveType[]{NONE, MATCH, RACE};
      }
   }
}
