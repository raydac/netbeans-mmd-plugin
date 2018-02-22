/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import com.igormaznitsa.nbmindmap.utils.NbUtils;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "Mind Map",
        iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo32.png",
        keywords = "mindmap",
        keywordsCategory = "mindmap",
        id = "nb-mmd-config-main",
        position = 10000
)
public final class MMDCfgOptionsPanelController extends OptionsPanelController {

  private MMDCfgPanel panel;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private boolean changed;

  @Override
  public void update() {
    getPanel().load(NbUtils.getPreferences());
    this.changed = false;
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final MMDCfgPanel panel = getPanel();
        panel.store(NbUtils.getPreferences(),panel.getConfig(),true);
        changed = false;
      }
    });
  }

  @Override
  public void cancel() {
    // need not do anything special, if no changes have been persisted yet
  }

  @Override
  public boolean isValid() {
    return getPanel().valid();
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
    return getPanel();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    this.pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    this.pcs.removePropertyChangeListener(l);
  }

  private MMDCfgPanel getPanel() {
    if (this.panel == null) {
      this.panel = new MMDCfgPanel(this);
    }
    return this.panel;
  }

  void changed() {
    if (!this.changed) {
      this.changed = true;
      this.pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
    }
    this.pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
  }

}
