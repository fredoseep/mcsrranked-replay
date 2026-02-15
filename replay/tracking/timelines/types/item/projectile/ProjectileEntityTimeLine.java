package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item.projectile;

import com.mcsrranked.client.anticheat.replay.ReplayEntityTracker;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.WorldUUIDIdentifier;
import com.mcsrranked.client.utils.ClientUtils;
import java.nio.ByteBuffer;
import java.util.Random;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

public class ProjectileEntityTimeLine extends ProjectileTimeLine<WorldUUIDIdentifier> {
   private final int entityTypeId;
   private final ProjectileEntityTimeLine.SoundPlayType soundPlayType;

   protected ProjectileEntityTimeLine(WorldTypes world, Vector3f pos, short yaw, short pitch, int entityId, Vec3d velocity, int entityTypeId, ProjectileEntityTimeLine.SoundPlayType soundPlayType) {
      super(TimeLineType.PROJECTILE_ENTITY, world, pos, yaw, pitch, entityId, velocity);
      this.entityTypeId = entityTypeId;
      this.soundPlayType = soundPlayType;
   }

   protected ProjectileEntityTimeLine(WorldTypes world, Vector3f pos, float yaw, float pitch, int entityId, Vec3d velocity, int entityTypeId, ProjectileEntityTimeLine.SoundPlayType soundPlayType) {
      super(TimeLineType.PROJECTILE_ENTITY, world, pos, yaw, pitch, entityId, velocity);
      this.entityTypeId = entityTypeId;
      this.soundPlayType = soundPlayType;
   }

   public int getEntityTypeId() {
      return this.entityTypeId;
   }

   public ProjectileEntityTimeLine.SoundPlayType getSoundPlayType() {
      return this.soundPlayType;
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 5).put(superBuffer).putInt(this.entityTypeId).put((byte)this.getSoundPlayType().ordinal());
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!silence) {
         Entity entity = ((EntityType)Registry.ENTITY_TYPE.get(this.getEntityTypeId())).create(this.getWorld().toWorld(server));
         if (entity != null) {
            entity.setUuid(this.getEntityUUID());
            if (entity instanceof ExplosiveProjectileEntity) {
               ((ExplosiveProjectileEntity)entity).posX = this.getVelocity().x;
               ((ExplosiveProjectileEntity)entity).posY = this.getVelocity().y;
               ((ExplosiveProjectileEntity)entity).posZ = this.getVelocity().z;
            } else {
               entity.setVelocity(this.getVelocity());
            }

            ServerWorld world = this.getWorld().toWorld(server);
            ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().spawnEntity(world, entity, this.getPosition(), this.getYaw(), this.getPitch());
            entityTracker.disableFollowPos();
            entityTracker.disableRespawn();
            entityTracker.setVisible(true);
            this.getSoundPlayType().soundPlayer.play(world, this.getPosition(), new Random());
         }

      }
   }

   public WorldUUIDIdentifier getIdentifier() {
      return new WorldUUIDIdentifier(this.getWorld(), this.getEntityUUID());
   }

   public static enum SoundPlayType {
      EMPTY((world, pos, random) -> {
      }),
      GHAST_FIREBALL((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
      }),
      BLAZE_FIREBALL((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
      }),
      ENTITY_TRIDENT((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_DROWNED_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
      }),
      SHULKER_BULLET((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
      }),
      DRAGON_FIREBALL((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
      }),
      WITHER_SKULL((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
      }),
      ENTITY_SHOT_CROSSBOW((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F);
      }),
      SHOOT_SNOWBALL((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, SoundCategory.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
      }),
      FIREWORK_FIRE((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.2F);
      }),
      PLAYER_PEARL_THROW((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
      }),
      ENTITY_POTION_THROW((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_SPLASH_POTION_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
      }),
      SHOOT_ARROW((world, pos, random) -> {
         world.playSound((PlayerEntity)null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
      });

      private final ProjectileEntityTimeLine.SoundPlayer soundPlayer;

      private SoundPlayType(ProjectileEntityTimeLine.SoundPlayer soundPlayer) {
         this.soundPlayer = soundPlayer;
      }

      // $FF: synthetic method
      private static ProjectileEntityTimeLine.SoundPlayType[] $values() {
         return new ProjectileEntityTimeLine.SoundPlayType[]{EMPTY, GHAST_FIREBALL, BLAZE_FIREBALL, ENTITY_TRIDENT, SHULKER_BULLET, DRAGON_FIREBALL, WITHER_SKULL, ENTITY_SHOT_CROSSBOW, SHOOT_SNOWBALL, FIREWORK_FIRE, PLAYER_PEARL_THROW, ENTITY_POTION_THROW, SHOOT_ARROW};
      }
   }

   private interface SoundPlayer {
      void play(ServerWorld var1, Vec3d var2, Random var3);
   }

   public static class ProjectileEntityTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;
      private Vec3d velocity;
      private int entityTypeId;
      private int entityId;
      private ProjectileEntityTimeLine.SoundPlayType soundPlayType;

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setVelocity(Vec3d velocity) {
         this.velocity = velocity;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setVelocity(double x, double y, double z) {
         this.velocity = new Vec3d(x, y, z);
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setEntityTypeId(int entityTypeId) {
         this.entityTypeId = entityTypeId;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setEntityId(int entityId) {
         this.entityId = entityId;
         return this;
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder setSoundType(ProjectileEntityTimeLine.SoundPlayType soundPlayType) {
         this.soundPlayType = soundPlayType;
         return this;
      }

      public ProjectileEntityTimeLine build() {
         return new ProjectileEntityTimeLine(this.world, this.position, this.yaw, this.pitch, this.entityId, this.velocity, this.entityTypeId, this.soundPlayType);
      }
   }

   public static class ProjectileEntityTimeLineFactory implements TimeLineFactorySingleton<WorldUUIDIdentifier> {
      public static final ProjectileEntityTimeLine.ProjectileEntityTimeLineFactory INSTANCE = new ProjectileEntityTimeLine.ProjectileEntityTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PROJECTILE_ENTITY};
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder getBuilder() {
         return new ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder();
      }

      public ProjectileEntityTimeLine.ProjectileEntityTimeLineBuilder getFromEntity(Entity entity, ProjectileEntityTimeLine.SoundPlayType soundPlayType) {
         return INSTANCE.getBuilder().setWorld(WorldTypes.fromDimension(entity.world.getDimension())).setPosition(ClientUtils.vec3dToVector3f(entity.getPos())).setYaw(entity.yaw).setPitch(entity.pitch).setVelocity(entity.getVelocity()).setEntityId(entity.getEntityId()).setEntityTypeId(Registry.ENTITY_TYPE.getRawId(entity.getType())).setSoundType(soundPlayType);
      }

      public ProjectileEntityTimeLine getFromBytes(ByteBuffer buffer) {
         return new ProjectileEntityTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort(), buffer.getInt(), new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble()), buffer.getInt(), ProjectileEntityTimeLine.SoundPlayType.values()[buffer.get()]);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, WorldUUIDIdentifier param) {
         ReplayEntityTracker<?> entityTracker = tracker.getEntityManager().getEntityTracker(param.getUUID());
         if (entityTracker != null) {
            entityTracker.setVisible(false);
         }

      }
   }
}
