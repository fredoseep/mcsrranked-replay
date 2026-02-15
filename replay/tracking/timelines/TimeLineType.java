package com.mcsrranked.client.anticheat.replay.tracking.timelines;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.EmptyTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.BlockBreakTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.BlockProgressTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.BlockRemoveTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.BlockUpdateTimeLineV1;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.BlockUpdateTimeLineV2;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.ChestAnimationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block.ExplosionEffectTimeline;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon.CrystalDestroyTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon.DragonDeathTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon.DragonHealthTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon.DragonPositionTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityAddTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityDamageTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityMoveLookTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityMovePosTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityMoveTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity.EntityRemoveTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.ItemEquipTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.projectile.ItemEntityTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.projectile.ProjectileEntityTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerPauseTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerPoseTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerPositionLookTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerPositionPosTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerPositionTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerRemoveTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerRideTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player.PlayerSpawnTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;

public enum TimeLineType {
   EMPTY(EmptyTimeLine.EmptyTimeLineFactory.INSTANCE),
   PLAYER_POSITION(PlayerPositionTimeLine.PlayerPositionTimeLineFactory.INSTANCE, 8),
   PLAYER_POSITION_LOOK(PlayerPositionLookTimeLine.PlayerPositionLookTimeLineFactory.INSTANCE, 9),
   PLAYER_POSITION_POS(PlayerPositionPosTimeLine.PlayerPositionPosTimeLineFactory.INSTANCE, 9),
   PLAYER_SPAWN(PlayerSpawnTimeLine.PlayerSpawnTimeLineFactory.INSTANCE, 5),
   BLOCK_UPDATE_V1(BlockUpdateTimeLineV1.BlockUpdateTimeLineFactory.INSTANCE, 8),
   BLOCK_BREAK(BlockBreakTimeLine.BlockBreakTimeLineFactory.INSTANCE, 9),
   BLOCK_REMOVE(BlockRemoveTimeLine.BlockRemoveTimeLineFactory.INSTANCE, 9),
   ENTITY_ADD(EntityAddTimeLine.EntityAddTimeLineFactory.INSTANCE, 5),
   ENTITY_REMOVE(EntityRemoveTimeLine.EntityRemoveTimeLineFactory.INSTANCE, 12),
   ENTITY_MOVE(EntityMoveTimeLine.EntityMoveTimeLineFactory.INSTANCE, 8),
   ENTITY_MOVE_LOOK(EntityMoveLookTimeLine.EntityMoveLookTimeLineFactory.INSTANCE, 9),
   ENTITY_MOVE_POS(EntityMovePosTimeLine.EntityMovePosTimeLineFactory.INSTANCE, 9),
   ENTITY_DAMAGE(EntityDamageTimeLine.EntityDamageTimeLineFactory.INSTANCE),
   PROJECTILE_ENTITY(ProjectileEntityTimeLine.ProjectileEntityTimeLineFactory.INSTANCE),
   ITEM_EQUIP(ItemEquipTimeLine.ItemEquipTimeLineFactory.INSTANCE, 12),
   PLAYER_POSE(PlayerPoseTimeLine.PlayerPoseTimeLineFactory.INSTANCE),
   PLAYER_REMOVE(PlayerRemoveTimeLine.PlayerRemoveTimeLineFactory.INSTANCE, 15),
   BLOCK_PROGRESS(BlockProgressTimeLine.BlockProgressTimeLineFactory.INSTANCE),
   PLAYER_RIDE(PlayerRideTimeLine.PlayerRideTimeLineFactory.INSTANCE),
   ITEM_ENTITY(ItemEntityTimeLine.ItemEntityTimeLineFactory.INSTANCE),
   DRAGON_HEALTH_UPDATE(DragonHealthTimeLine.DragonHealthTimeLineFactory.INSTANCE),
   DRAGON_CRYSTAL_UPDATE(CrystalDestroyTimeLine.CrystalDestroyTimeLineFactory.INSTANCE),
   DRAGON_MOVE(DragonPositionTimeLine.DragonPositionTimeLineFactory.INSTANCE),
   DRAGON_DEATH(DragonDeathTimeLine.DragonDeathTimeLineFactory.INSTANCE),
   DEBUG_DRAGON_REFRESH(EmptyTimeLine.EmptyTimeLineFactory.INSTANCE),
   DEBUG_PLAYER_FOLLOW(EmptyTimeLine.EmptyTimeLineFactory.INSTANCE),
   DEBUG_ENTITIES_REFRESH(EmptyTimeLine.EmptyTimeLineFactory.INSTANCE),
   BLOCK_UPDATE_V2(BlockUpdateTimeLineV2.BlockUpdateTimeLineFactory.INSTANCE, 8),
   EXPLOSION_EFFECT(ExplosionEffectTimeline.ExplosionEffectTimelineFactory.INSTANCE),
   PLAYER_PAUSE(PlayerPauseTimeLine.PlayerPauseTimeLineFactory.INSTANCE),
   CHEST_ANIMATION(ChestAnimationTimeLine.ChestAnimationTimeLineFactory.INSTANCE);

   private final TimeLineFactorySingleton<? extends Identifier> timeLineFactory;
   private final int priority;

   public int getPriority() {
      return this.priority;
   }

   public TimeLineFactorySingleton getTimeLineFactory() {
      return this.timeLineFactory;
   }

   private TimeLineType(TimeLineFactorySingleton<? extends Identifier> timeLineFactory) {
      this(timeLineFactory, 10);
   }

   private TimeLineType(TimeLineFactorySingleton<? extends Identifier> timeLineFactory, int priority) {
      this.timeLineFactory = timeLineFactory;
      this.priority = priority;
   }

   // $FF: synthetic method
   private static TimeLineType[] $values() {
      return new TimeLineType[]{EMPTY, PLAYER_POSITION, PLAYER_POSITION_LOOK, PLAYER_POSITION_POS, PLAYER_SPAWN, BLOCK_UPDATE_V1, BLOCK_BREAK, BLOCK_REMOVE, ENTITY_ADD, ENTITY_REMOVE, ENTITY_MOVE, ENTITY_MOVE_LOOK, ENTITY_MOVE_POS, ENTITY_DAMAGE, PROJECTILE_ENTITY, ITEM_EQUIP, PLAYER_POSE, PLAYER_REMOVE, BLOCK_PROGRESS, PLAYER_RIDE, ITEM_ENTITY, DRAGON_HEALTH_UPDATE, DRAGON_CRYSTAL_UPDATE, DRAGON_MOVE, DRAGON_DEATH, DEBUG_DRAGON_REFRESH, DEBUG_PLAYER_FOLLOW, DEBUG_ENTITIES_REFRESH, BLOCK_UPDATE_V2, EXPLOSION_EFFECT, PLAYER_PAUSE, CHEST_ANIMATION};
   }
}
