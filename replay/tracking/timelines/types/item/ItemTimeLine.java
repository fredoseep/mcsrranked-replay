package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.nio.ByteBuffer;


public abstract class ItemTimeLine<T extends Identifier> extends TimeLine<T> {
   private final Item item;

   protected ItemTimeLine(TimeLineType type, Item item) {
      super(type);
      this.item = item;
   }

   public Item getItem() {
      return this.item;
   }

   public String toString() {
      return Registry.ITEM.getRawId(this.item) + "";
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(4).putInt(Registry.ITEM.getRawId(this.item));
   }
}
