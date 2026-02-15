package com.mcsrranked.client.anticheat.replay.tracking;

import com.google.common.collect.Lists;
import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.replay.ReplayDragonFight;

import com.mcsrranked.client.anticheat.replay.ReplayEntityManager;
import com.mcsrranked.client.anticheat.replay.render.BlockBreakingStack;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldPosIIdentifier;
import com.mcsrranked.client.info.match.MatchSplit;
import com.mcsrranked.client.info.match.MatchSplitTime;
import com.mcsrranked.client.info.match.MatchTimeline;

import com.mcsrranked.client.vanillafix.RenderTaskQueue;
import com.mojang.datafixers.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.FileUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;

public class OpponentPlayerTracker {
   private final SortedMap<TimeLineType, Pair<OpponentPlayerTracker.DebugPair, OpponentPlayerTracker.DebugPair>> debugTime = new TreeMap<>(Comparator.comparing(Enum::ordinal));
   private long systemTime = System.currentTimeMillis();
   private final UUID uuid;
   private final ReplayEntityManager replayEntityManager;
   private final CopyOnWriteArrayList<Integer> worldResetTicks = new CopyOnWriteArrayList();
   public final Map<WorldPosIIdentifier, BlockState> blockStateCache = new HashMap();
   private final Map<Integer, Map<Byte, ArrayList<TimeLine<?>>>> timeLines = new ConcurrentHashMap();
   private final Map<Byte, Map<Identifier, Map<Integer, ArrayList<TimeLine<?>>>>> rollbackTimeLineMap = new ConcurrentHashMap();
   public final Map<WorldPosIIdentifier, OpponentPlayerTracker.UpdateState> updateBlockStateMap = new LinkedHashMap();
   public final Map<Integer, Map<Byte, ItemStack>> playerInventoryCache = new ConcurrentHashMap();
   public final TreeMap<Integer, MatchSplitTime> splitTimeTreeMap = new TreeMap();
   public final Map<Integer, WorldTypes> playerDimensionCache = new LinkedHashMap();
   public final Map<Integer, Boolean> deathTickMap = new ConcurrentHashMap();
   private final ReplayDragonFight dragonFight;
   private int currentTicks = -1;
   private int lastTickCount = -1;
   private int lastResetTick = 0;
   private boolean active = false;
   private boolean ghostMode = false;
   private boolean followDimension = true;
   private boolean displayNameTag = false;
   private boolean ghostOnly = false;
   private final File cacheFile;
   private boolean cached = false;

   public OpponentPlayerTracker(UUID uuid, String nickname) {
      this.uuid = uuid;
      this.replayEntityManager = new ReplayEntityManager(this, this.uuid, nickname);
      this.dragonFight = new ReplayDragonFight(this);
      this.cacheFile = MCSRRankedClient.REPLAY_CACHE_PATH.resolve(uuid.toString() + ".prd").toFile();
      this.cacheFile.deleteOnExit();
      if (this.cacheFile.exists()) {
         this.cacheFile.delete();
      }

      this.playerDimensionCache.put(0, WorldTypes.OVERWORLD);
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public int getCurrentTicks() {
      return this.currentTicks;
   }

   public int getLastTicks() {
      return this.lastTickCount;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setFollowDimension(boolean followDimension) {
      this.followDimension = followDimension;
   }

   public boolean isFollowDimension() {
      return this.followDimension;
   }

   public boolean shouldDisplayNameTag() {
      return this.displayNameTag;
   }

   public void setDisplayNameTag(boolean displayNameTag) {
      this.displayNameTag = displayNameTag;
   }

   public boolean isCached() {
      return this.cached;
   }

   public void setCached(boolean cached) {
      if (this.cached != cached) {
         this.cached = cached;
         if (this.isCached()) {
            try {
               if (!this.cacheFile.exists()) {
                  return;
               }

               BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(this.cacheFile.toPath())));

               while(reader.ready()) {
                  String line = reader.readLine();
                  ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(line));
                  byteBuffer.position(16);
                  Map<Integer, Map<Byte, ArrayList<TimeLine<?>>>> timelines = this.convertTimeLines(byteBuffer);
                  TreeMap<Integer, List<TimeLine<?>>> timelineInitializer = new TreeMap();
                  Iterator var7 = timelines.entrySet().iterator();

                  Entry entry;
                  while(var7.hasNext()) {
                     entry = (Entry)var7.next();
                     this.addTimeLine((Integer)entry.getKey(), (Map)entry.getValue(), timelineInitializer);
                  }

                  var7 = timelineInitializer.entrySet().iterator();

                  while(var7.hasNext()) {
                     entry = (Entry)var7.next();
                     this.updateTimelineNoneCache((Integer)entry.getKey(), (List)entry.getValue());
                  }
               }

               reader.close();
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         } else {
            this.clear();
         }

      }
   }

