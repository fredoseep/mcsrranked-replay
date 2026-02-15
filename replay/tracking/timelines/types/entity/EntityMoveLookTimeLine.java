package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.RotationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

public class EntityMoveLookTimeLine extends RotationTimeLine<WorldUUIDIdentifier> {
   private final int entityId;

   protected EntityMoveLookTimeLine(WorldTypes world, short yaw, short pitch, int entityId) {
      super(TimeLineType.ENTITY_MOVE_LOOK, world, yaw, pitch);
      this.entityId = entityId;
   }

   protected EntityMoveLookTimeLine(WorldTypes world, float yaw, float pitch, int entityId) {
      super(TimeLineType.ENTITY_MOVE_LOOK, world, yaw, pitch);
      this.entityId = entityId;
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      tracker.getEntityManager().moveEntity(this.getWorld().toWorld(server), this.getEntityUUID(), this.getYaw(), this.getPitch());
   }

   public UUID getEntityUUID() {
      return EntityTimeLine.getEntityUUIDById(this.entityId);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putInt(this.entityId);
   }

   public static class EntityMoveLookTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private float yaw;
      private float pitch;
      private int entityId;

      public EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public EntityMoveLookTimeLine build() {
         return new EntityMoveLookTimeLine(this.world, this.yaw, this.pitch, this.entityId);
      }
   }

   public static class EntityMoveLookTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityMoveLookTimeLine.EntityMoveLookTimeLineFactory INSTANCE = new EntityMoveLookTimeLine.EntityMoveLookTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ENTITY_MOVE_LOOK};
      }

      public EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder getBuilder() {
         return new EntityMoveLookTimeLine.EntityMoveLookTimeLineBuilder();
      }

      public EntityMoveLookTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityMoveLookTimeLine(WorldTypes.values()[buffer.get()], buffer.getShort(), buffer.getShort(), buffer.getInt());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         tracker.getEntityManager().moveEntity(param.getWorld().toWorld(server), param.getUUID());
      }
   }
}
