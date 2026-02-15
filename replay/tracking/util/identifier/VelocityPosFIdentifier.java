package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.client.util.math.Vector3f;

import java.util.Objects;


public class VelocityPosFIdentifier extends WorldPosFIdentifier {
   private final Vector3f velocity;

   public VelocityPosFIdentifier(Vector3f pos, WorldTypes world, Vector3f velocity) {
      super(world, pos);
      this.velocity = velocity;
   }

   public Vector3f getVelocity() {
      return this.velocity;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VelocityPosFIdentifier that = (VelocityPosFIdentifier)o;
         return Objects.equals(this.velocity, that.velocity);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.velocity});
   }
}
