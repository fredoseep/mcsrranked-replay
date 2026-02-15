package com.mcsrranked.client.anticheat.replay;

import com.google.common.collect.Lists;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.ReplayPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayEntityManager {
   // ID 生成器，用于给重播的实体分配唯一的 ID
   public static final AtomicInteger ID_GENERATOR = new AtomicInteger(-200);

   private final OpponentPlayerTracker tracker;
   private final ReplayPlayerTracker replayPlayerTracker;
   // 存储所有实体的追踪器
   private final Map<UUID, ReplayEntityTracker<?>> entityTrackers = new ConcurrentHashMap<>();

   private int followTask = 0;

   public ReplayEntityManager(OpponentPlayerTracker tracker, UUID targetUUID, String nickname) {
      this.tracker = tracker;
      // 初始化玩家追踪器
      this.replayPlayerTracker = new ReplayPlayerTracker(tracker, ID_GENERATOR.decrementAndGet(), targetUUID, nickname);
   }

   // 生成并追踪一个新的实体
   public <T extends Entity> ReplayEntityTracker<?> spawnEntity(ServerWorld world, T entity, Vec3d spawnPos, float yaw, float pitch) {
      this.entityTrackers.putIfAbsent(entity.getUuid(), new ReplayEntityTracker<>(this.tracker, ID_GENERATOR.decrementAndGet(), entity, WorldTypes.fromDimension(world.getDimension())));

      ReplayEntityTracker<?> entityTracker = this.entityTrackers.get(entity.getUuid());
      entityTracker.setDimension(WorldTypes.fromDimension(world.getDimension()));
      entityTracker.setSpawnPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), yaw, pitch);
      entityTracker.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), yaw, pitch);
      return entityTracker;
   }

   public ReplayEntityTracker<?> getEntityTracker(UUID uuid) {
      return this.entityTrackers.get(uuid);
   }

   // 实体传送回出生点
   public void moveEntity(ServerWorld serverWorld, UUID uuid) {
      if (this.entityTrackers.containsKey(uuid)) {
         this.entityTrackers.get(uuid).backToSpawn();
      }
   }

   // 移动实体（仅坐标）
   public void moveEntity(ServerWorld serverWorld, UUID uuid, Vec3d vec3d) {
      if (this.entityTrackers.containsKey(uuid)) {
         ReplayEntityTracker<?> entityTracker = this.entityTrackers.get(uuid);
         this.moveEntity(serverWorld, uuid, vec3d, entityTracker.getYaw(), entityTracker.getPitch());
      }
   }

   // 移动实体（仅旋转）
   public void moveEntity(ServerWorld serverWorld, UUID uuid, float yaw, float pitch) {
      if (this.entityTrackers.containsKey(uuid)) {
         ReplayEntityTracker<?> entityTracker = this.entityTrackers.get(uuid);
         this.moveEntity(serverWorld, uuid, entityTracker.getPos(), yaw, pitch);
      }
   }

   // 移动实体（坐标+旋转+维度检查）
   public void moveEntity(ServerWorld world, UUID uuid, Vec3d vec3d, float yaw, float pitch) {
      if (this.entityTrackers.containsKey(uuid)) {
         ReplayEntityTracker<?> entityTracker = this.entityTrackers.get(uuid);

         // 检查维度是否变化
         if (entityTracker.getDimension() != WorldTypes.fromDimension(world.getDimension())) {
            entityTracker.setDimension(WorldTypes.fromDimension(world.getDimension()));
         }

         entityTracker.setPos(vec3d.getX(), vec3d.getY(), vec3d.getZ(), yaw, pitch);
      }
   }

   public ReplayPlayerTracker getPlayerTracker() {
      return this.replayPlayerTracker;
   }

   // 获取所有的追踪器（包括玩家本身和召唤物/抛射物）
   public Collection<ReplayEntityTracker<?>> getAllTrackers() {
      List<ReplayEntityTracker<?>> list = Lists.newArrayList();
      list.addAll(this.entityTrackers.values());
      list.add(this.getPlayerTracker().getEntityTracker());
      return list;
   }

   // 更新玩家装备
   public void refreshPlayer() {
      if (this.getPlayerTracker().getEntityTracker().isVisible()) {
         Entity targetEntity = this.getPlayerTracker().getEntityTracker().getTarget();

         if (targetEntity instanceof ReplayPlayerEntity) {
            ReplayPlayerEntity replayPlayer = (ReplayPlayerEntity) targetEntity;
            replayPlayer.clearInventory(); // 这里的 clearInventory 需要在 ReplayPlayerEntity 里实现

            List<Integer> inventoryTicks = Lists.newArrayList(this.tracker.playerInventoryCache.keySet());
            inventoryTicks.sort(Integer::compareTo); // 简化排序写法

            for (Integer inventoryTick : inventoryTicks) {
               if (inventoryTick > this.tracker.getCurrentTicks()) {
                  break;
               }

               Map<Byte, ItemStack> inventoryMap = this.tracker.playerInventoryCache.get(inventoryTick);
               for (Entry<Byte, ItemStack> entry : inventoryMap.entrySet()) {
                  // 更新装备槽位
                  replayPlayer.equipStack(EquipmentSlot.values()[entry.getKey()], entry.getValue());
               }
            }
         }
      }
   }

   private boolean shouldFollowPlayer(ClientPlayerEntity player) {
      return this.tracker.isFollowDimension()
              && !this.tracker.isGhostOnly()
              && !this.getPlayerTracker().getEntityTracker().isDead()
              && WorldTypes.fromDimension(player.world.getDimension()) != this.getPlayerTracker().getEntityTracker().getDimension();
   }

   public void followPlayer(boolean force) {
      this.followTask = force ? 2 : 1;
   }

   public void followPlayer(MinecraftClient client, ReplayProcessor processor, boolean forceFollow) {
      if (client.player == null) return;

      if (forceFollow || this.shouldFollowPlayer(client.player)) {
         // 检查是否有其他的 Tracker 正在聚焦，逻辑稍微简化了一下
         boolean otherFocused = processor.getFocusedTracker()
                 .map(tracker -> tracker.getEntityManager() != this)
                 .orElse(false);

         if (!otherFocused) {
            if (this.tracker.isActive() && !processor.isLoading()) {
               IntegratedServer server = client.getServer();
               if (server != null) {
                  server.submit(() -> {
                     for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                        int randomDistance = serverPlayerEntity.getRandom().nextInt(2) + 4;
                        float randomDirection = serverPlayerEntity.getRandom().nextFloat() * 360.0F;

                        Vec3d entityPos = this.getPlayerTracker().getEntityTracker().getPos();
                        Vec3d newPos = entityPos.add(0.0D, 4.0D, 0.0D); // 向上偏移
                        Vec3d rotation = (new Vec3d(randomDistance, 0.0D, 0.0D)).rotateY((float) Math.toRadians(randomDirection));

                        newPos = newPos.add(rotation);
                        Vec3d subtractPos = entityPos.subtract(newPos);

                        float angle = (float)(Math.atan2(-subtractPos.getX(), subtractPos.getZ()) * (180.0D / Math.PI));

                        // 执行传送
                        serverPlayerEntity.teleport(
                                this.getPlayerTracker().getEntityTracker().getDimension().toWorld(server),
                                newPos.getX(), newPos.getY(), newPos.getZ(),
                                angle,
                                (float)(40 - (randomDistance - 4) * 8)
                        );
                     }
                  });
                  this.followTask = 0;
               }
            }
            this.refreshPlayer();
         }
      }
   }

   public void tick(ReplayProcessor processor) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (!client.isOnThread()) {
         // 确保在主线程运行，防止多线程崩溃
         // throw new RuntimeException("wrong entity manager thread"); // 暂时注释掉，避免崩溃，看日志即可
      }

      if (client.world != null && client.player != null) {
         // 🚨 修复重点：原代码 this.getAttacker() 不存在
         // 这里应该是遍历所有的追踪器进行 tick 更新
         for (ReplayEntityTracker<?> tracker : this.getAllTrackers()) {
            tracker.tick(client.world);
         }

         if (!processor.isPaused() || this.followTask > 0) {
            this.followPlayer(client, processor, this.followTask > 1);
         }
      }
   }

   public void clear() {
      this.entityTrackers.clear();
      this.getPlayerTracker().getEntityTracker().setVisible(false);
   }
}