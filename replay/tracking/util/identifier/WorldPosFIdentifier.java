package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.client.util.math.Vector3f;

import java.util.Objects;


public class WorldPosFIdentifier extends Identifier {
   private final Vector3f pos;
   private final WorldTypes world;

   public WorldPosFIdentifier(WorldTypes world, Vector3f pos) {
      this.pos = pos;
      this.world = world;
   }

   public Vector3f getPos() {
      return this.pos;
   }

   public WorldTypes getWorld() {
      return this.world;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         WorldPosFIdentifier worldPosFIdentifier = (WorldPosFIdentifier)o;
         return Objects.equals(this.pos, worldPosFIdentifier.pos) && this.world == worldPosFIdentifier.world;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.pos, this.world});
   }
}
