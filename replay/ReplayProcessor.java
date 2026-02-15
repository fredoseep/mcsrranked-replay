package com.mcsrranked.client.anticheat.replay;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.replay.file.ReplayManager;
import com.mcsrranked.client.anticheat.replay.file.ReplayRecordFile;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldPosIIdentifier;
import com.mcsrranked.client.info.match.MatchTimeline;
import com.mcsrranked.client.info.player.BasePlayer;
import com.mcsrranked.client.utils.TextureUtils;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.crypto.SecretKey;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;

public class ReplayProcessor {
   private int curTrackerTick = 0;
   private int maxTrackerTick = 0;
   private final Map<UUID, OpponentPlayerTracker> trackerMap = new ConcurrentHashMap();
   private final Map<Integer, List<MatchTimeline>> timelineMap = new ConcurrentHashMap();
   private final List<BasePlayer> players = new ArrayList();
   private boolean active = false;
   private boolean paused = false;
   private boolean ghostModeOnly = false;
   private boolean ghostMode = false;
   private boolean followDimension = true;
   private boolean displayNameTag = false;
   private int tickSpeed = 1;
   private final List<OpponentPlayerTracker> activeTrackers = new CopyOnWriteArrayList();
   private OpponentPlayerTracker focusedTracker = null;
   private boolean live = false;
   private boolean moving = false;
   private boolean updating = false;
   private final boolean fromReplayFile;
   private final boolean enableCompress;
   private final Queue<UUID> lastCached = new ConcurrentLinkedQueue();
   private final Map<WorldPosIIdentifier, OpponentPlayerTracker.UpdateState> updateBlockStateMap = new LinkedHashMap();

   public ReplayProcessor(ReplayRecordFile recordFile) throws Exception {
      ZipFile zipFile = new ZipFile(recordFile.getFile());
      this.players.addAll(recordFile.getMeta().getPlayers());
      this.enableCompress = recordFile.getMeta().getVersion() >= 32;
      this.prepareReplayFile(recordFile, zipFile);

      for(Iterator var3 = this.getPlayers().iterator(); var3.hasNext(); ReplayManager.CURRENT_LOADING = (int)((double)this.players.size() * 1.0D / (double)recordFile.getMeta().getPlayers().size() * 5.0D) + 95) {
         BasePlayer player = (BasePlayer)var3.next();
         TextureUtils.loadPlayerSkin(player.getUUID());
         this.activeTracker(player.getUUID(), (MinecraftServer)null);
      }

      InputStream inputStream = zipFile.getInputStream(new ZipEntry("timelines.json"));
      JsonArray timelines = (new JsonParser()).parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonArray();
      inputStream.close();
      Iterator var5 = timelines.iterator();

      while(var5.hasNext()) {
         JsonElement jsonElement = (JsonElement)var5.next();
         if (jsonElement instanceof JsonObject) {
            MatchTimeline timeline = (MatchTimeline)MCSRRankedClient.GSON.fromJson(jsonElement, MatchTimeline.class);
            this.updateTimeline(timeline);
         }
      }

      zipFile.close();
      ReplayManager.CURRENT_LOADING = 99;
      this.fromReplayFile = true;
   }

   private void prepareReplayFile(ReplayRecordFile recordFile, ZipFile zipFile) throws Exception {
      Map<UUID, String> uuidToNickname = Maps.newHashMap();
      Iterator var4 = this.getPlayers().iterator();

      while(var4.hasNext()) {
         BasePlayer player = (BasePlayer)var4.next();
         uuidToNickname.put(player.getUUID(), player.getNickname());
      }

      SecretKey symmetricKey = ReplayManager.generateSecretKey(Base64.getDecoder().decode(recordFile.getMeta().getSymmetricKey()));
      ZipEntry zipEntry = zipFile.getEntry("replay.rpd");
      InputStream zipIn = zipFile.getInputStream(zipEntry);
      long totalBytes = zipEntry.getSize();
      long readBytes = 0L;
      byte[] bytes;
      if (this.enableCompress) {
         DataInputStream dis = new DataInputStream(zipIn);

         try {
            while(true) {
               int length = dis.readInt();
               bytes = new byte[length];
               dis.readFully(bytes);
               this.loadReplayBytes(symmetricKey, bytes, uuidToNickname);
               readBytes += (long)(4 + bytes.length);
               ReplayManager.CURRENT_LOADING = (int)((double)readBytes * 1.0D / (double)totalBytes * 95.0D);
            }
         } catch (EOFException var14) {
         }
      } else {
         BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, StandardCharsets.UTF_8));

