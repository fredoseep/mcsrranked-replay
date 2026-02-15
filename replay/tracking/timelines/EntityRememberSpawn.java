package com.mcsrranked.client.anticheat.replay.tracking.timelines;


import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

public interface EntityRememberSpawn {
   Vec3d ranked$getOriginalSpawnPos();

   Pair<Float, Float> ranked$getOriginalSpawnYawAndPitch();

   void ranked$updateOriginalSpawn();
}
