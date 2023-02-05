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

package com.igormaznitsa.nbmindmap.nb.options;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.nbmindmap.nb.editor.MMDGraphEditor;
import com.igormaznitsa.nbmindmap.utils.DialogProviderManager;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
    categoryName = "Mind Map",
    iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo32.png",
    keywords = "mindmap",
    keywordsCategory = "mindmap",
    id = "nb-mmd-config-main",
    position = 10000
)
public final class MMDCfgOptionsPanelController extends OptionsPanelController {

  private PreferencesPanel panel;
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private boolean changed;

  @Override
  public void update() {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(NbUtils.getPreferences());
    this.getPanel().load(config);
    this.changed = false;
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(() -> {
      try {
        final MindMapPanelConfig config = this.panel.save();
        config.saveTo(NbUtils.getPreferences());
        changed = false;
      } finally {
        MMDGraphEditor.notifyReloadConfig();
      }
    });
  }

  @Override
  public void cancel() {
    // need not do anything special, if no changes have been persisted yet
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean isChanged() {
    return this.changed;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return null; // new HelpCtx("...ID") if you have a help set
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    return this.getPanel().getPanel();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    this.propertyChangeSupport.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    this.propertyChangeSupport.removePropertyChangeListener(l);
  }

  private PreferencesPanel getPanel() {
    if (this.panel == null) {
      this.panel = new PreferencesPanel(UIComponentFactoryProvider.findInstance(),
          DialogProviderManager.getInstance().getDialogProvider(), this::onPossibleChange);
    }
    return this.panel;
  }

  void onPossibleChange(final PreferencesPanel panel) {
    this.changed = panel.checkChanges();
    if (this.changed) {
      this.propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false,
          true);
    }
    this.propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
  }

  void changed() {
    if (!this.changed) {
      this.changed = true;
      this.propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false,
          true);
    }
    this.propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
  }

}