         String line;
         while((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
               bytes = Base64.getDecoder().decode(line);
               this.loadReplayBytes(symmetricKey, bytes, uuidToNickname);
               readBytes += (long)line.getBytes(StandardCharsets.UTF_8).length;
               ReplayManager.CURRENT_LOADING = (int)((double)readBytes * 1.0D / (double)totalBytes * 95.0D);
            }
         }

         reader.close();
      }

      Iterator var18 = uuidToNickname.keySet().iterator();

      while(var18.hasNext()) {
         UUID uuid = (UUID)var18.next();
         if (!this.trackerMap.containsKey(uuid)) {
            OpponentPlayerTracker tracker = new OpponentPlayerTracker(uuid, (String)uuidToNickname.get(uuid));
            tracker.setCached(true);
            this.trackerMap.put(uuid, tracker);
         }
      }

   }

   private void loadReplayBytes(SecretKey symmetricKey, byte[] bytes, Map<UUID, String> uuidToNickname) throws Exception {
      ByteBuffer buffer = ReplayManager.decryptByteBuffer(symmetricKey, bytes, this.enableCompress);
      UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
      OpponentPlayerTracker tracker;
      if (this.trackerMap.containsKey(uuid)) {
         tracker = (OpponentPlayerTracker)this.trackerMap.get(uuid);
      } else {
         tracker = new OpponentPlayerTracker(uuid, (String)uuidToNickname.get(uuid));
         tracker.setCached(this.trackerMap.size() < 2);
         this.trackerMap.put(uuid, tracker);
      }

      tracker.receiveOpponentTimeLine(buffer);
   }

   public ReplayProcessor() {
      this.enableCompress = true;
      this.live = true;
      this.fromReplayFile = false;
   }

   public Map<UUID, OpponentPlayerTracker> getTrackerMap() {
      return this.trackerMap;
   }

   public int getMaxTrackerTick() {
      return this.maxTrackerTick;
   }

   public List<OpponentPlayerTracker> getActiveTrackers() {
      return this.activeTrackers;
   }

   public Optional<OpponentPlayerTracker> getFocusedTracker() {
      return Optional.ofNullable(this.focusedTracker);
   }

   public void setFocusedTracker(UUID uuid, MinecraftServer server) {
      this.updating = true;
      if (server != null && Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.setFocusedTracker(uuid, server);
         });
      } else {
         if (!this.isGhostMode()) {
            this.activeTracker(uuid, server);
         } else {
            Optional.ofNullable((OpponentPlayerTracker)this.getTrackerMap().get(uuid)).ifPresent((tracker) -> {
               this.focusedTracker = tracker;
            });
         }

         Optional.ofNullable((OpponentPlayerTracker)this.getTrackerMap().get(uuid)).ifPresent((tracker) -> {
            tracker.getEntityManager().followPlayer(false);
         });
         this.updating = false;
      }
   }

   public Optional<OpponentPlayerTracker> getTracker(UUID uuid) {
      return uuid == null ? Optional.empty() : Optional.ofNullable((OpponentPlayerTracker)this.getTrackerMap().get(uuid));
   }

   public void addNewTracker(BasePlayer player) {
      OpponentPlayerTracker tracker = new OpponentPlayerTracker(player.getUUID(), player.getNickname());
      tracker.setCached(this.players.size() < 2);
      this.players.add(player);
      this.getTrackerMap().put(player.getUUID(), tracker);
      if (this.getActiveTrackers().isEmpty()) {
         this.activeTracker(player.getUUID(), (MinecraftServer)null);
      }

   }

   public void removeTracker(BasePlayer player) {
      this.players.remove(player);
      this.getTrackerMap().remove(player.getUUID());
      this.inactiveTracker(player.getUUID(), (MinecraftServer)null);
   }

   public void activeTracker(UUID uuid, MinecraftServer server) {
      if (server != null && Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.activeTracker(uuid, server);
         });
      } else {
         if (!this.isGhostMode() && this.getFocusedTracker().isPresent()) {
            this.inactiveTracker(((OpponentPlayerTracker)this.getFocusedTracker().get()).getUuid(), server);
         }

         OpponentPlayerTracker tracker = (OpponentPlayerTracker)this.getTrackerMap().get(uuid);
         if (tracker != null) {
            this.getActiveTrackers().add(tracker);
            this.focusedTracker = tracker;
            this.maxTrackerTick = tracker.getLastTicks();
            tracker.setActive(true);
            tracker.updateBlockStateMap.putAll(this.updateBlockStateMap);
            this.updateBlockStateMap.clear();
            if (!this.isGhostMode()) {
               tracker.setCached(true);
               if (server != null) {
                  this.moveToTick(server, this.curTrackerTick);
               }
            }

            if (!this.lastCached.contains(uuid) && !this.isGhostMode()) {
               this.lastCached.add(uuid);
               if (this.lastCached.size() > 2) {
                  UUID cacheUuid = (UUID)this.lastCached.poll();
                  this.getTracker(cacheUuid).ifPresent((t2) -> {
                     t2.setCached(false);
                  });
               }
            }

         }
      }
   }

   public void inactiveTracker(UUID uuid, MinecraftServer server) {
      if (server != null && Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.inactiveTracker(uuid, server);
         });
      } else {
         Iterator var3 = this.getActiveTrackers().iterator();

         while(var3.hasNext()) {
            OpponentPlayerTracker tracker = (OpponentPlayerTracker)var3.next();
            if (tracker.getUuid().equals(uuid)) {
               this.curTrackerTick = tracker.getCurrentTicks();
               tracker.setActive(false);
               tracker.updateBlockStateMap.clear();
               if (server != null) {
                  tracker.rollbackAllActions(server);
                  MinecraftClient.getInstance().execute(() -> {
                     tracker.getEntityManager().tick(this);
                  });
               }

               this.updateBlockStateMap.putAll(tracker.updateBlockStateMap);
               this.getActiveTrackers().remove(tracker);
               if (this.focusedTracker == tracker) {
                  this.focusedTracker = this.getActiveTrackers().isEmpty() ? null : (OpponentPlayerTracker)this.getActiveTrackers().get(0);
               }
               break;
            }
         }

      }
   }

   public void tickTracker(@NotNull MinecraftServer server) {
      if (Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.tickTracker(server);
         });
      } else {
         if (this.curTrackerTick == 0) {
            Iterator var2 = server.getWorlds().iterator();

            while(var2.hasNext()) {
               ServerWorld world = (ServerWorld)var2.next();
               if (world.getRegistryKey() == World.END) {
                  ServerWorld.createEndSpawnPlatform(world);
               }
            }
         }

         if (!this.isPaused() && !this.moving) {
            this.getActiveTrackers().forEach((tracker) -> {
               tracker.tickTracker(server, this.isLive(), false);
            });
            this.getFocusedTracker().ifPresent((tracker) -> {
               this.curTrackerTick = tracker.getCurrentTicks();
               Iterator var3;
               if (!this.isLive() && this.getTimelineMap().containsKey(this.getCurrentTicks())) {
                  var3 = ((List)this.getTimelineMap().get(this.getCurrentTicks())).iterator();

                  while(var3.hasNext()) {
                     MatchTimeline timeline = (MatchTimeline)var3.next();
                     String nickname = (String)this.getPlayers().stream().filter((player) -> {
                        return player.getUUID().equals(timeline.getUUID());
                     }).findFirst().map(BasePlayer::getNickname).orElse("Unknown");
                     server.getPlayerManager().broadcastChatMessage(timeline.getText(nickname), MessageType.SYSTEM, timeline.getUUID());
                  }
               }

               var3 = server.getWorlds().iterator();

               while(var3.hasNext()) {
                  ServerWorld world = (ServerWorld)var3.next();
                  world.setTimeOfDay((long)(this.curTrackerTick - (this.isGhostMode() ? 0 : tracker.getLastResetTickFrom(this.curTrackerTick))));
               }

            });
         }

         this.getActiveTrackers().forEach((tracker) -> {
            tracker.tickBlockStateUpdate(server);
            MinecraftClient.getInstance().execute(() -> {
               tracker.getEntityManager().tick(this);
            });
         });
         InGameTimer.getInstance().setRTAMode(true);
      }
   }

   public void moveToTick(@NotNull MinecraftServer server, int tick) {
      this.moveToTick(server, tick, (Runnable)null);
   }

   public void moveToTick(@NotNull MinecraftServer server, int tick, Runnable after) {
      if (Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.moveToTick(server, tick, after);
         });
      } else if (!this.moving) {
         this.moving = true;
         Iterator var4 = this.getActiveTrackers().iterator();

         while(var4.hasNext()) {
            OpponentPlayerTracker tracker = (OpponentPlayerTracker)var4.next();

            try {
               tracker.moveToTick(tick, server);
            } catch (Exception var8) {
               var8.printStackTrace();
            }

            this.curTrackerTick = tracker.getCurrentTicks();
            Iterator var6 = server.getWorlds().iterator();

            while(var6.hasNext()) {
               ServerWorld world = (ServerWorld)var6.next();
               world.setTimeOfDay((long)(this.curTrackerTick - tracker.getLastResetTickFrom(this.curTrackerTick)));
            }
         }

         if (after != null) {
            after.run();
         }

         this.moving = false;
      }
   }

   public boolean isFromReplayFile() {
      return this.fromReplayFile;
   }

   public boolean isLive() {
      return this.live;
   }

   public int getTickSpeed() {
      return this.tickSpeed;
   }

   public void setTickSpeed(int tickSpeed) {
      this.tickSpeed = MathHelper.clamp(tickSpeed, 1, 5);
   }

   public boolean isLoading() {
      return this.moving || this.updating;
   }

   public void setLive(boolean live) {
      this.live = live;
   }

   public boolean isPaused() {
      return this.paused || this.getActiveTrackers().isEmpty();
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
   }

   public boolean isGhostMode() {
      return this.ghostMode;
   }

   public void enableGhostOnly() {
      this.setGhostMode(true, (MinecraftServer)null);
      this.ghostModeOnly = true;
      Iterator var1 = this.trackerMap.values().iterator();

      while(var1.hasNext()) {
         OpponentPlayerTracker tracker = (OpponentPlayerTracker)var1.next();
         tracker.enableGhostOnly();
      }

   }

   public boolean isGhostModeOnly() {
      return this.ghostModeOnly;
   }

   public void setGhostMode(boolean ghostMode, MinecraftServer server) {
      if (server != null && Thread.currentThread() != server.getThread()) {
         server.execute(() -> {
            this.setGhostMode(ghostMode, server);
         });
      } else if (!this.updating) {
         this.ghostMode = ghostMode;
         this.updating = true;
         int ticks = this.getCurrentTicks();
         OpponentPlayerTracker focused = this.focusedTracker;
         Iterator var5 = Lists.newArrayList(this.getActiveTrackers()).iterator();

         OpponentPlayerTracker tracker;
         while(var5.hasNext()) {
            tracker = (OpponentPlayerTracker)var5.next();
            this.inactiveTracker(tracker.getUuid(), server);
         }

         if (ghostMode) {
            var5 = this.trackerMap.values().iterator();

            while(var5.hasNext()) {
               tracker = (OpponentPlayerTracker)var5.next();
               tracker.setGhostMode(true);
               this.activeTracker(tracker.getUuid(), server);
            }

            this.focusedTracker = focused;
         } else {
            var5 = this.trackerMap.values().iterator();

            while(var5.hasNext()) {
               tracker = (OpponentPlayerTracker)var5.next();
               tracker.setGhostMode(false);
               if (focused == tracker) {
                  this.activeTracker(tracker.getUuid(), server);
               }
            }
         }

         if (server != null) {
            this.moveToTick(server, ticks);
         }

         this.updating = false;
      }
   }

   public boolean shouldStopTick() {
      return this.isActive() && !this.ghostModeOnly;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public void setFollowDimension(boolean followDimension) {
      this.followDimension = followDimension;
      Iterator var2 = this.getActiveTrackers().iterator();

      while(var2.hasNext()) {
         OpponentPlayerTracker activeTracker = (OpponentPlayerTracker)var2.next();
         activeTracker.setFollowDimension(followDimension);
      }

   }

   public boolean isFollowDimension() {
      return this.followDimension;
   }

   public void setDisplayNameTag(boolean displayNameTag) {
      this.displayNameTag = displayNameTag;
      Iterator var2 = this.getTrackerMap().values().iterator();

      while(var2.hasNext()) {
         OpponentPlayerTracker tracker = (OpponentPlayerTracker)var2.next();
         tracker.setDisplayNameTag(this.displayNameTag);
      }

   }

   public boolean shouldDisplayNameTag() {
      return this.displayNameTag;
   }

   public Map<Integer, List<MatchTimeline>> getTimelineMap() {
      return this.timelineMap;
   }

   public Collection<MatchTimeline> getTimelines() {
      List<MatchTimeline> allTimelines = new ArrayList();
      Iterator var2 = this.getTimelineMap().values().iterator();

      while(var2.hasNext()) {
         List<MatchTimeline> value = (List)var2.next();
         allTimelines.addAll(value);
      }

      allTimelines.sort(Comparator.comparingLong(MatchTimeline::getTime));
      return allTimelines;
   }

   public void updateTimeline(MatchTimeline timeline) {
      if (timeline.isReset() && !timeline.isAdvancementRoot()) {
         this.timelineMap.putIfAbsent(timeline.getTick(), new ArrayList());
         ((List)this.timelineMap.get(timeline.getTick())).add(timeline);
      }

      if (timeline.isReset()) {
         ((OpponentPlayerTracker)this.getTrackerMap().get(timeline.getUUID())).addResetWorldTimeline(timeline.getTick());
      }

      if (timeline.isComplete()) {
         this.timelineMap.putIfAbsent(timeline.getTick(), new ArrayList());
         ((List)this.timelineMap.get(timeline.getTick())).add(timeline);
      }

      ((OpponentPlayerTracker)this.getTrackerMap().get(timeline.getUUID())).onUpdateMatchTimeline(timeline);
   }

   public List<BasePlayer> getPlayers() {
      return this.players;
   }

   public int getCurrentTicks() {
      return this.curTrackerTick;
   }

   public void setCurrentTicks(int currentTicks) {
      this.curTrackerTick = MathHelper.clamp(currentTicks, 0, this.getMaxTrackerTick());
   }

   public void reset() {
      IntegratedServer server = MinecraftClient.getInstance().server;

      OpponentPlayerTracker tracker;
      for(Iterator var2 = this.trackerMap.values().iterator(); var2.hasNext(); tracker.rollbackAllActions(server)) {
         tracker = (OpponentPlayerTracker)var2.next();
         if (server == null) {
            tracker.reset();
         }
      }

      this.setCurrentTicks(0);
   }
}
