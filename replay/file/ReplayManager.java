package com.mcsrranked.client.anticheat.replay.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mcsrranked.client.MCSRRankedClient;
import com.mcsrranked.client.anticheat.AntiCheatConfig;
import net.minecraft.client.MinecraftClient;

import com.mcsrranked.client.anticheat.replay.ReplayProcessor;
import com.mcsrranked.client.anticheat.replay.file.migrate.ReplayMetaUnder30;
import com.mcsrranked.client.gui.RankedTransitionOverlay;
import com.mcsrranked.client.gui.screen.MessageScreen;
import com.mcsrranked.client.gui.screen.match.replay.ReplayLoadingScreen;
import com.mcsrranked.client.info.race.WeeklyRace;
import com.mcsrranked.client.info.race.WeeklyRaceRecord;
import com.mcsrranked.client.world.WorldCreator;
import com.mcsrranked.client.world.WorldCreatorBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import org.apache.commons.io.FileUtils;
import org.xerial.snappy.Snappy;

public class ReplayManager {
   public static Screen LAST_REPLAY_SCREEN = null;
   public static int CURRENT_LOADING = 0;
   public static Thread REPLAY_LOAD_THREAD = null;
   public static final Path REPLAY_FILE_PATH;
   public static final Path TEMP_REPLAY_FILE_PATH;

