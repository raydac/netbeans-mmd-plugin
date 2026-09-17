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

package com.igormaznitsa.ideamindmap.swing;

import static com.igormaznitsa.mindmap.model.logger.LoggerFactory.getLogger;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.ResourceBundle.getBundle;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class AboutForm {
  private static final Logger LOGGER = getLogger(AboutForm.class);
  private static final ResourceBundle BUNDLE = getBundle("i18n/Bundle");
  private static final Pattern PLUGIN_VERSION = Pattern.compile("<version>([^<]+)</version>");
  private JPanel mainPanel;
  private JHtmlLabel htmlLabelText;

  public AboutForm() {
    this.htmlLabelText.setText(BUNDLE.getString("AboutText").replace("${version}", readPluginVersion()));
    this.htmlLabelText.addLinkListener((JHtmlLabel.LinkListener) (source, link) -> {
      try {
        IdeaUtils.browseURI(URI.create(link), false);
      } catch (Exception ex) {
        LOGGER.error("Can't open about-dialog link: " + link, ex);
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

  private static String readPluginVersion() {
    try (final InputStream stream = AboutForm.class.getResourceAsStream("/META-INF/plugin.xml")) {
      if (stream == null) {
        return "<unknown>";
      }
      final Matcher matcher = PLUGIN_VERSION.matcher(new String(stream.readAllBytes(), UTF_8));
      return matcher.find() ? matcher.group(1) : "<unknown>";
    } catch (final IOException ex) {
      LOGGER.error("Can't read plugin version from plugin.xml", ex);
      return "<unknown>";
    }
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
