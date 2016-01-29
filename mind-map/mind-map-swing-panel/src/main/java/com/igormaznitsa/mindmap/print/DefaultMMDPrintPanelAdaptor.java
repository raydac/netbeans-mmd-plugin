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
package com.igormaznitsa.mindmap.print;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.Icons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Dimension;
import javax.swing.Icon;

public class DefaultMMDPrintPanelAdaptor implements MMDPrintPanel.Adaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMMDPrintPanelAdaptor.class);
  
  @Override
  public void startBackgroundTask (final MMDPrintPanel source, final String name, final Runnable task) {
    final Thread thread = new Thread(task,name);
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public boolean isDarkTheme (final MMDPrintPanel source) {
    return Utils.isDarkTheme();
  }

  @Override
  public Icon getIcon (final MMDPrintPanel source, final MMDPrintPanel.IconId iconId) {
     switch(iconId){
       case PAGE : return Icons.ICO_PAGE.getIcon();
       case PRINTER : return Icons.ICO_PRINTER.getIcon();
       default: LOGGER.error("Request for unsupportred icon ["+iconId.name()+']'); return null;
     }
  }

  @Override
  public void onPrintTaskStarted (final MMDPrintPanel source) {
  }

  @Override
  public Dimension getPreferredSizeOfPanel (final MMDPrintPanel source) {
    return new Dimension(600, 450);
  }
}
