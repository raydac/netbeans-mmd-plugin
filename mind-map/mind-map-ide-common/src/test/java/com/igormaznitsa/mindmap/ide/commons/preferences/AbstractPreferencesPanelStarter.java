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

package com.igormaznitsa.mindmap.ide.commons.preferences;

import com.igormaznitsa.mindmap.ide.commons.AbstractUiStarter;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.SwingMessageDialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class AbstractPreferencesPanelStarter extends AbstractUiStarter {

  public static void main(final String ... args){
    AbstractUiStarter.main(AbstractPreferencesPanelStarter.class.getName());
  }

  @Override
  public JPanel makePanel(UIComponentFactory componentFactory,
                          DialogProvider dialogProvider) {
    final AbstractPreferencesPanel bp =
        new AbstractPreferencesPanel(UIComponentFactoryProvider.findInstance(),
            new SwingMessageDialogProvider()) {
          @Override
          protected JButton processColorButton(final JButton button) {
            button.setMargin(new Insets(3, 8, 3, 0));
            return button;
          }

          @Override
          public void onSave(MindMapPanelConfig config) {

          }

          @Override
          public List<JComponent> findMiscComponents(UIComponentFactory componentFactory) {
            return Collections.emptyList();
          }

          @Override
          public List<JComponent> findFeaturesComponents(UIComponentFactory componentFactory) {
            return Collections.emptyList();
          }

          @Override
          public void onLoad(MindMapPanelConfig config) {

          }

          @Override
          public List<AbstractPreferencesPanel.ButtonInfo> findButtonInfo(
              UIComponentFactory componentFactory, DialogProvider dialogProvider) {
            final List<AbstractPreferencesPanel.ButtonInfo> buttonInfos = new ArrayList<>();

            buttonInfos.add(AbstractPreferencesPanel.ButtonInfo.from(null, "Donate", a -> {
            }));
            buttonInfos.add(AbstractPreferencesPanel.ButtonInfo.from(null, "About", a -> {
            }));
            buttonInfos.add(AbstractPreferencesPanel.ButtonInfo.splitter());
            buttonInfos.add(AbstractPreferencesPanel.ButtonInfo.from(null, "Export to file", a -> {
              this.exportAsFileDialog(this::getPanel);
            }));
            buttonInfos.add(
                AbstractPreferencesPanel.ButtonInfo.from(null, "Import from file", a -> {
                  this.importFromFileDialog(this::getPanel);
                }));
            buttonInfos.add(
                AbstractPreferencesPanel.ButtonInfo.from(null, "System file extensions", a -> {
                }));
            buttonInfos.add(AbstractPreferencesPanel.ButtonInfo.splitter());
            buttonInfos.add(
                AbstractPreferencesPanel.ButtonInfo.from(null, "Reset to default", a -> {
                }));

            return buttonInfos;
          }

        };

    bp.load(new MindMapPanelConfig());

    return bp.getPanel();
  }

}