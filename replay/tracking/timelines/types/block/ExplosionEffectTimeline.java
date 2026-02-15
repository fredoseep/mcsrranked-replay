package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.block;

import com.google.common.collect.Lists;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionFTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;
import java.util.Iterator;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ExplosionEffectTimeline extends PositionFTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();
   private final float radius;

   protected ExplosionEffectTimeline(WorldTypes world, Vector3f position, float radius) {
      super(TimeLineType.EXPLOSION_EFFECT, world, position);
      this.radius = radius;
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public float getRadius() {
      return this.radius;
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!silence) {
         Iterator var4 = this.getWorld().toWorld(server).getPlayers().iterator();

         while(var4.hasNext()) {
            ServerPlayerEntity player = (ServerPlayerEntity)var4.next();
            player.networkHandler.sendPacket(new ExplosionS2CPacket((double)this.getX(), (double)this.getY(), (double)this.getZ(), this.getRadius(), Lists.newArrayList(), Vec3d.ZERO));
         }

      }
   }

   public ByteBuffer toBytes() {
      ByteBuffer superBuffer = (ByteBuffer)super.toBytes().rewind();
      return ByteBuffer.allocate(superBuffer.remaining() + 4).put(superBuffer).putFloat(this.radius);
   }

   public static class ExplosionEffectTimelineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float radius;

      public ExplosionEffectTimeline.ExplosionEffectTimelineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public ExplosionEffectTimeline.ExplosionEffectTimelineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public ExplosionEffectTimeline.ExplosionEffectTimelineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public ExplosionEffectTimeline.ExplosionEffectTimelineBuilder setRadius(float radius) {
         this.radius = radius;
         return this;
      }

      public ExplosionEffectTimeline build() {
         return new ExplosionEffectTimeline(this.world, this.position, this.radius);
      }
   }

   public static class ExplosionEffectTimelineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final ExplosionEffectTimeline.ExplosionEffectTimelineFactory INSTANCE = new ExplosionEffectTimeline.ExplosionEffectTimelineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.EXPLOSION_EFFECT};
      }

      public ExplosionEffectTimeline.ExplosionEffectTimelineBuilder getBuilder() {
         return new ExplosionEffectTimeline.ExplosionEffectTimelineBuilder();
      }

      public ExplosionEffectTimeline getFromBytes(ByteBuffer buffer) {
         return new ExplosionEffectTimeline(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getFloat());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
