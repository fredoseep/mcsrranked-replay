package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import java.util.Objects;

public class PlayerSlotIdentifier extends Identifier {
   private final byte slot;

   public PlayerSlotIdentifier(byte slot) {
      this.slot = slot;
   }

   public byte getSlot() {
      return this.slot;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PlayerSlotIdentifier that = (PlayerSlotIdentifier)o;
         return this.slot == that.slot;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.slot});
   }
}
