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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

public class ProjectCellRenderer extends JLabel implements ListCellRenderer, UIResource{

  private static final long serialVersionUID = -9028250303574049796L;

  public ProjectCellRenderer(){
    super();
    setOpaque(true);
  }
  
  @Override
  public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
    if (!(value instanceof Project)){
      return this;
    }
    
    setName("ComboBox.listRenderer");
    
    final ProjectInformation info = ProjectUtils.getInformation((Project)value);
    setText(info.getDisplayName());
    setIcon(info.getIcon());
    
    if (isSelected){
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    }else{
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    
    return this;
  }
  
}
