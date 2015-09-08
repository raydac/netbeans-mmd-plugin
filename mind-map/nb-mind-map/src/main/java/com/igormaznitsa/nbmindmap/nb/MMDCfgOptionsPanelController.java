/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb;

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

  private MMDCfgPanel panel;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private boolean changed;

  @Override
  public void update() {
    getPanel().load();
    changed = false;
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        getPanel().store();
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
    return changed;
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
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  private MMDCfgPanel getPanel() {
    if (panel == null) {
      panel = new MMDCfgPanel(this);
    }
    return panel;
  }

  void changed() {
    if (!changed) {
      changed = true;
      pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
    }
    pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
  }

}
