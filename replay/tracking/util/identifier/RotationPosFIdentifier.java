package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import net.minecraft.client.util.math.Vector3f;

import java.util.Objects;


public class RotationPosFIdentifier extends WorldPosFIdentifier {
   private final float yaw;
   private final float pitch;

   public RotationPosFIdentifier(Vector3f pos, WorldTypes world, float yaw, float pitch) {
      super(world, pos);
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RotationPosFIdentifier that = (RotationPosFIdentifier)o;
         return Float.compare(that.yaw, this.yaw) == 0 && Float.compare(that.pitch, this.pitch) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.yaw, this.pitch});
   }
}
