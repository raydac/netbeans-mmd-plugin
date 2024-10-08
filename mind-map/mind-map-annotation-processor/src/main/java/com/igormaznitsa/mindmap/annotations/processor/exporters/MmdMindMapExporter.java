/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.annotations.processor.exporters;

import com.igormaznitsa.mindmap.model.MindMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class MmdMindMapExporter implements MindMapBinExporter {

  @Override
  public byte[] export(Path rootFolder, Path targetFile, MindMap map) throws IOException {
    return map.asString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String getFileExtension() {
    return "mmd";
  }
}
