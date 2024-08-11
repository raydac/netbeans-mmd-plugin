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
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.exporters.ORGMODEExporter;
import java.net.URISyntaxException;

public class OrgModeMindMapExporter extends AbstractMmdPanelBasedExporter {

  private final AbstractExporter.ExtrasToStringConverter extrasToStringConverter =
      new AbstractExporter.ExtrasToStringConverter() {
        @Override
        public String apply(PluginContext pluginContext, Extra<?> extra) {
          if (extra.getType() == Extra.ExtraType.FILE) {
            try {
              final MMapURI thisFile = MMapURI.makeFromFilePath(
                  pluginContext.getProjectFolder().getParentFile(),
                  OrgModeMindMapExporter.super.getExtrasStringConverter()
                      .apply(pluginContext, extra),
                  null);

              if (thisFile.isAbsolute()) {
                return thisFile.asURI().toASCIIString();
              } else {
                return "file://./" + thisFile.asURI().toASCIIString();
              }
            } catch (URISyntaxException ex) {
              return "file://" + OrgModeMindMapExporter.super.getExtrasStringConverter()
                  .apply(pluginContext, extra);
            }
          } else {
            return OrgModeMindMapExporter.this.getDelegate().getDefaultExtrasStringConverter()
                .apply(pluginContext, extra);
          }
        }
      };

  public OrgModeMindMapExporter() {
    super(new ORGMODEExporter());
  }

  @Override
  protected AbstractExporter.ExtrasToStringConverter getExtrasStringConverter() {
    return this.extrasToStringConverter;
  }

  @Override
  public String getFileExtension() {
    return "org";
  }
}
