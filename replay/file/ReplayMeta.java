package com.mcsrranked.client.anticheat.replay.file;

import com.mcsrranked.client.info.match.MatchType;
import com.mcsrranked.client.info.match.server.MatchResult;
import com.mcsrranked.client.info.player.BasePlayer;
import java.util.Date;
import java.util.List;

public class ReplayMeta {
   private final int version;
   private final Integer matchId;
   private final long date;
   private final int matchType;
   private final String overworldSeed;
   private final String netherSeed;
   private final String theEndSeed;
   private final String symmetricKey;
   private final MatchResult result;
   private final List<BasePlayer> players;

   public ReplayMeta(int version, Integer matchId, int date, int matchType, String overworldSeed, String netherSeed, String theEndSeed, String symmetricKey, MatchResult result, List<BasePlayer> players) {
      this.version = version;
      this.matchId = matchId;
      this.date = (long)date;
      this.matchType = matchType;
      this.overworldSeed = overworldSeed;
      this.netherSeed = netherSeed;
      this.theEndSeed = theEndSeed;
      this.symmetricKey = symmetricKey;
      this.result = result;
      this.players = players;
   }

   public int getVersion() {
      return this.version;
   }

   public Integer getMatchID() {
      return this.matchId;
   }

   public Date getDate() {
      return new Date(this.date);
   }

   public MatchResult getResult() {
      return this.result;
   }

   public List<BasePlayer> getPlayers() {
      return this.players;
   }

   public MatchType getMatchType() {
      return MatchType.byID(this.matchType);
   }

   public String getOverworldSeed() {
      return this.overworldSeed;
   }

   public String getNetherSeed() {
      return this.netherSeed;
   }

   public String getTheEndSeed() {
      return this.theEndSeed == null ? this.getOverworldSeed() : this.theEndSeed;
   }

   public String getSymmetricKey() {
      return this.symmetricKey;
   }
}