   public void reset() {
      this.currentTicks = -1;
      this.lastResetTick = 0;
      this.clear();
   }

   public void clear() {
      this.replayEntityManager.clear();
      this.worldResetTicks.clear();
      this.blockStateCache.clear();
      this.timeLines.clear();
      this.rollbackTimeLineMap.clear();
      this.updateBlockStateMap.clear();
      this.playerInventoryCache.clear();
   }

   public void addResetWorldTimeline(int tick) {
      this.worldResetTicks.add(tick);
   }

   public int getLastResetTickFrom(int tick) {
      return (Integer)this.worldResetTicks.stream().filter((t) -> {
         return t <= tick;
      }).max(Comparator.naturalOrder()).orElse(0);
   }

   public ReplayEntityManager getEntityManager() {
      return this.replayEntityManager;
   }

   public ReplayPlayerTracker getReplayPlayerTracker() {
      return this.getEntityManager().getPlayerTracker();
   }

   public void onUpdateMatchTimeline(MatchTimeline matchTimeline) {
      MatchSplit split = MatchSplit.fromTimeline(matchTimeline);
      if (split != null) {
         this.splitTimeTreeMap.put(matchTimeline.getTick(), new MatchSplitTime(split, matchTimeline.getTime()));
      }

      if (matchTimeline.getType().startsWith("projectelo.timeline.death")) {
         this.deathTickMap.put(matchTimeline.getTick(), matchTimeline.getType().equals("projectelo.timeline.death"));
      }

   }

   public MatchSplitTime getMatchSplitData(int tick) {
      Integer key = (Integer)this.splitTimeTreeMap.floorKey(tick);
      return key == null ? new MatchSplitTime(MatchSplit.STARTED, 0L) : (MatchSplitTime)this.splitTimeTreeMap.get(key);
   }

   public boolean updateBlockState(MinecraftServer server, WorldPosIIdentifier identifier, BlockState state) {
      return this.updateBlockState(server, identifier, state, 3, true);
   }

   public boolean updateBlockState(MinecraftServer server, WorldPosIIdentifier identifier, BlockState state, boolean update) {
      return this.updateBlockState(server, identifier, state, 3, update);
   }

   public boolean updateBlockState(MinecraftServer server, WorldPosIIdentifier identifier, BlockState state, int flags, boolean update) {
      ServerWorld world = identifier.getWorld().toWorld(server);
      BlockPos blockPos = new BlockPos(identifier.getPos());
      this.updateBlockStateMap.remove(identifier);
      if (world.isChunkLoaded(new BlockPos(identifier.getPos()))&& (world.getEnderDragonFight() == null || world.getEnderDragonFight().exitPortalLocation != null)) {//5.6.12新增判定条件防止没开始龙战就更改方块
         BlockState blockState = state.getBlock() == Blocks.BARRIER ? world.getFluidState(blockPos).getBlockState() : state;
         if (state.getBlock() == Blocks.NETHER_PORTAL) {
            flags = 0;
         }

         if (update) {
            this.blockStateCache.putIfAbsent(identifier, world.getBlockState(blockPos));
         }

         world.setBlockState(blockPos, blockState, flags);
         return true;
      } else {
         this.updateBlockStateMap.put(identifier, new OpponentPlayerTracker.UpdateState(state, flags, update));
         return false;
      }
   }

