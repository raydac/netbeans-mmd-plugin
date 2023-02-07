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

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.SwingMessageDialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class AbstractPreferencesPanelStarter extends AbstractPreferencesPanel {

  public AbstractPreferencesPanelStarter(UIComponentFactory uiComponentFactory,
                                         DialogProvider dialogProvider) {
    super(uiComponentFactory, dialogProvider);
  }

  @Override
  protected JButton processColorButton(final JButton button) {
    button.setHorizontalAlignment(JButton.CENTER);
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
  public List<ButtonInfo> findButtonInfo(UIComponentFactory componentFactory, DialogProvider dialogProvider) {
    final List<ButtonInfo> buttonInfos = new ArrayList<>();

    buttonInfos.add(ButtonInfo.from(null, "Donate", a -> {
    }));
    buttonInfos.add(ButtonInfo.from(null, "About", a -> {
    }));
    buttonInfos.add(ButtonInfo.splitter());
    buttonInfos.add(ButtonInfo.from(null, "Export to file", a -> {
      this.exportAsFileDialog(this::getPanel);
    }));
    buttonInfos.add(ButtonInfo.from(null, "Import from file", a -> {
      this.importFromFileDialog(this::getPanel);
    }));
    buttonInfos.add(ButtonInfo.from(null, "System file extensions", a -> {
    }));
    buttonInfos.add(ButtonInfo.splitter());
    buttonInfos.add(ButtonInfo.from(null, "Reset to default", a -> {
    }));

    return buttonInfos;
  }

  public static void main(String... args) {
    SwingUtilities.invokeLater(() -> {
      final JFrame frame = new JFrame("Test panel");

      final AbstractPreferencesPanel bp =
          new AbstractPreferencesPanelStarter(UIComponentFactoryProvider.findInstance(),
              new SwingMessageDialogProvider());

      bp.load(new MindMapPanelConfig());

      frame.setContentPane(bp.getPanel());
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
    });
  }


}