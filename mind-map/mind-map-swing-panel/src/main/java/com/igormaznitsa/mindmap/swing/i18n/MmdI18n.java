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

package com.igormaznitsa.mindmap.swing.i18n;

import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provider way to get all string constants for mind map panel project.
 * @since 1.6.0
 */
public final class MmdI18n {
  private static final String RESOURCE_PATH = "com/igormaznitsa/mindmap/swing/panel/Bundle";
  private static final MmdI18n INSTANCE = new MmdI18n();

  private MmdI18n() {

  }

  public static MmdI18n getInstance() {
    return INSTANCE;
  }

  public ResourceBundle findBundle() {
    return this.findBundle(IDEBridgeFactory.findInstance().getIDELocale());
  }

  public ResourceBundle findBundle(final Locale locale) {
    return ResourceBundle.getBundle(RESOURCE_PATH, locale);
  }
}
