package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.item;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.PlayerSlotIdentifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;

public class ItemEquipTimeLine extends ItemTimeLine<PlayerSlotIdentifier> {
   private final byte slot;
   private final boolean enchanted;

   protected ItemEquipTimeLine(Item item, byte slot, boolean enchanted) {
      super(TimeLineType.ITEM_EQUIP, item);
      this.slot = slot;
      this.enchanted = enchanted;
   }

   public int getSlot() {
      return this.slot;
   }

   public boolean isEnchanted() {
      return this.enchanted;
   }

   public String toString() {
      return super.toString() + "," + this.slot + "," + (this.enchanted ? "1" : "0");
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      ItemStack stack = new ItemStack(this.getItem());
      if (this.isEnchanted()) {
         stack.addEnchantment(Enchantments.PROTECTION, 1);
      }

      EquipmentSlot equipSlot = EquipmentSlot.values()[this.getSlot()];
      ((ReplayPlayerEntity)tracker.getReplayPlayerTracker().getEntityTracker().getTarget()).equipStack(equipSlot, stack);
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      ItemStack stack = new ItemStack(this.getItem());
      if (this.isEnchanted()) {
         stack.addEnchantment(Enchantments.PROTECTION, 1);
      }

      tracker.playerInventoryCache.putIfAbsent(tick, new HashMap());
      ((Map)tracker.playerInventoryCache.get(tick)).put(this.slot, stack);
   }

   public PlayerSlotIdentifier getIdentifier() {
      return new PlayerSlotIdentifier(this.slot);
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(2 + superBuffer.remaining()).put(superBuffer).put(this.slot).put((byte)(this.enchanted ? 1 : 0));
   }

   public static class ItemEquipTimeLineBuilder implements TimeLineBuilder {
      private Item item;
      private byte slot;
      private boolean enchanted;

      public ItemEquipTimeLine.ItemEquipTimeLineBuilder setItem(Item item) {
         this.item = item;
         return this;
      }

      public ItemEquipTimeLine.ItemEquipTimeLineBuilder setSlot(byte slot) {
         this.slot = slot;
         return this;
      }

      public ItemEquipTimeLine.ItemEquipTimeLineBuilder setEnchanted(boolean enchanted) {
         this.enchanted = enchanted;
         return this;
      }

      public ItemEquipTimeLine build() {
         return new ItemEquipTimeLine(this.item, this.slot, this.enchanted);
      }
   }

   public static class ItemEquipTimeLineFactory implements TimeLineFactorySingleton<PlayerSlotIdentifier> {
      public static final ItemEquipTimeLine.ItemEquipTimeLineFactory INSTANCE = new ItemEquipTimeLine.ItemEquipTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.ITEM_EQUIP};
      }

      public ItemEquipTimeLine.ItemEquipTimeLineBuilder getBuilder() {
         return new ItemEquipTimeLine.ItemEquipTimeLineBuilder();
      }

      public ItemEquipTimeLine getFromBytes(ByteBuffer buffer) {
         return new ItemEquipTimeLine(Item.byRawId(buffer.getInt()), buffer.get(), buffer.get() == 1);
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, PlayerSlotIdentifier param) {
         ((ReplayPlayerEntity)tracker.getReplayPlayerTracker().getEntityTracker().getTarget()).equipStack(EquipmentSlot.values()[param.getSlot()], new ItemStack(Items.AIR));
      }
   }
}
