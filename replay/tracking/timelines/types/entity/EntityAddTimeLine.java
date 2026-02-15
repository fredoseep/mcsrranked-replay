package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.entity;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;
import java.util.UUID;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.registry.Registry;
import net.minecraft.item.Item;

public class EntityAddTimeLine extends EntityTimeLine<WorldUUIDIdentifier> {
   private final int entityTypeId;
   private final int customData;

   protected EntityAddTimeLine(WorldTypes world, Vector3f position, short yaw, short pitch, int entityId, int entityTypeId, int customData) {
      super(TimeLineType.ENTITY_ADD, world, position, yaw, pitch, entityId);
      this.entityTypeId = entityTypeId;
      this.customData = customData;
   }

   protected EntityAddTimeLine(WorldTypes world, Vector3f position, float yaw, float pitch, int entityId, int entityTypeId, int customData) {
      super(TimeLineType.ENTITY_ADD, world, position, yaw, pitch, entityId);
      this.entityTypeId = entityTypeId;
      this.customData = customData;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      createEntity(tracker, this.getEntityTypeId(), this.getWorld().toWorld(server), this.getEntityUUID(), this.getPosition(), this.getYaw(), this.getPitch(), this.getCustomData(), this.getEntityId(), silence);
   }

   private static void createEntity(OpponentPlayerTracker tracker, int type, ServerWorld world, UUID uuid, Vec3d pos, float yaw, float pitch, int customData, @Nullable Integer entityId, boolean silence) {
      try {
         Entity entity = ((EntityType)Registry.ENTITY_TYPE.get(type)).create(world);
         if (entity != null) {
            if (entity instanceof SlimeEntity) {
               ((SlimeEntity)entity).setSize(customData, false);
            } else if (entity instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity;
               if (entity instanceof MobEntity) {
                  ((MobEntity)entity).setBaby(customData < 0);
               }

               if (customData != Integer.MIN_VALUE) {
                  livingEntity.setStackInHand(livingEntity.getActiveHand(), new ItemStack(Item.byRawId(Math.abs(customData))));
               }
            } else if (entity instanceof TntEntity) {
               if (silence) {
                  return;
               }

               entity = new TntEntity(world, pos.x, pos.y, pos.z, (LivingEntity)null);
            }

            ((Entity)entity).setUuid(uuid);
            if (entity instanceof EnderDragonEntity && entityId != null) {
               EnderDragonPart[] parts = ((EnderDragonEntity)entity).getBodyParts();

               for(int i = 0; i < parts.length; ++i) {
                  parts[i].setUuid(getEntityUUIDById(entityId + i));
               }
            }

            ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().spawnEntity(world, (Entity)entity, pos, yaw, pitch);
            if (entity instanceof TntEntity) {
               entityTracker.disableFollowPos();
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public int getEntityTypeId() {
      return this.entityTypeId;
   }

   public int getCustomData() {
      return this.customData;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 8).put(superBuffer).putInt(this.entityTypeId).putInt(this.customData);
   }

   public static class EntityAddTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;
      private int entityId;
      private int entityTypeId;
      private int customData;

      public EntityAddTimeLine.EntityAddTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder setData(Entity entity) {
         if (entity instanceof SlimeEntity) {
            this.customData = ((SlimeEntity)entity).getSize();
         } else if (entity instanceof LivingEntity) {
            this.customData = Item.getRawId(((LivingEntity)entity).getMainHandStack().getItem());
            if (((LivingEntity)entity).isBaby()) {
               this.customData = this.customData == 0 ? Integer.MIN_VALUE : -this.customData;
            }
         } else if (entity instanceof TntEntity) {
            this.customData = ((TntEntity)entity).getFuse();
         }

         this.entityTypeId = Registry.ENTITY_TYPE.getRawId(entity.getType());
         return this;
      }

      public EntityAddTimeLine build() {
         return new EntityAddTimeLine(this.world, this.position, this.yaw, this.pitch, this.entityId, this.entityTypeId, this.customData);
      }
   }

   public static class EntityAddTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final EntityAddTimeLine.EntityAddTimeLineFactory INSTANCE = new EntityAddTimeLine.EntityAddTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ENTITY_ADD, TimeLineType.ENTITY_REMOVE};
      }

      public EntityAddTimeLine.EntityAddTimeLineBuilder getBuilder() {
         return new EntityAddTimeLine.EntityAddTimeLineBuilder();
      }

      public EntityAddTimeLine getFromBytes(ByteBuffer buffer) {
         return new EntityAddTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort(), buffer.getInt(), buffer.getInt(), buffer.getInt());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().getEntityTracker(param.getUUID());
         if (entityTracker != null) {
            entityTracker.setVisible(false);
         }

      }
   }
}
