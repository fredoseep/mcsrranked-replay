package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import java.util.Objects;
import java.util.UUID;

public class UUIDIdentifier extends Identifier {
   private final UUID uuid;

   public UUIDIdentifier(UUID uuid) {
      this.uuid = uuid;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         UUIDIdentifier that = (UUIDIdentifier)o;
         return Objects.equals(this.uuid, that.uuid);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.uuid});
   }
}
