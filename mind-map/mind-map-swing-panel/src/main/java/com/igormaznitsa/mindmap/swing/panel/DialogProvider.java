/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import java.awt.Component;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

public interface DialogProvider {

  void msgError(Component parentComponent, String text);

  void msgInfo(Component parentComponent, String text);

  void msgWarn(Component parentComponent, String text);

  boolean msgConfirmOkCancel(Component parentComponent, String title, String question);

  boolean msgOkCancel(Component parentComponent, String title, JComponent component);

  boolean msgConfirmYesNo(Component parentComponent, String title, String question);

  Boolean msgConfirmYesNoCancel(Component parentComponent, String title, String question);

  File msgSaveFileDialog(Component parentComponent, PluginContext pluginContext, String id,
                         String title, File defaultFolder, boolean filesOnly,
                         FileFilter[] fileFilter, String approveButtonText);

  File msgOpenFileDialog(Component parentComponent, PluginContext pluginContext, String id,
                         String title, File defaultFolder, boolean filesOnly,
                         FileFilter[] fileFilter, String approveButtonText);
}
