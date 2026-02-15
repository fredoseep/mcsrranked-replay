package com.mcsrranked.client.anticheat.replay.tracking.util;


import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public enum WorldTypes {
   OVERWORLD(-1715414785),
   NETHER(-1724708856),
   END(-1725756073);

   private final int color;

   private WorldTypes(int color) {
      this.color = color;
   }

   public int getColor() {
      return this.color;
   }

   public static WorldTypes fromDimension(DimensionType dimension) {
      if (dimension == null) {
         return OVERWORLD;
      } else if (dimension.isBedWorking()) {
         return OVERWORLD;
      } else if (dimension.isRespawnAnchorWorking()) {
         return NETHER;
      } else {
         return dimension.hasEnderDragonFight() ? END : OVERWORLD;
      }
   }

   public static WorldTypes current() {
      return MinecraftClient.getInstance().world == null ? OVERWORLD : fromDimension(MinecraftClient.getInstance().world.getDimension());
   }

   public ServerWorld toWorld(MinecraftServer server) {
      RegistryKey<World> key = this.ordinal() == 0 ? World.OVERWORLD : (this.ordinal() == 1 ? World.NETHER : World.END);
      return server.getWorld(key);
   }

   // $FF: synthetic method
   private static WorldTypes[] $values() {
      return new WorldTypes[]{OVERWORLD, NETHER, END};
   }
}
