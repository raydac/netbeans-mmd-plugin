/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not usne this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.nbmindmap.exporters;

import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.utils.Icons;
import java.io.IOException;
import javax.swing.ImageIcon;

public final class PNGImageExporter extends MindMapExporter {

  public PNGImageExporter() {
    super();
  }

  @Override
  public void doExport(final MindMapPanel viewPanel) throws IOException {
  }

  @Override
  public String getName() {
    return "PNG image";
  }

  @Override
  public String getReference() {
    return "Export of mind map as a PNG image file";
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.IMAGE.getIcon();
  }
}
