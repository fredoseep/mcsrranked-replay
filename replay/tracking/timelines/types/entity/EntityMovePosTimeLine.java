package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionFTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;
import java.util.UUID;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.MinecraftServer;

public class EntityMovePosTimeLine extends PositionFTimeLine<WorldUUIDIdentifier> {
   private final int entityId;

   protected EntityMovePosTimeLine(WorldTypes world, Vector3f position, int entityId) {
      super(TimeLineType.ENTITY_MOVE_POS, world, position);
      this.entityId = entityId;
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      tracker.getEntityManager().moveEntity(this.getWorld().toWorld(server), this.getEntityUUID(), this.getPosition());
   }

   public UUID getEntityUUID() {
      return EntityTimeLine.getEntityUUIDById(this.entityId);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putInt(this.entityId);
   }

   public static class EntityMovePosTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private int entityId;

      public EntityMovePosTimeLine.EntityMovePosTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public EntityMovePosTimeLine.EntityMovePosTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public EntityMovePosTimeLine.EntityMovePosTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public EntityMovePosTimeLine.EntityMovePosTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public EntityMovePosTimeLine build() {
         return new EntityMovePosTimeLine(this.world, this.position, this.entityId);
      }
   }

   public static class EntityMovePosTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityMovePosTimeLine.EntityMovePosTimeLineFactory INSTANCE = new EntityMovePosTimeLine.EntityMovePosTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ENTITY_MOVE_POS};
      }

      public EntityMovePosTimeLine.EntityMovePosTimeLineBuilder getBuilder() {
         return new EntityMovePosTimeLine.EntityMovePosTimeLineBuilder();
      }

      public EntityMovePosTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityMovePosTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getInt());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         tracker.getEntityManager().moveEntity(param.getWorld().toWorld(server), param.getUUID());
      }
   }
}
