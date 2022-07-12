/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.sciareto.ui.tabs;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.Paths;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.EditorContentType;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public final class TabTitle extends JPanel {

  private static final long serialVersionUID = -6534083975320248288L;
  private final JLabel titleLabel;
  private final JButton closeButton;
  private volatile File associatedFile;
  private volatile boolean changed;
  private final Context context;
  private final TabProvider parent;

  private boolean visited;

  private static final Icon CloseTabIconTransparent;
  private static final Icon CloseTabIcon;

  static {
    final Image image = UiUtils.loadIcon("cancel.png"); //NOI18N
    if (image == null) {
      throw new NullPointerException("Can't load image from resource");
    }
    CloseTabIcon = new ImageIcon(image);
    CloseTabIconTransparent = new ImageIcon(UiUtils.makeWithAlpha(image, 0.4f));
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(TabTitle.class);

  public TabTitle(@Nonnull final Context context, @Nonnull final TabProvider parent,
                  @Nullable final File associatedFile) {
    super(new GridBagLayout());
    this.parent = parent;
    this.context = context;
    this.associatedFile = associatedFile;
    this.changed = this.associatedFile == null;
    this.setOpaque(false);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1000.0d;

    final TabTitle theInstance = this;

    this.titleLabel = new JLabel() {
      private static final long serialVersionUID = 8689945842487138781L;

      @Override
      protected void processKeyEvent(@Nonnull final KeyEvent e) {
        theInstance.getParent().dispatchEvent(e);
      }

      @Override
      public String getToolTipText() {
        return theInstance.getToolTipText();
      }

      @Override
      public boolean isFocusable() {
        return false;
      }
    };
    this.add(this.titleLabel, constraints);

    final Icon uiCloseIcon = UIManager.getIcon("InternalFrameTitlePane.closeIcon"); //NOI18N

    this.closeButton = new JButton() {
      @Override
      public String getToolTipText() {
        return theInstance.getToolTipText();
      }

      @Override
      public boolean isFocusable() {
        return false;
      }
    };

    if (uiCloseIcon == null) {
      this.closeButton.setIcon(CloseTabIconTransparent);
      this.closeButton.setRolloverIcon(CloseTabIcon);
      this.closeButton.setPressedIcon(CloseTabIcon);
    } else {
      this.closeButton.setIcon(uiCloseIcon);
    }

    this.closeButton.setToolTipText("Close tab");
    this.closeButton.setBorder(null);
    this.closeButton.setContentAreaFilled(false);
    this.closeButton.setMargin(new Insets(0, 0, 0, 0));
    this.closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.closeButton.setOpaque(false);
    this.closeButton.addActionListener(e -> doSafeClose());
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 0.0d;
    constraints.insets = new Insets(2, 8, 2, 0);

    this.add(this.closeButton, constraints);

    updateView();

    ToolTipManager.sharedInstance().registerComponent(closeButton);
    ToolTipManager.sharedInstance().registerComponent(this.titleLabel);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public void visited() {
    this.visited = true;
  }

  public boolean isVisited() {
    return this.visited;
  }

  @Override
  public boolean contains(int x, int y) {
    return this.closeButton.getBounds().contains(x, y);
  }

  public boolean save() throws IOException {
    boolean result = false;
    if (this.parent.saveDocument()) {
      result = true;
      this.changed = false;
      updateView();
    }
    return result;
  }

  public boolean saveAs() throws IOException {
    boolean result = false;
    if (this.parent.saveDocumentAs()) {
      result = true;
      final NodeProject project = this.context.findProjectForFile(this.associatedFile);
      if (project != null) {
        SciaRetoStarter.getApplicationFrame().asyncReloadProject(project, new Runnable() {
          @Override
          public void run() {
            context.focusInTree(TabTitle.this);
          }
        });
      }
    }
    return result;
  }

  @Override
  public boolean isFocusable() {
    return false;
  }

  @Override
  @Nullable
  public String getToolTipText() {
    return this.associatedFile == null ? null : this.associatedFile.getAbsolutePath();
  }

  public boolean belongFolderOrSame(@Nonnull final File folder) {
    boolean result = false;
    if (this.associatedFile != null) {
      return folder.equals(this.associatedFile) ||
          Paths.toPath(this.associatedFile).startsWith(Paths.toPath(folder));
    }
    return result;
  }

  @Nonnull
  public TabProvider getProvider() {
    return this.parent;
  }

  public void doSafeClose() {
    final boolean close = !this.changed
        || DialogProviderManager.getInstance().getDialogProvider()
        .msgConfirmOkCancel(SciaRetoStarter.getApplicationFrame(), "Non saved file",
            "Close unsaved document '" + makeName() + "\'?");
    if (close) {
      this.context.closeTab(this);
    }
  }

  @Nullable
  public File getAssociatedFile() {
    return this.associatedFile;
  }

  public void setAssociatedFile(@Nullable final File file) {
    this.associatedFile = file;
    updateView();
  }

  public void setChanged(final boolean flag) {
    this.changed = flag;
    updateView();
  }

  public boolean isChanged() {
    return this.changed;
  }

  public void dispose() {
    this.parent.getEditor().dispose();
  }

  public boolean reload(final boolean askUserConfirmationIfChanged) {
    boolean reloaded = false;

    if (askUserConfirmationIfChanged && isChanged() &&
        !DialogProviderManager.getInstance().getDialogProvider()
            .msgConfirmYesNo(SciaRetoStarter.getApplicationFrame(), "File changed",
                String.format("File '%s' is changed, reload?",
                    (this.associatedFile == null ? "..." : this.associatedFile.getName())))) {
      return reloaded;
    }

    final File file = getAssociatedFile();
    if (file != null && file.isFile()) {
      try {
        this.parent.loadContent(file);
        reloaded = true;
      } catch (IOException ex) {
        LOGGER.error("Can't reload file :" + file, ex); //NOI18N
      }
    }
    return reloaded;
  }

  @Nonnull
  public EditorContentType getType() {
    return this.parent.getEditor().getEditorContentType();
  }

  @Nonnull
  private String makeName() {
    final File file = this.associatedFile;
    return file == null ? "Untitled" : file.getName(); //NOI18N
  }

  private void updateView() {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        titleLabel.setText(
            "<html>" + (changed ? "<b>*<u>" : "") + StringEscapeUtils.escapeHtml3(makeName()) +
                (changed ? "</u></b>" : "") + "</html>"); //NOI18N
        revalidate();
      }
    });
  }

}
