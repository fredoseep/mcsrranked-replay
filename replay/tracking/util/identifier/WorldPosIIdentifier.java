package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;


public class WorldPosIIdentifier extends Identifier {
   private final Vec3i pos;
   private final WorldTypes world;

   public WorldPosIIdentifier(WorldTypes world, Vec3i pos) {
      this.pos = pos;
      this.world = world;
   }

   public Vec3i getPos() {
      return this.pos;
   }

   public WorldTypes getWorld() {
      return this.world;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         WorldPosIIdentifier worldPosIIdentifier = (WorldPosIIdentifier)o;
         return Objects.equals(this.pos, worldPosIIdentifier.pos) && this.world == worldPosIIdentifier.world;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.pos, this.world});
   }
}
