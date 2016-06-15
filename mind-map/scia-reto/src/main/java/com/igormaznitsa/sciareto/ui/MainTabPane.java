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

import java.io.File;
import javax.annotation.Nonnull;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class MainTabPane extends JTabbedPane {
  
  private static final long serialVersionUID = -8971773653667281550L;
  
  public MainTabPane(){
    super(JTabbedPane.TOP);
  }

  public void createTab(@Nonnull final TabProvider panel){
    super.addTab("...", panel.getMainComponent());
    final int count = this.getTabCount() - 1;
    this.setTabComponentAt(count, panel.getTabTitle());
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        panel.getMainComponent().requestFocus();
      }
    });
    this.setSelectedIndex(count);
  }
  
  public boolean focusToFile(final File file) {
    for (int i=0;i<this.getTabCount();i++){
      final TabTitle title = (TabTitle)this.getTabComponentAt(i);
      if (file.equals(title.getAssociatedFile())){
        this.setSelectedIndex(i);
        return true;
      }
    }
    return false;
  }
  
  private void clickToClose(@Nonnull final TabProvider provider){
    int index = -1;
    for(int i=0;i<this.getTabCount();i++){
      if (this.getTabComponentAt(i) == provider.getMainComponent()) {
        index = i;
        break;
      }
    }
    
    if (index >= 0){
      this.removeTabAt(index);
    }
  }
  
}
