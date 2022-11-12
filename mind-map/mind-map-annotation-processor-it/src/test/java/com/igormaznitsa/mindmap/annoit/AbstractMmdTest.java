package com.igormaznitsa.mindmap.annoit;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.mindmap.model.MindMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;

public abstract class AbstractMmdTest {
  protected Path getBaseDir() {
    final String baseDir = System.getProperty("mmd.basedir", null);
    assertNotNull("Can't find MMD base folder", baseDir);
    return new File(baseDir).toPath();
  }

  protected Path getSrcDir() {
    final String srcDir = System.getProperty("mmd.src.dir", null);
    assertNotNull("Can't find MMD source folder", srcDir);
    return new File(srcDir).toPath();
  }

  protected MindMap loadMindMap(final String path) throws IOException {
    final Path asPath = new File(FilenameUtils.normalizeNoEndSeparator(path)).toPath();
    assertFalse("path must be relative one", asPath.isAbsolute());
    final Path mindMapFile = this.getSrcDir().resolve(asPath);
    assertTrue("Can't find mind map file: "+ mindMapFile, Files.isRegularFile(mindMapFile));
    return new MindMap(new StringReader(readFileToString(mindMapFile.toFile(), UTF_8)));
  }
}
