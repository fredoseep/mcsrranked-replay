package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import java.util.Objects;
import java.util.UUID;

public class WorldUUIDIdentifier extends Identifier {
   private final WorldTypes world;
   private final UUID uuid;

   public WorldUUIDIdentifier(WorldTypes world, UUID uuid) {
      this.world = world;
      this.uuid = uuid;
   }

   public WorldTypes getWorld() {
      return this.world;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!this.getClass().isInstance(o)) {
         return false;
      } else {
         WorldUUIDIdentifier worldUUIDIdentifier = (WorldUUIDIdentifier)o;
         return this.world == worldUUIDIdentifier.world && Objects.equals(this.uuid, worldUUIDIdentifier.uuid);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.world, this.uuid});
   }
}