   public void tickBlockStateUpdate(MinecraftServer server) {
      HashSet<Entry<WorldPosIIdentifier, OpponentPlayerTracker.UpdateState>> entries = new LinkedHashSet(this.updateBlockStateMap.entrySet());
      Iterator var3 = entries.iterator();

      while(var3.hasNext()) {
         Entry<WorldPosIIdentifier, OpponentPlayerTracker.UpdateState> entry = (Entry)var3.next();
         ServerWorld world = ((WorldPosIIdentifier)entry.getKey()).getWorld().toWorld(server);
         BlockPos blockPos = new BlockPos(((WorldPosIIdentifier)entry.getKey()).getPos());
         if (world.isChunkLoaded(blockPos)) {
            BlockState blockState = ((OpponentPlayerTracker.UpdateState)entry.getValue()).blockState.getBlock() == Blocks.BARRIER ? world.getFluidState(blockPos).getBlockState() : ((OpponentPlayerTracker.UpdateState)entry.getValue()).blockState;
            int flags = ((OpponentPlayerTracker.UpdateState)entry.getValue()).flags;
            if (blockState.getBlock() == Blocks.NETHER_PORTAL) {
               flags = 0;
            }

            this.updateBlockStateMap.remove(entry.getKey());
            if (((OpponentPlayerTracker.UpdateState)entry.getValue()).update) {
               this.blockStateCache.putIfAbsent((WorldPosIIdentifier)entry.getKey(), world.getBlockState(blockPos));
            }

            world.setBlockState(blockPos, blockState, flags);
         }
      }

   }

   public ReplayDragonFight getDragonFight() {
      return this.dragonFight;
   }

   private void printDebug() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         Iterator var1 = this.debugTime.entrySet().iterator();

