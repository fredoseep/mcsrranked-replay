package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.projectile;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

public class ItemEntityTimeLine extends ProjectileTimeLine<WorldUUIDIdentifier> {
   private final Item item;

   protected ItemEntityTimeLine(WorldTypes world, Vector3f pos, short yaw, short pitch, int entityId, Vec3d velocity, Item item) {
      super(TimeLineType.ITEM_ENTITY, world, pos, yaw, pitch, entityId, velocity);
      this.item = item;
   }

   protected ItemEntityTimeLine(WorldTypes world, Vector3f pos, float yaw, float pitch, int entityId, Vec3d velocity, Item item) {
      super(TimeLineType.ITEM_ENTITY, world, pos, yaw, pitch, entityId, velocity);
      this.item = item;
   }

   public Item getItem() {
      return this.item;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putInt(Item.getRawId(this.item));
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ItemEntity itemEntity = new ItemEntity(this.getWorld().toWorld(server), (double)this.getX(), (double)this.getY(), (double)this.getZ(), new ItemStack(this.getItem()));
      itemEntity.setVelocity(this.getVelocity());
      itemEntity.setUuid(this.getEntityUUID());
      itemEntity.setPickupDelayInfinite();
      ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().spawnEntity(this.getWorld().toWorld(server), itemEntity, this.getPosition(), this.getYaw(), this.getPitch());
      entityTracker.disableFollowPos();
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public static class ItemEntityTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;
      private Vec3d velocity;
      private Item item;
      private int entityId;

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setVelocity(Vec3d velocity) {
         this.velocity = velocity;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setItem(Item item) {
         this.item = item;
         return this;
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public ItemEntityTimeLine build() {
         return new ItemEntityTimeLine(this.world, this.position, this.yaw, this.pitch, this.entityId, this.velocity, this.item);
      }
   }

   public static class ItemEntityTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final ItemEntityTimeLine.ItemEntityTimeLineFactory INSTANCE = new ItemEntityTimeLine.ItemEntityTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ITEM_ENTITY};
      }

      public ItemEntityTimeLine.ItemEntityTimeLineBuilder getBuilder() {
         return new ItemEntityTimeLine.ItemEntityTimeLineBuilder();
      }

      public ItemEntityTimeLine getFromBytes(ByteBuffer buffer) {
         return new ItemEntityTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort(), buffer.getInt(), new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble()), Item.byRawId(buffer.getInt()));
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().getEntityTracker(param.getUUID());
         if (entityTracker != null) {
            entityTracker.setVisible(false);
         }

      }
   }
}
