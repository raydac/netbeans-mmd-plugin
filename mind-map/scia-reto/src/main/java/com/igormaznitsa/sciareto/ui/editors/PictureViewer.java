/*
 * Copyright 2016 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.igormaznitsa.sciareto.ui.editors;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.tabs.TabProvider;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;

public class PictureViewer extends JScrollPane implements TabProvider {

  private static final long serialVersionUID = 4262835444678960206L;

  private final TabTitle title;
  
  public PictureViewer(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super();
    this.title = new TabTitle(context, this, file);
    final Image image = file == null ? null : ImageIO.read(file);
    if (image != null){
      this.setViewportView(new JLabel(new ImageIcon(image)));
    }
  }

  @Override
  public boolean saveDocument() {
    return true;
  }
  
  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this;
  }
  
}
