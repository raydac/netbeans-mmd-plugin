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

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.exporters.MDExporter;
import java.io.File;
import java.nio.file.Path;

public class MDMindMapExporter extends AbstractMmdPanelBasedExporter {

  private final AbstractExporter.ExtrasToStringConverter baseFolderAwareConverter =
      new AbstractExporter.ExtrasToStringConverter() {
        @Override
        public String apply(final PluginContext pluginContext, final Extra<?> extra) {
          if (extra.getType() == Extra.ExtraType.FILE) {
            final Path thisFile = pluginContext.getMindMapFile().toPath();
            final Path linkedFile =
                ((ExtraFile) extra).getValue().asFile(pluginContext.getProjectFolder()).toPath();

            try {
              String normalized = thisFile.relativize(linkedFile).normalize().toString();
              while (normalized.startsWith(".." + File.separatorChar + ".." + File.separatorChar)) {
                normalized = normalized.substring(3);
              }
              if (normalized.startsWith(".." + File.separatorChar) &&
                  normalized.lastIndexOf(File.separatorChar) == 2) {
                normalized = normalized.substring(3);
              }
              return normalized;
            } catch (IllegalArgumentException ex) {
              // can't relativize
              return linkedFile.toAbsolutePath().toString();
            }

          } else {
            return MDMindMapExporter.this.getDelegate().getDefaultExtrasStringConverter()
                .apply(pluginContext, extra);
          }
        }
      };

  @Override
  protected AbstractExporter.ExtrasToStringConverter getExtrasStringConverter() {
    return this.baseFolderAwareConverter;
  }

  public MDMindMapExporter() {
    super(new MDExporter());
  }

  @Override
  public String getFileExtension() {
    return "MD";
  }
}
