package com.mcsrranked.client.anticheat.replay.tracking.timelines.types.dragon;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineBuilder;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineFactorySingleton;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.TimeLineType;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.RotationTimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.CustomIdentifier;
import java.nio.ByteBuffer;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.MinecraftServer;

public class DragonPositionTimeLine extends TimeLine<CustomIdentifier> {
   private static final byte BYTE = 2;
   private final Vector3f position;
   private final short yaw;
   private final short pitch;

   protected DragonPositionTimeLine(Vector3f position, short yaw, short pitch) {
      super(TimeLineType.DRAGON_MOVE);
      this.position = position;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   protected DragonPositionTimeLine(Vector3f position, float yaw, float pitch) {
      this(position, RotationTimeLine.convertRotation(yaw), RotationTimeLine.convertRotation(pitch));
   }

   public Vector3f getPosition() {
      return this.position;
   }

   public float getPitch() {
      return RotationTimeLine.convertValue(this.pitch);
   }

   public float getYaw() {
      return RotationTimeLine.convertValue(this.yaw);
   }

   public ByteBuffer toBytes() {
      return ByteBuffer.allocate(16).putFloat(this.getPosition().getX()).putFloat(this.getPosition().getY()).putFloat(this.getPosition().getZ()).putShort(this.yaw).putShort(this.pitch);
   }

   public void runTimeLine(OpponentPlayerTracker tracker, MinecraftServer server, boolean silence) {
      if (!tracker.getDragonFight().hasInit()) {
         tracker.getDragonFight().refresh(tracker.getCurrentTicks(), server);
      }

      tracker.getDragonFight().getDragon().ifPresent((enderDragonEntity) -> {
         enderDragonEntity.refreshPositionAndAngles((double)this.getPosition().getX(), (double)this.getPosition().getY(), (double)this.getPosition().getZ(), this.getYaw(), this.getPitch());
      });
   }

   public CustomIdentifier getIdentifier() {
      return new CustomIdentifier((byte)2);
   }

   public static class DragonPositionTimeLineBuilder implements TimeLineBuilder {
      private Vector3f position;
      private float yaw;
      private float pitch;

      public DragonPositionTimeLine.DragonPositionTimeLineBuilder setPosition(Vector3f position) {
         this.position = position;
         return this;
      }

      public DragonPositionTimeLine.DragonPositionTimeLineBuilder setYaw(float yaw) {
         this.yaw = yaw;
         return this;
      }

      public DragonPositionTimeLine.DragonPositionTimeLineBuilder setPitch(float pitch) {
         this.pitch = pitch;
         return this;
      }

      public DragonPositionTimeLine.DragonPositionTimeLineBuilder setPosition(float x, float y, float z) {
         this.position = new Vector3f(x, y, z);
         return this;
      }

      public DragonPositionTimeLine build() {
         return new DragonPositionTimeLine(this.position, this.yaw, this.pitch);
      }
   }

   public static class DragonPositionTimeLineFactory implements TimeLineFactorySingleton<CustomIdentifier> {
      public static final DragonPositionTimeLine.DragonPositionTimeLineFactory INSTANCE = new DragonPositionTimeLine.DragonPositionTimeLineFactory();

      public TimeLineType[] getInvertedTypes() {
         return new TimeLineType[0];
      }

      public DragonPositionTimeLine.DragonPositionTimeLineBuilder getBuilder() {
         return new DragonPositionTimeLine.DragonPositionTimeLineBuilder();
      }

      public DragonPositionTimeLine getFromBytes(ByteBuffer buffer) {
         return new DragonPositionTimeLine(new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()), buffer.getShort(), buffer.getShort());
      }

      public void defaultExecute(OpponentPlayerTracker tracker, MinecraftServer server, CustomIdentifier param) {
      }
   }
}
