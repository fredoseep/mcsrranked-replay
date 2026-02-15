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
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

public class EntityRemoveTimeLine extends WorldTimeLine<WorldUUIDIdentifier> {
   private final boolean death;
   private final int entityId;

   protected EntityRemoveTimeLine(WorldTypes world, int entityId, boolean death) {
      super(TimeLineType.ENTITY_REMOVE, world);
      this.entityId = entityId;
      this.death = death;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 5).put(superBuffer).putInt(this.entityId).put((byte)(this.death ? 1 : 0));
   }

   public UUID getEntityUUID() {
      return new UUID(11497110107101100L, (long)this.entityId);
   }

   public boolean isDeath() {
      return this.death;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().getEntityTracker(this.getEntityUUID());
      if (entityTracker != null) {
         if (this.isDeath() && !silence) {
            entityTracker.death();
         } else {
            entityTracker.setVisible(false);
         }
      }

   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public static class EntityRemoveTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private int entityId;
      private boolean death;

      public EntityRemoveTimeLine.EntityRemoveTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public EntityRemoveTimeLine.EntityRemoveTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public EntityRemoveTimeLine.EntityRemoveTimeLineBuilder setDeath(boolean death) {
         this.death = death;
         return this;
      }

      public EntityRemoveTimeLine build() {
         return new EntityRemoveTimeLine(this.world, this.entityId, this.death);
      }
   }

   public static class EntityRemoveTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityRemoveTimeLine.EntityRemoveTimeLineFactory INSTANCE = new EntityRemoveTimeLine.EntityRemoveTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ENTITY_ADD, TimeLineType.ENTITY_REMOVE};
      }

      public EntityRemoveTimeLine.EntityRemoveTimeLineBuilder getBuilder() {
         return new EntityRemoveTimeLine.EntityRemoveTimeLineBuilder();
      }

      public EntityRemoveTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityRemoveTimeLine(WorldTypes.values()[buffer.get()], buffer.getInt(), buffer.get() == 1);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
      }
   }
}
