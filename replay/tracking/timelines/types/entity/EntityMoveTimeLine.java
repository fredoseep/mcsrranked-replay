package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.MinecraftServer;

public class EntityMoveTimeLine extends EntityTimeLine<WorldUUIDIdentifier> {
   protected EntityMoveTimeLine(WorldTypes world, Vector3f position, short yaw, short pitch, int entityId) {
      super(TimeLineType.ENTITY_MOVE, world, position, yaw, pitch, entityId);
   }

   protected EntityMoveTimeLine(WorldTypes world, Vector3f position, float yaw, float pitch, int entityId) {
      super(TimeLineType.ENTITY_MOVE, world, position, yaw, pitch, entityId);
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      tracker.getEntityManager().moveEntity(this.getWorld().toWorld(server), this.getEntityUUID(), this.getPosition(), this.getYaw(), this.getPitch());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class EntityMoveTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;
      private int entityId;

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public EntityMoveTimeLine build() {
         return new EntityMoveTimeLine(this.world, this.position, this.yaw, this.pitch, this.entityId);
      }
   }

   public static class EntityMoveTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityMoveTimeLine.EntityMoveTimeLineFactory INSTANCE = new EntityMoveTimeLine.EntityMoveTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ENTITY_MOVE};
      }

      public EntityMoveTimeLine.EntityMoveTimeLineBuilder getBuilder() {
         return new EntityMoveTimeLine.EntityMoveTimeLineBuilder();
      }

      public EntityMoveTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityMoveTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort(), buffer.getInt());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         tracker.getEntityManager().moveEntity(param.getWorld().toWorld(server), param.getUUID());
      }
   }
}
