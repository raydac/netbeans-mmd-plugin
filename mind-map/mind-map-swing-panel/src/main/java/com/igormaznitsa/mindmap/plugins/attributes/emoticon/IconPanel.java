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
package com.igormaznitsa.mindmap.plugins.attributes.emoticon;

import java.awt.GridLayout;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public final class IconPanel extends JPanel {

  private static final long serialVersionUID = 4823626757838675154L;

  private final ButtonGroup group = new ButtonGroup();

  public IconPanel() {
    super(new GridLayout(0, 6));
    add(makeIconButton(group, "empty"));
    for (final String s : MiscIcons.getNames()) {
      add(makeIconButton(group, s));
    }
  }

  @Nullable
  public String getSelectedName() {
    final Enumeration<AbstractButton> iterator = this.group.getElements();
    while (iterator.hasMoreElements()) {
      final JToggleButton button = (JToggleButton) iterator.nextElement();
      if (button.isSelected()) {
        return button.getName();
      }
    }
    return null;
  }

  @Nonnull
  private JToggleButton makeIconButton(@Nonnull final ButtonGroup group, @Nonnull final String name) {
    final JToggleButton result = Utils.UI_COMPO_FACTORY.makeToggleButton();
    result.setIcon(new ImageIcon(MiscIcons.findForName(name)));
    result.setName(name);
    result.setToolTipText(name);
    group.add(result);
    return result;
  }
}