         while(var1.hasNext()) {
            Entry<TimeLineType, Pair<OpponentPlayerTracker.DebugPair, OpponentPlayerTracker.DebugPair>> debugEntry = (Entry)var1.next();
            if (((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getFirst()).count > 0) {
               MCSRRankedClient.LOGGER.info("[{} - tick] count: {} | taken: {} | average: {}", ((TimeLineType)debugEntry.getKey()).name(), ((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getFirst()).count, ((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getFirst()).total, (double)((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getFirst()).total / ((double)((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getFirst()).count * 1.0D));
            }

            if (((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getSecond()).count > 0) {
               MCSRRankedClient.LOGGER.info("[{} - back] count: {} | taken: {} | average: {}", ((TimeLineType)debugEntry.getKey()).name(), ((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getSecond()).count, ((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getSecond()).total, (double)((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getSecond()).total / ((double)((OpponentPlayerTracker.DebugPair)((Pair)debugEntry.getValue()).getSecond()).count * 1.0D));
            }
         }

         this.debugTime.clear();
      }
   }

   private void captureDebug() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         this.systemTime = System.currentTimeMillis();
      }
   }

   private void putDebug(TimeLineType type, boolean ticking) {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         this.debugTime.putIfAbsent(type, new Pair(new OpponentPlayerTracker.DebugPair(), new OpponentPlayerTracker.DebugPair()));
         OpponentPlayerTracker.DebugPair pair = ticking ? (OpponentPlayerTracker.DebugPair)((Pair)this.debugTime.get(type)).getFirst() : (OpponentPlayerTracker.DebugPair)((Pair)this.debugTime.get(type)).getSecond();
         ++pair.count;
         pair.total += System.currentTimeMillis() - this.systemTime;
      }
   }

   public void moveToTick(int tick, MinecraftServer server) {
      ((RenderTaskQueue)MinecraftClient.getInstance()).ranked$addRenderTask(() -> {
         ((BlockBreakingStack)MinecraftClient.getInstance().worldRenderer).ranked$clearAllBreakingInfo();
      });
      int ct = this.getCurrentTicks();
      this.debugTime.clear();
      Vec3d beforePos = this.getReplayPlayerTracker().getEntityTracker().getPos();
      if (ct < tick) {
         for(int i = ct; i < tick; ++i) {
            this.tickTracker(server, false, true);
         }
      } else if (ct > tick) {
         this.rollbackUntilTick(tick - 1, server);
         if (tick > 0) {
            this.tickTracker(server, false, false);
         }
      }

      Vec3d afterPos = this.getReplayPlayerTracker().getEntityTracker().getPos();
      this.checkResetRollback(server);
      this.getDragonFight().refresh(tick, server);
      this.getEntityManager().followPlayer(afterPos.distanceTo(beforePos) > 200.0D);
      this.printDebug();
   }

   private Optional<ArrayList<TimeLine<?>>> findLatestTimeLine(List<Map<Integer, ArrayList<TimeLine<?>>>> timeLines, int tick, int resetOffset) {
      ArrayList<TimeLine<?>> timeLineArrayList = new ArrayList();

      for(int i = tick - 1; i >= 0; --i) {
         Iterator var6 = timeLines.iterator();

         while(var6.hasNext()) {
            Map<Integer, ArrayList<TimeLine<?>>> timeLineMap = (Map)var6.next();
            if (resetOffset <= i && timeLineMap != null && timeLineMap.containsKey(i)) {
               timeLineArrayList.addAll((Collection)timeLineMap.get(i));
               return Optional.of(timeLineArrayList);
            }
         }
      }

      return Optional.empty();
   }

   public void rollbackAllActions(MinecraftServer server) {
      this.rollbackUntilTick(0, server);
   }

   public void setGhostMode(boolean ghostMode) {
      this.ghostMode = ghostMode;
      ((ReplayPlayerEntity)this.getReplayPlayerTracker().getEntityTracker().getTarget()).setGhostMode(ghostMode);
   }

   public void enableGhostOnly() {
      this.ghostOnly = true;
   }

   public boolean isGhostOnly() {
      return this.ghostOnly;
   }

   public boolean isGhostMode() {
      return this.ghostMode;
   }

   public void receiveOpponentTimeLine(ByteBuffer byteBuffer) throws IOException {
      ByteBuffer remainingBuffer = byteBuffer.duplicate();
      Map<Integer, Map<Byte, ArrayList<TimeLine<?>>>> timelines = this.convertTimeLines(byteBuffer);
      TreeMap<Integer, List<TimeLine<?>>> timelineInitializer = new TreeMap();
      Iterator var5;
      Entry entry;
      int tick;
      if (this.isCached()) {
         var5 = timelines.entrySet().iterator();

         while(var5.hasNext()) {
            entry = (Entry)var5.next();
            this.addTimeLine((Integer)entry.getKey(), (Map)entry.getValue(), timelineInitializer);
         }
      } else {
         for(var5 = timelines.entrySet().iterator(); var5.hasNext(); this.lastTickCount = Math.max(tick, this.lastTickCount)) {
            entry = (Entry)var5.next();
            tick = (Integer)entry.getKey();
            timelineInitializer.putIfAbsent(tick, new ArrayList());
            Iterator var8 = ((Map)entry.getValue()).values().iterator();

            while(var8.hasNext()) {
               ArrayList<TimeLine<?>> value = (ArrayList)var8.next();
               ((List)timelineInitializer.get(tick)).addAll(value);
            }
         }
      }

      var5 = timelineInitializer.entrySet().iterator();

      while(var5.hasNext()) {
         entry = (Entry)var5.next();
         this.updateTimelineNoneCache((Integer)entry.getKey(), (List)entry.getValue());
      }

      FileUtils.writeStringToFile(this.cacheFile, Base64.getEncoder().encodeToString(remainingBuffer.array()) + "\n", StandardCharsets.UTF_8, true);
   }

   public Map<Integer, Map<Byte, ArrayList<TimeLine<?>>>> convertTimeLines(ByteBuffer buffer) {
      ArrayList timeLineList = new ArrayList();

      while(buffer.hasRemaining()) {
         timeLineList.add(TimeLinePackage.fromBytes(this, buffer));
      }

      return (Map)timeLineList.stream().collect(Collectors.groupingBy(TimeLinePackage::getTick, Collectors.groupingBy(TimeLinePackage::getType, Collectors.mapping(TimeLinePackage::getTimeLine, Collectors.toCollection(ArrayList::new)))));
   }

   private void addTimeLine(int tick, Map<Byte, ArrayList<TimeLine<?>>> playerTimeLine, TreeMap<Integer, List<TimeLine<?>>> timelineInitializer) {
      this.timeLines.putIfAbsent(tick, new TreeMap(Comparator.comparing((s) -> {
         return TimeLineType.values()[(Byte)s].getPriority();
      })));
      timelineInitializer.putIfAbsent(tick, new ArrayList());
      Iterator var4 = playerTimeLine.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<Byte, ArrayList<TimeLine<?>>> entry = (Entry)var4.next();
         Byte type = (Byte)entry.getKey();
         ArrayList<TimeLine<?>> playerTimelines = (ArrayList)entry.getValue();
         ((Map)this.timeLines.get(tick)).putIfAbsent(type, new ArrayList());
         ((ArrayList)((Map)this.timeLines.get(tick)).get(type)).addAll(playerTimelines);
         this.rollbackTimeLineMap.putIfAbsent(type, new HashMap());
         Iterator var8 = playerTimelines.iterator();

         while(var8.hasNext()) {
            TimeLine<?> timeLine = (TimeLine)var8.next();
            ((Map)this.rollbackTimeLineMap.get(type)).putIfAbsent(timeLine.getIdentifier(), new HashMap());
            ((Map)((Map)this.rollbackTimeLineMap.get(type)).get(timeLine.getIdentifier())).putIfAbsent(tick, new ArrayList());
            ((ArrayList)((Map)((Map)this.rollbackTimeLineMap.get(type)).get(timeLine.getIdentifier())).get(tick)).add(timeLine);
            timeLine.onInit(this, tick);
            ((List)timelineInitializer.get(tick)).add(timeLine);
         }
      }

      this.lastTickCount = Math.max(tick, this.lastTickCount);
   }

   private void updateTimelineNoneCache(int tick, List<TimeLine<?>> timelines) {
      ReplayPlayerState playerState = null;

      TimeLine timeLine;
      for(Iterator var4 = timelines.iterator(); var4.hasNext(); timeLine.onInit(this, tick)) {
         timeLine = (TimeLine)var4.next();
         if (timeLine.isPlayerMovementTimeline()) {
            if (playerState == null) {
               playerState = this.getReplayPlayerTracker().getStateTree(tick);
            }

            timeLine.onPlayerStateUpdate(playerState);
         }
      }

   }

   public Optional<Map<Byte, ArrayList<TimeLine<?>>>> getTimeLines(int tick) {
      return Optional.ofNullable((Map)this.timeLines.get(tick));
   }

   public void tickTracker(MinecraftServer server, boolean live, boolean silence) {
      this.tickTracker(server, live, silence, 5);
   }

   public void tickTracker(MinecraftServer server, boolean live, boolean silence, int depth) {
      if (this.currentTicks <= this.lastTickCount && depth > 0 && this.isActive()) {
         this.checkResetRollback(server);
         if (!this.isGhostMode()) {
            this.getTimeLines(this.currentTicks).ifPresent((integerArrayListMap) -> {
               integerArrayListMap.forEach((integer, playerTimeLines) -> {
                  playerTimeLines.forEach((timeLine) -> {
                     this.captureDebug();
                     timeLine.runTimeLine(this, server, silence);
                     this.putDebug(timeLine.getType(), true);
                  });
               });
            });
            if (!this.getDragonFight().hasInit()) {
               this.getDragonFight().refresh(0, server);
            } else {
               this.getDragonFight().tick(this.currentTicks, server);
            }
         }

         this.getReplayPlayerTracker().tick(this.currentTicks);
         ++this.currentTicks;
         if (live && this.lastTickCount - this.currentTicks >= 200) {
            --depth;
            this.tickTracker(server, true, silence, depth);
         }

      }
   }

   public void checkResetRollback(MinecraftServer server) {
      if (this.lastResetTick != this.getLastResetTickFrom(this.getCurrentTicks())) {
         List<Byte> types = Lists.newArrayList(this.rollbackTimeLineMap.keySet());
         types.sort((s1, s2) -> {
            int priority1 = TimeLineType.values()[s1].getPriority();
            int priority2 = TimeLineType.values()[s2].getPriority();
            return priority1 == priority2 ? s1 - s2 : priority1 - priority2;
         });
         int resetOffset = this.getLastResetTickFrom(this.getCurrentTicks());
         Iterator var4 = types.iterator();

         while(var4.hasNext()) {
            Byte type = (Byte)var4.next();
            Map<Identifier, Map<Integer, ArrayList<TimeLine<?>>>> timelineMap = (Map)this.rollbackTimeLineMap.get(type);
            timelineMap.forEach((identifier, integerArrayListMap) -> {
               this.captureDebug();
               TimeLineType.values()[type].getTimeLineFactory().defaultExecute(this, server, identifier);
               this.putDebug(TimeLineType.values()[type], false);
            });
         }

         this.lastResetTick = resetOffset;
      }

   }

   public void rollbackUntilTick(int tick, MinecraftServer server) {
      int resetOffset = this.getLastResetTickFrom(tick);
      if (!this.isGhostMode()) {
         List<Byte> types = Lists.newArrayList(this.rollbackTimeLineMap.keySet());
         types.sort((s1, s2) -> {
            int priority1 = TimeLineType.values()[s1].getPriority();
            int priority2 = TimeLineType.values()[s2].getPriority();
            return priority1 == priority2 ? s1 - s2 : priority1 - priority2;
         });
         Iterator var5 = types.iterator();

         while(var5.hasNext()) {
            Byte type = (Byte)var5.next();
            Map<Identifier, Map<Integer, ArrayList<TimeLine<?>>>> timelineMap = (Map)this.rollbackTimeLineMap.get(type);
            timelineMap.forEach((identifier, integerArrayListMap) -> {
               if (tick >= 1) {
                  List<Map<Integer, ArrayList<TimeLine<?>>>> stateModifyingTimeLines = (List)Arrays.stream(TimeLineType.values()[type].getTimeLineFactory().getInvertedTypes()).filter((timeLineType) -> {
                     return this.rollbackTimeLineMap.containsKey((byte)timeLineType.ordinal());
                  }).map((timeLineType) -> {
                     return (Map)((Map)this.rollbackTimeLineMap.get((byte)timeLineType.ordinal())).get(identifier);
                  }).collect(Collectors.toList());
                  Optional<ArrayList<TimeLine<?>>> optional = this.findLatestTimeLine(stateModifyingTimeLines, tick, resetOffset);
                  optional.ifPresent((latestTimeLines) -> {
                     latestTimeLines.forEach((timeLine) -> {
                        this.captureDebug();
                        timeLine.runTimeLine(this, server, true);
                        this.putDebug(timeLine.getType(), false);
                     });
                  });
                  if (!optional.isPresent() || ((ArrayList)optional.get()).isEmpty()) {
                     this.captureDebug();
                     TimeLineType.values()[type].getTimeLineFactory().defaultExecute(this, server, identifier);
                     this.putDebug(TimeLineType.values()[type], false);
                  }
               } else {
                  this.captureDebug();
                  TimeLineType.values()[type].getTimeLineFactory().defaultExecute(this, server, identifier);
                  this.putDebug(TimeLineType.values()[type], false);
               }

            });
         }
      }

      this.lastResetTick = resetOffset;
      this.currentTicks = tick;
   }

   public static class UpdateState {
      private final BlockState blockState;
      private final int flags;
      private final boolean update;

      private UpdateState(BlockState blockState, int flags, boolean update) {
         this.blockState = blockState;
         this.flags = flags;
         this.update = update;
      }

      // $FF: synthetic method
      UpdateState(BlockState x0, int x1, boolean x2, Object x3) {
         this(x0, x1, x2);
      }
   }

   private static class DebugPair {
      int count;
      long total;

      private DebugPair() {
         this.count = 0;
         this.total = 0L;
      }

      // $FF: synthetic method
      DebugPair(Object x0) {
         this();
      }
   }
}
