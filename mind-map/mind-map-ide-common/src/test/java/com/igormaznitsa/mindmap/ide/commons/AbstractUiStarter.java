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

package com.igormaznitsa.mindmap.ide.commons;

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.SwingMessageDialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.lang.invoke.MethodHandles;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class AbstractUiStarter {

  private static AbstractUiStarter makeInstance(final String className) {
    try {
      final Class<?> klazz = Class.forName(className);
      return (AbstractUiStarter) klazz.getConstructor().newInstance();
    } catch (Exception ex) {
      throw new Error(ex);
    }
  }

  public static void main(final String... args) {
    SwingUtilities.invokeLater(() -> {
      final JFrame frame = new JFrame("Test frame");
      frame.setLayout(new BorderLayout());

      AbstractUiStarter starter = makeInstance(args[0]);

      frame.add(starter.makePanel(UIComponentFactoryProvider.findInstance(),
          new SwingMessageDialogProvider()), BorderLayout.CENTER);

      final JButton buttonClose = UIComponentFactoryProvider.findInstance().makeButton();
      buttonClose.setText("Close");
      buttonClose.addActionListener(a -> frame.dispose());

      final JPanel buttons = UIComponentFactoryProvider.findInstance().makePanel();
      buttons.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
      buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
      buttons.add(buttonClose);

      frame.add(buttons, BorderLayout.SOUTH);

      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
    });
  }

  public abstract JPanel makePanel(final UIComponentFactory componentFactory,
                                   final DialogProvider dialogProvider);

}