   public static SecretKey generateSecretKey(byte[] keyArray) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(2, AntiCheatConfig.REPLAY_PUBLIC_KEY);
      return new SecretKeySpec(cipher.doFinal(keyArray), "AES");
   }

   public static ByteBuffer decryptByteBuffer(SecretKey secretKey, byte[] byteData, boolean decompress) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(2, secretKey);
      byte[] bytes = cipher.doFinal(byteData);
      return ByteBuffer.wrap(decompress ? Snappy.uncompress(bytes) : bytes);
   }

   public static void deleteOldestFile() {
      File[] files = TEMP_REPLAY_FILE_PATH.toFile().listFiles();
      if (files != null) {
         List<File> fileList = (List)Arrays.stream(files).filter((file) -> {
            return file.getName().endsWith(".rrf");
         }).sorted(Comparator.comparingLong(File::lastModified)).collect(Collectors.toList());
         int var2 = fileList.size();

         while(var2-- >= 5) {
            try {
               FileUtils.forceDelete((File)fileList.get(0));
            } catch (IOException var7) {
               throw new RuntimeException(var7);
            } finally {
               fileList.remove(0);
            }
         }

      }
   }

   public static void getAllReplayFiles(Consumer<ReplayRecordFile> provider) {
      List<File> fileList = new ArrayList();
      File[] var2 = (File[])Objects.requireNonNull(REPLAY_FILE_PATH.toFile().listFiles());
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         File file = var2[var4];
         if (file.getName().endsWith(".rrf")) {
            fileList.add(file);
         }
      }

      fileList.sort(Comparator.comparingLong((filex) -> {
         return System.currentTimeMillis() - filex.lastModified();
      }));
      Iterator var7 = fileList.iterator();

      while(var7.hasNext()) {
         File file = (File)var7.next();

         try {
            provider.accept(convertReplayFile(file));
         } catch (Exception var6) {
            var6.printStackTrace();
            MCSRRankedClient.LOGGER.error("Failed to load replay file: \"{}\" - {}", file.getPath(), var6.getMessage());
         }
      }

   }

   public static ReplayRecordFile convertReplayFile(File file) throws Exception {
      ZipFile zipFile = new ZipFile(file);
      InputStream inputStream = zipFile.getInputStream(new ZipEntry("meta.json"));
      JsonElement metaJson = (new JsonParser()).parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      ReplayMeta replayMeta = (ReplayMeta)MCSRRankedClient.GSON.fromJson(metaJson, ReplayMeta.class);
      if (replayMeta.getVersion() < 30) {
         replayMeta = ReplayMetaUnder30.migrate(metaJson.getAsJsonObject());
      }

      inputStream.close();
      zipFile.close();
      return new ReplayRecordFile(file, replayMeta);
   }

   public static void playReplayProcessor(Screen parent, ReplayRecordFile recordFile) {
      LAST_REPLAY_SCREEN = parent;
      CURRENT_LOADING = 0;
      if ((Boolean)MCSRRankedClient.getOnlineMatch().map((match) -> {
         return match.getStatus().isQueued();
      }).orElse(false)) {
         MinecraftClient.getInstance().openScreen(new MessageScreen(parent, new TranslatableText("projectelo.text.replay_load_fail_match")));
      } else {
         ReplayLoadingScreen replayLoadingScreen = new ReplayLoadingScreen(parent);
         MinecraftClient.getInstance().openScreen(replayLoadingScreen);
         REPLAY_LOAD_THREAD = new Thread(() -> {
            ReplayProcessor processor = null;

            try {
               processor = recordFile.getProcessor();
            } catch (Exception var4) {
               var4.printStackTrace();
               replayLoadingScreen.onFail();
            }

            if (processor != null) {
               ReplayProcessor finalProcessor = processor;
               MinecraftClient.getInstance().execute(() -> {
                  if (REPLAY_LOAD_THREAD != null) {
                     MCSRRankedClient.CURRENT_REPLAY = finalProcessor;
                     finalProcessor.setActive(true);
                     WorldCreatorBuilder builder = (new WorldCreatorBuilder(recordFile.getMeta().getOverworldSeed(), recordFile.getMeta().getNetherSeed())).setTheEndSeed(recordFile.getMeta().getTheEndSeed()).spectate();
                     AtomicReference<RankedTransitionOverlay> reference = new AtomicReference();
                     WorldCreator worldCreator = builder.setAfterServerSetup(() -> {
                        ((RankedTransitionOverlay)reference.get()).fadeOut();
                     }).build();
                     RankedTransitionOverlay overlay = new RankedTransitionOverlay((transition) -> {
                     }, false);
                     reference.set(overlay);
                     MinecraftClient.getInstance().setOverlay(overlay);
                     overlay.fadeIn();
                     MCSRRankedClient.THREAD_EXECUTOR.submit(() -> {
                        while(!overlay.isFadeInFinished()) {
                           Thread.yield();
                           LockSupport.parkNanos("waiting for ready to requeue", 100000L);
                        }

                        MinecraftClient var10000 = MinecraftClient.getInstance();
                        Objects.requireNonNull(worldCreator);
                        var10000.execute(worldCreator::start);
                     });
                  }

               });
            }

         });
         REPLAY_LOAD_THREAD.start();
      }
   }

   public static boolean isValidReplayName(String name) {
      if (name.trim().isEmpty()) {
         return false;
      } else {
         name = name.endsWith(".rrf") ? name : name + ".rrf";

         try {
            (new File(name)).getCanonicalPath();
         } catch (IOException var5) {
            return false;
         }

         File[] var1 = (File[])Objects.requireNonNull(REPLAY_FILE_PATH.toFile().listFiles());
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            File file = var1[var3];
            if (file.getName().equals(name + ".rrf") || file.getName().equals(name)) {
               return false;
            }
         }

         return true;
      }
   }

   public static File downloadReplayFile(int matchID, String name, boolean temp) {
      CURRENT_LOADING = 0;
      name = name.endsWith(".rrf") ? name : name + ".rrf";
      File result = (temp ? TEMP_REPLAY_FILE_PATH : REPLAY_FILE_PATH).resolve(name).toFile();
      if (!temp && !isValidReplayName(name)) {
         return null;
      } else {
         File tempFile = TEMP_REPLAY_FILE_PATH.resolve(matchID + ".rrf").toFile();
         if (tempFile.exists()) {
            if (!temp) {
               tempFile.renameTo(result);
            }

            return result;
         } else {
            return downloadReplayFile(result, String.valueOf(matchID));
         }
      }
   }

   public static File downloadReplayFile(WeeklyRace race, WeeklyRaceRecord record) {
      CURRENT_LOADING = 0;
      String name = String.format("%s-%s-%s.rrf", race.getID(), record.getTime(), record.getPlayer().getUUIDString());
      File result = TEMP_REPLAY_FILE_PATH.resolve("race").resolve(name).toFile();
      TEMP_REPLAY_FILE_PATH.resolve("race").toFile().mkdirs();
      result.deleteOnExit();
      return result.exists() ? result : downloadReplayFile(result, String.format("/weekly-race/%s/%s?t=%s", race.getID(), record.getPlayer().getUUIDString(), record.getTime()));
   }

   private static File downloadReplayFile(File file, String target) {
      InputStream is = null;
      FileOutputStream os = null;

      try {
         URL url = new URL(MCSRRankedClient.REPLAY_CDN_URL + target);
         System.out.println("Replay url out:"+url);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestProperty("User-Agent", "MCSR-Ranked/" + MCSRRankedClient.MOD_CONTAINER.getMetadata().getVersion() + " (mcsrranked.com)");
         conn.setConnectTimeout(10000);
         conn.setReadTimeout(60000);
         int responseCode = conn.getResponseCode();
         if (responseCode != 200) {
            MCSRRankedClient.LOGGER.info("No replay to download. Server replied HTTP code: {}", responseCode);
            return null;
         } else {
            is = conn.getInputStream();
            os = new FileOutputStream(file);
            byte[] buffer = new byte[4096];

            int bytesRead;
            while((bytesRead = is.read(buffer)) != -1) {
               os.write(buffer, 0, bytesRead);
            }

            os.close();
            is.close();
            deleteOldestFile();
            MCSRRankedClient.LOGGER.info("Replay downloaded");
            conn.disconnect();
            return file;
         }
      } catch (Exception var11) {
         MCSRRankedClient.LOGGER.error("An error occurred while trying to download a replay.");
         var11.printStackTrace();

         try {
            if (is != null) {
               is.close();
            }

            if (os != null) {
               os.close();
            }
         } catch (IOException var10) {
            var10.printStackTrace();
         }

         return null;
      }
   }

   static {
      REPLAY_FILE_PATH = MCSRRankedClient.GLOBAL_PATH.resolve("replay");
      TEMP_REPLAY_FILE_PATH = MCSRRankedClient.GLOBAL_PATH.resolve("temp-replay");

      try {
         FileUtils.forceMkdir(REPLAY_FILE_PATH.toFile());
         FileUtils.forceMkdir(TEMP_REPLAY_FILE_PATH.toFile());
      } catch (IOException var1) {
         var1.printStackTrace();
      }

   }
}
