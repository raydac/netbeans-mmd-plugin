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

import java.util.Arrays;
import java.util.List;

/**
 * Set of registered binary exporters to make a binary file from formed mind map.
 *
 * @since 1.6.8
 */
public enum MmdExporter {
  MMD(new MmdMindMapExporter());

  public static final List<MmdExporter> VALUES = Arrays.asList(MmdExporter.values());
  private final MindMapBinExporter binExporter;

  MmdExporter(final MindMapBinExporter binExporter) {
    this.binExporter = binExporter;
  }

  public static MmdExporter find(final String name) {
    final String trimmed = name.trim();
    return VALUES.stream()
        .filter(x -> x.name().equalsIgnoreCase(trimmed))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(name));
  }

  public MindMapBinExporter getBinExporter() {
    return this.binExporter;
  }
}
