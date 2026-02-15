package com.mcsrranked.client.anticheat.replay.tracking;

import com.mcsrranked.client.anticheat.replay.Replay;
import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import java.util.TreeMap;
import java.util.UUID;


public class ReplayPlayerTracker {
   private final TreeMap<Integer, ReplayPlayerState> stateMap = new TreeMap();
   private final ReplayEntityTracker<ReplayPlayerEntity> entityTracker;
   private final OpponentPlayerTracker tracker;

   public ReplayPlayerTracker(OpponentPlayerTracker tracker, int entityId, UUID targetUUID, String nickname) {
      this.tracker = tracker;
      ReplayPlayerEntity replayPlayer = (ReplayPlayerEntity)Replay.REPLAY_PLAYER_ENTITY_TYPE.create((World)null);

      assert replayPlayer != null;

      replayPlayer.setCustomName(Text.method_30163(nickname));
      replayPlayer.setTargetSkinUuid(targetUUID);
      replayPlayer.setGhostMode(tracker.isGhostMode());
      this.entityTracker = new ReplayEntityTracker(tracker, entityId, replayPlayer, WorldTypes.OVERWORLD);
   }

   public ReplayPlayerState getStateTree(int tick) {
      Integer floorKey = (Integer)this.stateMap.floorKey(tick);
      ReplayPlayerState target = floorKey != null ? ((ReplayPlayerState)this.stateMap.get(floorKey)).duplicate() : new ReplayPlayerState();
      this.stateMap.put(tick, target);
      return target;
   }

   public void tick(int tick) {
      ((ReplayPlayerEntity)this.getEntityTracker().getTarget()).setCustomNameVisible(this.tracker.shouldDisplayNameTag());
      ReplayPlayerState state = (ReplayPlayerState)this.stateMap.get(tick);
      if (state != null) {
         state.apply(this.getEntityTracker());
      }
   }

   public ReplayEntityTracker<ReplayPlayerEntity> getEntityTracker() {
      return this.entityTracker;
   }
}
