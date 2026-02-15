package com.mcsrranked.client.anticheat.replay.tracking.timelines;

import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;
import com.mcsrranked.client.anticheat.replay.tracking.util.identifier.Identifier;
import java.nio.ByteBuffer;
import net.minecraft.server.MinecraftServer;

public interface TimeLineFactorySingleton<H extends Identifier> {
   TimeLineBuilder getBuilder();

   TimeLine<?> getFromBytes(ByteBuffer var1);

   void defaultExecute(OpponentPlayerTracker var1, MinecraftServer var2, H var3);

   TimeLineType[] getInvertedTypes();
}
