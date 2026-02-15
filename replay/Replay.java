package com.mcsrranked.client.anticheat.replay;

import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.replay.file.ReplayManager;
import com.mcsrranked.client.anticheat.replay.render.ReplayBoatEntity;
import com.mcsrranked.client.anticheat.replay.render.ReplayPlayerEntity;
import com.mcsrranked.client.anticheat.replay.tracking.OpponentPlayerTracker;
import com.mcsrranked.client.anticheat.replay.tracking.PersonalPlayerTracker;
import com.mcsrranked.client.info.player.BasePlayer;
import com.mcsrranked.client.socket.SocketInstance;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.SecretKey;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.attribute.EntityAttributes;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import net.minecraft.util.registry.Registry;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityDimensions;

public class Replay {
   public static final EntityType<ReplayPlayerEntity> REPLAY_PLAYER_ENTITY_TYPE;
   public static final EntityType<ReplayBoatEntity> REPLAY_BOAT_ENTITY_TYPE;
   private final PersonalPlayerTracker personalPlayerTracker;
   private final ReplayProcessor replayProcessor;
   private final SecretKey symmetricKey;
   private final byte[] signedSymmetricKeyArray;

   public static void onInitializeClient() {
      EntityRendererRegistry.INSTANCE.register(REPLAY_PLAYER_ENTITY_TYPE, (manager, context) -> {
         return new ReplayPlayerEntity.Renderer(manager);
      });
      FabricDefaultAttributeRegistry.register(REPLAY_PLAYER_ENTITY_TYPE, ReplayPlayerEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE).add(EntityAttributes.GENERIC_MAX_HEALTH, 65535.0D));
      EntityRendererRegistry.INSTANCE.register(REPLAY_BOAT_ENTITY_TYPE, (manager, context) -> {
         return new ReplayBoatEntity.Renderer(manager);
      });
      FabricDefaultAttributeRegistry.register(REPLAY_BOAT_ENTITY_TYPE, ReplayBoatEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE).add(EntityAttributes.GENERIC_MAX_HEALTH, 65535.0D));
   }

   public String getSymmetricKey() {
      return Base64.getEncoder().encodeToString(this.signedSymmetricKeyArray);
   }

   public Replay(String signedSymmetricKey) {
      this.replayProcessor = new ReplayProcessor();
      this.personalPlayerTracker = new PersonalPlayerTracker();

      byte[] signedSymmetricKeyArray1;
      try {
         if (signedSymmetricKey.isEmpty()) {
            throw new IllegalArgumentException();
         }

         signedSymmetricKeyArray1 = Base64.getDecoder().decode(signedSymmetricKey);
      } catch (IllegalArgumentException var5) {
         this.signedSymmetricKeyArray = null;
         this.symmetricKey = null;
         return;
      }

      this.signedSymmetricKeyArray = signedSymmetricKeyArray1;

      try {
         this.symmetricKey = ReplayManager.generateSecretKey(this.signedSymmetricKeyArray);
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public Replay(Replay replay) {
      this(replay.getSymmetricKey());
      this.getPersonalPlayerTracker().copySettings(replay.getPersonalPlayerTracker());
      Iterator var2 = replay.getReplayProcessor().getPlayers().iterator();

      while(var2.hasNext()) {
         BasePlayer player = (BasePlayer)var2.next();
         this.addOpponentPlayer(player);
      }

   }

   public static void initListener() {
      SocketInstance.on("m$replay_tracking", (payload) -> {
         MCSRRankedClient.getOnlineMatch().ifPresent((onlineMatch) -> {
            try {
               onlineMatch.updateReplay(payload.getNextBytes());
            } catch (Exception var3) {
               var3.printStackTrace();
            }

         });
      });
      SocketInstance.on("m$replay_meta", (data) -> {
         MCSRRankedClient.getOnlineMatch().ifPresent((onlineMatch) -> {
            onlineMatch.closeReplayStream().thenRun(() -> {
               try {
                  int index = 0;
                  Path path = ReplayManager.REPLAY_FILE_PATH.resolve((new SimpleDateFormat("yyMMdd-HHmmss")).format(new Date()) + ".rrf");
                  FileUtils.write(path.toFile(), "", StandardCharsets.UTF_8);
                  ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(path, StandardOpenOption.APPEND));

                  while(data.hasNext()) {
                     String property = data.getNextString();
                     if (index++ % 2 == 0) {
                        zipOutputStream.putNextEntry(new ZipEntry(property));
                     } else {
                        zipOutputStream.write(property.getBytes(StandardCharsets.UTF_8));
                        zipOutputStream.closeEntry();
                     }
                  }

                  zipOutputStream.putNextEntry(new ZipEntry("replay.rpd"));
                  InputStream inputStream = Files.newInputStream(MCSRRankedClient.TEMP_REPLAY_FILE.toPath());
                  byte[] buffer = new byte[1024];

                  int bytesRead;
                  while((bytesRead = inputStream.read(buffer)) != -1) {
                     zipOutputStream.write(buffer, 0, bytesRead);
                  }

                  inputStream.close();
                  zipOutputStream.closeEntry();
                  zipOutputStream.close();
               } catch (Exception var7) {
                  var7.printStackTrace();
               }

            });
         });
      });
   }

   public Optional<OpponentPlayerTracker> getOpponentPlayerTracker(UUID uuid) {
      return this.getReplayProcessor().getTracker(uuid);
   }

   public void addOpponentPlayer(BasePlayer player) {
      this.getReplayProcessor().addNewTracker(player);
   }

   public void removeOpponentPlayer(BasePlayer player) {
      this.getReplayProcessor().removeTracker(player);
   }

   public PersonalPlayerTracker getPersonalPlayerTracker() {
      return this.personalPlayerTracker;
   }

   public ReplayProcessor getReplayProcessor() {
      return this.replayProcessor;
   }

   public SecretKey getSecretKey() {
      return this.symmetricKey;
   }

   public void reset() {
      this.getPersonalPlayerTracker().reset();
      this.getReplayProcessor().reset();
   }

   static {
      REPLAY_PLAYER_ENTITY_TYPE = (EntityType)Registry.register(Registry.ENTITY_TYPE, new Identifier("mcsrranked", "replay_player"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, ReplayPlayerEntity::new).disableSaving().fireImmune().disableSummon().spawnableFarFromPlayer().dimensions(EntityDimensions.changing(0.6F, 1.8F)).build());
      REPLAY_BOAT_ENTITY_TYPE = (EntityType)Registry.register(Registry.ENTITY_TYPE, new Identifier("mcsrranked", "replay_boat"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, ReplayBoatEntity::new).disableSaving().fireImmune().disableSummon().spawnableFarFromPlayer().dimensions(EntityDimensions.changing(1.375F, 0.5625F)).build());
   }
}
