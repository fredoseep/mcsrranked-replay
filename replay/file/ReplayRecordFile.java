package com.mcsrranked.client.anticheat.replay.file;

import com.mcsrranked.client.anticheat.replay.ReplayProcessor;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class ReplayRecordFile {
   private File file;
   private final ReplayMeta meta;

   public ReplayRecordFile(File file, ReplayMeta meta) {
      this.file = file;
      this.meta = meta;
   }

   public String getName() {
      return this.getFile().getName().substring(0, this.getFile().getName().length() - 4);
   }

   public void setName(String text) throws IOException {
      File newFile = this.getFile().getParentFile().toPath().resolve(text + ".rrf").toFile();
      FileUtils.moveFile(this.getFile(), newFile);
      this.file = newFile;
   }

   public File getFile() {
      return this.file;
   }

   public ReplayMeta getMeta() {
      return this.meta;
   }

   public ReplayProcessor getProcessor() throws Exception {
      return new ReplayProcessor(this);
   }
}
