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
import net.minecraft.entity.EntityPose;
import net.minecraft.server.MinecraftServer;

public class PlayerSpawnTimeLine extends PositionFRotationTimeLine<EmptyIdentifier> {
   private static final EmptyIdentifier EMPTY = new EmptyIdentifier();

   protected PlayerSpawnTimeLine(WorldTypes world, Vector3f position, short yaw, short pitch) {
      super(TimeLineType.PLAYER_SPAWN, world, position, yaw, pitch);
   }

   protected PlayerSpawnTimeLine(WorldTypes world, Vector3f position, float yaw, float pitch) {
      super(TimeLineType.PLAYER_SPAWN, world, position, yaw, pitch);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
   }

   public void onInit(OpponentPlayerTracker tracker, int tick) {
      tracker.playerDimensionCache.put(tick, this.getWorld());
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
      state.setPose(EntityPose.STANDING);
      state.setDeath(false);
      state.setVisible(true);
      state.setInanimate();
   }

   public static class PlayerSpawnTimeLineBuilder implements TimeLineBuilder {
      private WorldTypes world;
      private Vector3f position;
      private float yaw;
      private float pitch;

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder setWorld(WorldTypes world) {
         this.world = world;
         return this;
      }

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public PlayerSpawnTimeLine build() {
         return new PlayerSpawnTimeLine(this.world, this.position, this.yaw, this.pitch);
      }
   }

   public static class PlayerSpawnTimeLineFactory implements TimeLineFactorySingleton<EmptyIdentifier> {
      public static final PlayerSpawnTimeLine.PlayerSpawnTimeLineFactory INSTANCE = new PlayerSpawnTimeLine.PlayerSpawnTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[]{TimeLineType.PLAYER_SPAWN, TimeLineType.PLAYER_REMOVE, TimeLineType.PLAYER_POSITION, TimeLineType.PLAYER_POSITION_LOOK, TimeLineType.PLAYER_POSITION_POS};
      }

      public PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder getBuilder() {
         return new PlayerSpawnTimeLine.PlayerSpawnTimeLineBuilder();
      }

      public PlayerSpawnTimeLine getFromBytes(ByteBuffer buffer) {
         return new PlayerSpawnTimeLine(WorldTypes.values()[buffer.get()], new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, EmptyIdentifier param) {
      }
   }
}
