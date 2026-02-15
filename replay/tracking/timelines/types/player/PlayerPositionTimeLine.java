package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.player;

import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerState;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.PositionFRotationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.WorldTypes;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.EmptyIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.MinecraftServer;

public class PlayerPositionTimeLine extends PositionFRotationTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();

   protected PlayerPositionTimeLine(WorldTypes world, Vector3f position, short yaw, short pitch) {
      super(TimeLineType.PLAYER_POSITION, world, position, yaw, pitch);
   }

   protected PlayerPositionTimeLine(WorldTypes world, Vector3f position, float yaw, float pitch) {
      super(TimeLineType.PLAYER_POSITION, world, position, yaw, pitch);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public EmptyIdentifier getIdentifier() {
      return EMPTY;
   }

   public boolean isPlayerMovementTimeline() {
      return true;
   }

   public void onPlayerStateUpdate(ReplayPlayerState state) {
      state.setWorldType(this.getWorld());
      state.setPos(this.getPosition());
      state.setPitch(this.getPitch());
      state.setYaw(this.getYaw());
   }

   public ByteBuffer toBytes() {
      return super.toBytes();
   }

   public static class PlayerPositionTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;

      public PlayerPositionTimeLineBuilder() {
         this.world = WorldTypes.OVERWORLD;
         this.position = new Vector3f(0.0F, 0.0F, 0.0F);
         this.yaw = 0.0F;
         this.pitch = 0.0F;
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public PlayerPositionTimeLine build() {
         return new PlayerPositionTimeLine(this.world, this.position, this.yaw, this.pitch);
      }
   }

   public static class PlayerPositionTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerPositionTimeLine.PlayerPositionTimeLineFactory INSTANCE = new PlayerPositionTimeLine.PlayerPositionTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_POSITION};
      }

      public PlayerPositionTimeLine.PlayerPositionTimeLineBuilder getBuilder() {
         return new PlayerPositionTimeLine.PlayerPositionTimeLineBuilder();
      }

      public PlayerPositionTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerPositionTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
