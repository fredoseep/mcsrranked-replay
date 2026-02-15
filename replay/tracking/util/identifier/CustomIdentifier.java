package com.mcsrranked.client.anticheat.replay.tracking.util.identifier;

import java.util.Objects;

public class CustomIdentifier extends Identifier {
   private final byte value;

   public CustomIdentifier(byte value) {
      this.value = value;
   }

   public byte getValue() {
      return this.value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CustomIdentifier that = (CustomIdentifier)o;
         return this.value == that.value;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.value});
   }
}
