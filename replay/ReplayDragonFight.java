package com.mcsrranked.client.anticheat.replay;

import com.google.common.collect.Lists;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeDragonSpawnable;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.WorldEntityAddable;
import com.mcsrranked.client.utils.ClientUtils;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.system.CallbackI;

public class ReplayDragonFight {
   public static final float WING_MULTIPLIER = 0.025F;
   private EnderDragonEntity dragonEntity = null;
   private final OpponentPlayerTracker tracker;
   private final ConcurrentHashMap<Integer, Integer> dragonHealthTimeline = new ConcurrentHashMap();
   private final ConcurrentHashMap<Integer, Integer> crystalDestroyTimeline = new ConcurrentHashMap();
   private boolean hasInit = false;

   public ReplayDragonFight(OpponentPlayerTracker tracker) {
      this.tracker = tracker;
   }

   public void refresh(int tick, MinecraftServer server) {
      List<EndSpikeFeature.Spike> crystals;
      if (this.tracker.isGhostMode()) {
         if (this.dragonEntity != null && !this.dragonEntity.removed) {
            this.dragonEntity.removed = true;
         }

         Iterator var14 = server.getWorlds().iterator();

         while(true) {
            ServerWorld world;
            EnderDragonFight endFight;
            do {
               if (!var14.hasNext()) {
                  this.hasInit = true;
                  return;
               }

               world = (ServerWorld)var14.next();
               endFight = world.getEnderDragonFight();
            } while(endFight == null);

            List<EndSpikeFeature.Spike> spikes = EndSpikeFeature.getSpikes(world);
            Iterator var18 = spikes.iterator();

            while(var18.hasNext()) {
               EndSpikeFeature.Spike spike = (EndSpikeFeature.Spike)var18.next();

               if (world.isChunkLoaded(spike.getCenterX() >> 4, spike.getCenterZ() >> 4)) {
                  List<EndCrystalEntity> entitiesToRemove = world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox());
                  entitiesToRemove.forEach(Entity::remove);
               }
            }
         }
      } else {
         this.hasInit = false;
         Optional<Integer> lastTickTimeline = this.dragonHealthTimeline.keySet().stream().filter((ix) -> {
            return ix <= tick;
         }).max(Comparator.naturalOrder());
         ConcurrentHashMap var10001 = this.dragonHealthTimeline;
         Objects.requireNonNull(var10001);
         int targetHealth = lastTickTimeline
                 .map(obj -> (Integer) var10001.get(obj))
                 .orElse(200);
         List<Integer> destroyCrystals = Lists.newArrayList();
         this.crystalDestroyTimeline.keySet().stream().filter((ix) -> {
            return ix <= tick;
         }).forEach((key) -> {
            destroyCrystals.add((Integer)this.crystalDestroyTimeline.get(key));
         });
         Iterator var6 = server.getWorlds().iterator();

         while(true) {
            ServerWorld world;
            EnderDragonFight endFight;
            do {
               if (!var6.hasNext()) {
                  this.getDragon().ifPresent((dragon) -> {
                     dragon.setHealth(targetHealth >= 0 ? (float)Math.max(1, targetHealth) : 0.0F);
                  });
                  return;
               }

               world = (ServerWorld)var6.next();
               endFight = world.getEnderDragonFight();
            } while(endFight == null);

            crystals = EndSpikeFeature.getSpikes(world);
            Iterator var10 = crystals.iterator();

            EndSpikeFeature.Spike spike;
            while(var10.hasNext()) {
               spike = (EndSpikeFeature.Spike)var10.next();
               if (!world.isChunkLoaded(spike.getCenterX() >> 4, spike.getCenterZ() >> 4)) {
                  return;
               }
            }

            for(int i = 0; i < crystals.size(); ++i) {
               spike = (EndSpikeFeature.Spike)crystals.get(i);
               List<EndCrystalEntity> foundCrystals = world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox());

               if (destroyCrystals.contains(i)) {
                  foundCrystals.forEach(Entity::remove);
               } else if (foundCrystals.isEmpty()) {
                  EndCrystalEntity endCrystalEntity = (EndCrystalEntity)EntityType.END_CRYSTAL.create(world.getWorld());
                  assert endCrystalEntity != null;
                  endCrystalEntity.refreshPositionAndAngles(
                          (double)spike.getCenterX() + 0.5D,
                          (double)(spike.getHeight() + 1),
                          (double)spike.getCenterZ() + 0.5D,
                          (new Random()).nextFloat() * 360.0F,
                          0.0F
                  );
                  ((WorldEntityAddable)world).ranked$spawnEntityInReplay(endCrystalEntity);
               }
            }

            endFight.resetEndCrystals();
            if ((Boolean)this.getDragon().map((dragonEntity) -> {
               return dragonEntity.ticksSinceDeath > 0;
            }).orElse(false)) {
               this.dragonEntity.remove();
               this.dragonEntity = null;
               endFight.respawnDragon();
               this.dragonEntity = ((TimeDragonSpawnable)endFight).replay$createDummyDragon();
            }

            if (targetHealth >= 0 && !this.getDragon().isPresent()) {
               endFight.respawnDragon();
               this.dragonEntity = ((TimeDragonSpawnable)endFight).replay$createDummyDragon();
            }

            if (this.dragonEntity != null) {
               this.dragonEntity.getPhaseManager().setPhase(targetHealth <= 0 ? PhaseType.DYING : PhaseType.HOLDING_PATTERN);
            }

            this.hasInit = true;
         }
      }
   }

   public boolean hasInit() {
      return this.hasInit;
   }

   public void tick(int tick, MinecraftServer server) {
      this.getDragon().ifPresent((dragon) -> {
         if (dragon.getPhaseManager().getCurrent().getType() == PhaseType.DYING) {
            dragon.connectedCrystal = null;
         }

      });
      if (this.getDragon().isPresent() && ((EnderDragonEntity)this.getDragon().get()).removed) {
         this.dragonEntity = null;
      } else {
         if (this.dragonHealthTimeline.containsKey(tick)) {
            int health = (Integer)this.dragonHealthTimeline.get(tick);
            this.getDragon().ifPresent((dragon) -> {
               if (health == 0) {
                  dragon.getPhaseManager().setPhase(PhaseType.DYING);
                  dragon.damage(DamageSource.GENERIC, 1.0F);
                  dragon.setHealth(0.0141F);
               } else if (health < 0) {
                  dragon.setHealth(0.0F);
                  dragon.damage(DamageSource.GENERIC, 2.14748365E9F);
                  this.dragonEntity = null;
               } else {
                  dragon.damage(DamageSource.GENERIC, 1.0F);
                  dragon.setHealth((float)health);
               }

               if (!dragon.world.isClient()) {
                  dragon.world.sendEntityStatus(dragon, (byte)2);
                  ClientUtils.playSound(SoundEvents.ENTITY_ENDER_DRAGON_HURT, dragon.getSoundCategory(), dragon, 5.0F, (dragon.getRandom().nextFloat() - dragon.getRandom().nextFloat()) * 0.2F + 1.0F);
               }

            });
         }

         if (this.crystalDestroyTimeline.containsKey(tick)) {
            Iterator var9 = server.getWorlds().iterator();

            while(true) {
               while(true) {
                  ServerWorld world;
                  do {
                     if (!var9.hasNext()) {
                        return;
                     }

                     world = (ServerWorld)var9.next();
                  } while(world.getEnderDragonFight() == null);

                  int crystalIndex = (Integer)this.crystalDestroyTimeline.get(tick);
                  List<EndSpikeFeature.Spike> spikes = EndSpikeFeature.getSpikes(world);

                  for(int i = 0; i < spikes.size(); ++i) {
                     if (i == crystalIndex) {
                        EndSpikeFeature.Spike spike = (EndSpikeFeature.Spike)spikes.get(i);
                        world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox()).forEach(Entity::remove);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public void onUpdateHealth(int tick, int health) {
      this.dragonHealthTimeline.put(tick, health);
   }

   public void onDestroyCrystal(int tick, int index) {
      this.crystalDestroyTimeline.put(tick, index);
   }

   public Optional<EnderDragonEntity> getDragon() {
      return this.dragonEntity != null && this.dragonEntity.getHealth() != 0.0F && !this.dragonEntity.removed ? Optional.of(this.dragonEntity) : Optional.empty();
   }

   public void clear() {
      if (this.dragonEntity != null) {
         this.dragonEntity.remove();
         this.dragonEntity = null;
      }

      this.crystalDestroyTimeline.clear();
      this.dragonHealthTimeline.clear();
   }
}
