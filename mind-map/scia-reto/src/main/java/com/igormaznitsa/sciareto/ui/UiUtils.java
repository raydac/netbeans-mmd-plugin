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
package com.igormaznitsa.sciareto.ui;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import com.igormaznitsa.meta.common.utils.IOUtils;

public final class UiUtils {

  public static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  
  private UiUtils() {
  }

  @Nullable
  public static Image loadImage(@Nonnull final String name) {
    final InputStream inStream = UiUtils.class.getClassLoader().getResourceAsStream("icons/" + name);
    Image result = null;
    if (inStream != null) {
      try {
        result = ImageIO.read(inStream);
      } catch (IOException ex) {
        result = null;
      } finally {
        IOUtils.closeQuetly(inStream);
      }
    }
    return result;
  }
  
  @Nullable
  public static String editText(@Nonnull final String title, @Nonnull final String text) {
    final NoteEditor textEditor = new NoteEditor(text);
    try {
      if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(title, textEditor)) {
        return textEditor.getText();
      } else {
        return null;
      }
    } finally {
      textEditor.dispose();
    }
  }

}
