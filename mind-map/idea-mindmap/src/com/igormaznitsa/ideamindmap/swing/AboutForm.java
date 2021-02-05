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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class AboutForm {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");
  private JPanel mainPanel;
  private JHtmlLabel htmlLabelText;

  public AboutForm() {
    final IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("nb-mind-map-idea"));
    this.htmlLabelText.setText(BUNDLE.getString("AboutText").replace("${version}", descriptor == null ? "<unknown>" : descriptor.getVersion()));
    this.htmlLabelText.addLinkListener(new JHtmlLabel.LinkListener() {
      @Override
      public void onLinkActivated(final JHtmlLabel source, final String link) {
        try {
          IdeaUtils.browseURI(URI.create(link), false);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    this.mainPanel.setPreferredSize(new Dimension(600, 400));
  }

  public static void show(final Component parent) {
    new DialogComponent(parent, "About", new AboutForm().mainPanel).show();
  }

  public static void show(final Project project) {
    new DialogComponent(project, "About", new AboutForm().mainPanel).show();
  }

  private static class DialogComponent extends DialogWrapper {
    private final JComponent component;

    public DialogComponent(final Component parent, final String title, final JComponent component) {
      super(parent, true);
      this.component = component;
      init();
      setTitle(title);
    }

    public DialogComponent(final Project project, final String title, final JComponent component) {
      super(project, true);
      this.component = component;
      init();
      setTitle(title);
    }

    @Nonnull
    protected Action[] createActions() {
      return new Action[] {getOKAction()};
    }

    protected void init() {
      setResizable(false);
      setModal(true);
      super.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return this.component;
    }
  }
}
