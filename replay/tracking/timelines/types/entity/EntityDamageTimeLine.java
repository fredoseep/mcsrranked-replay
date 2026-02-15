package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.WorldTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public class EntityDamageTimeLine extends WorldTimeLine<WorldUUIDIdentifier> {
   private final int entityId;
   private final int attackerId;

   protected EntityDamageTimeLine(WorldTypes world, int entityId, int attackerId) {
      super(TimeLineType.ENTITY_DAMAGE, world);
      this.entityId = entityId;
      this.attackerId = attackerId;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!silence) {
         ReplayEntityTracker<?> victim = this.getEntityTracker(tracker, this.getEntityId());
         ReplayEntityTracker<?> attacker = this.getEntityTracker(tracker, this.getAttackerId());
         if (victim != null) {
            if (attacker != null) {
               attacker.attacked();
            }

            victim.damaged();
         }

      }
   }

   private ReplayEntityTracker<?> getEntityTracker(OpponentPlayerTracker tracker, int id) {
      if (id == -1) {
         return null;
      } else if (id == -2) {
         ReplayEntityTracker<?> replayPlayer = tracker.getReplayPlayerTracker().getEntityTracker();
         return replayPlayer.isVisible() ? replayPlayer : null;
      } else {
         return tracker.getEntityManager().getEntityTracker(EntityTimeLine.getEntityUUIDById(id));
      }
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), EntityTimeLine.getEntityUUIDById(this.getEntityId()));
   }

   public int getEntityId() {
      return this.entityId;
   }

   public int getAttackerId() {
      return this.attackerId;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 8).put(superBuffer).putInt(this.getEntityId()).putInt(this.getAttackerId());
   }

   public static class EntityDamageTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private int entityId;
      private int attackerId;

      public EntityDamageTimeLine.EntityDamageTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      private int getIdFromEntity(Entity entity) {
         if (entity == null) {
            return -1;
         } else {
            return entity instanceof PlayerEntity ? -2 : entity.getEntityId();
         }
      }

      public EntityDamageTimeLine.EntityDamageTimeLineBuilder setEntity(Entity entity) {
         this.entityId = this.getIdFromEntity(entity);
         return this;
      }

      public EntityDamageTimeLine.EntityDamageTimeLineBuilder setAttacker(Entity entity) {
         this.attackerId = this.getIdFromEntity(entity);
         return this;
      }

      public EntityDamageTimeLine build() {
         return new EntityDamageTimeLine(this.world, this.entityId, this.attackerId);
      }
   }

   public static class EntityDamageTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityDamageTimeLine.EntityDamageTimeLineFactory INSTANCE = new EntityDamageTimeLine.EntityDamageTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[0];
      }

      public EntityDamageTimeLine.EntityDamageTimeLineBuilder getBuilder() {
         return new EntityDamageTimeLine.EntityDamageTimeLineBuilder();
      }

      public EntityDamageTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityDamageTimeLine(WorldTypes.values()[buffer.get()], buffer.getInt(), buffer.getInt());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
      }
   }
}
