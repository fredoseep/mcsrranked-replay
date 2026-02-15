package com.mcsrranked.client.anticheat.replay.file.migrate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.replay.file.ReplayMeta;
import java.util.Iterator;

public class ReplayMetaUnder30 {
   public static ReplayMeta migrate(JsonObject json) {
      JsonObject newData = new JsonObject();
      newData.add("matchId", JsonNull.INSTANCE);
      newData.addProperty("date", json.get("time").getAsLong());
      newData.addProperty("matchType", json.get("matchType").getAsInt());
      newData.addProperty("overworldSeed", json.get("seed").getAsString());
      newData.addProperty("netherSeed", json.get("seed").getAsString());
      newData.addProperty("symmetricKey", json.get("symmetricKey").getAsString());
      JsonObject result = new JsonObject();
      result.add("uuid", JsonNull.INSTANCE);
      result.addProperty("time", 0);
      Iterator var3 = json.getAsJsonArray("finalTimes").iterator();
      if (var3.hasNext()) {
         JsonElement finalTimes = (JsonElement)var3.next();
         result.add("uuid", finalTimes.getAsJsonObject().get("uuid"));
         result.add("time", finalTimes.getAsJsonObject().get("finalTime"));
      }

      newData.add("result", result);
      JsonArray players = new JsonArray();
      Iterator var8 = json.getAsJsonArray("players").iterator();

      while(var8.hasNext()) {
         JsonElement jsonElement = (JsonElement)var8.next();
         JsonObject player = new JsonObject();
         player.add("uuid", jsonElement.getAsJsonObject().get("uuid"));
         player.add("nickname", jsonElement.getAsJsonObject().get("nickname"));
         player.add("roleType", jsonElement.getAsJsonObject().get("badge"));
         player.add("eloRate", jsonElement.getAsJsonObject().get("elo_rate"));
         player.add("eloRank", jsonElement.getAsJsonObject().get("elo_rank"));
         players.add(player);
      }

      newData.add("players", players);
      return (ReplayMeta)MCSRRankedClient.GSON.fromJson(newData, ReplayMeta.class);
   }
}
