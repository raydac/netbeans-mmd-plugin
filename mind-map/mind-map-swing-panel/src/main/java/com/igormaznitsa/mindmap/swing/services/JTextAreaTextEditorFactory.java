/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.swing.panel.utils.JTextAreaCustomTextEditor;

public final class JTextAreaTextEditorFactory implements CustomTextEditorFactory {

  public static final JTextAreaTextEditorFactory INSTANCE = new JTextAreaTextEditorFactory();
  private final UIComponentFactory componentFactory = UIComponentFactoryProvider.findInstance();

  private JTextAreaTextEditorFactory() {

  }

  @Override
  public CustomTextEditor makeCustomTextEditor() {
    return new JTextAreaCustomTextEditor(INSTANCE.componentFactory.makeTextArea());
  }

}
