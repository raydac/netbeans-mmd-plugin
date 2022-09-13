package com.igormaznitsa.mindmap.plugins.importers;

import static java.util.Objects.requireNonNull;

import java.io.File;

public class AbstractImporterTest {

  protected static File findFile(final String resource) throws Exception {
    return new File(
        requireNonNull(AbstractImporterTest.class.getResource(resource)).toURI());
  }



}
