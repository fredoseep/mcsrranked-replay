package com.mcsrranked.client.anticheat.replay.tracking.timelines;

import com.mcsrranked.client.anticheat.replay.tracking.timelines.types.TimeLine;

public interface TimeLineBuilder {
   TimeLine<?> build();
}
