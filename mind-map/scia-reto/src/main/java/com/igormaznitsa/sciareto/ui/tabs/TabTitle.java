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
package com.igormaznitsa.sciareto.ui.tabs;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.apache.commons.lang.StringEscapeUtils;

import com.igormaznitsa.meta.common.interfaces.Disposable;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.Paths;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.EditorContentType;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;

public final class TabTitle extends JPanel {

  private static final long serialVersionUID = -6534083975320248288L;
  private final JLabel titleLabel;
  private final JButton closeButton;
  private volatile File associatedFile;
  private volatile boolean changed;
  private final Context context;
  private final TabProvider parent;

  private boolean visited;

  private static final Icon NIMBUS_CLOSE_ICON = new ImageIcon(UiUtils.loadIcon("nimbusCloseFrame.png"));

  private static final Logger LOGGER = LoggerFactory.getLogger(TabTitle.class);
  
  public TabTitle(@Nonnull final Context context, @Nonnull final TabProvider parent, @Nullable final File associatedFile) {
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

    final Icon uiCloseIcon = UIManager.getIcon("InternalFrameTitlePane.closeIcon");

    this.closeButton = new JButton(uiCloseIcon == null ? NIMBUS_CLOSE_ICON : uiCloseIcon) {
      private static final long serialVersionUID = -8005282815756047979L;

      @Override
      public String getToolTipText() {
        return theInstance.getToolTipText();
      }

      @Override
      public boolean isFocusable() {
        return false;
      }
    };
    this.closeButton.setToolTipText("Close tab");
    this.closeButton.setBorder(null);
    this.closeButton.setContentAreaFilled(false);
    this.closeButton.setMargin(new Insets(0, 0, 0, 0));
    this.closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.closeButton.setOpaque(false);
    this.closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        doSafeClose();
      }
    });
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
        project.getGroup().refreshProjectFolder(project);
        this.context.focusInTree(this);
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
      return folder.equals(this.associatedFile) || Paths.toPath(this.associatedFile).startsWith(Paths.toPath(folder));
    }
    return result;
  }

  @Nonnull
  public TabProvider getProvider() {
    return this.parent;
  }

  public void doSafeClose() {
    final boolean close = !this.changed || DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel("Non saved file", "Close unsaved document '" + makeName() + "\'?");
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

  public void disposeEditor() {
    final JComponent compo = this.parent.getMainComponent();
    if (compo instanceof Disposable) {
      ((Disposable) compo).dispose();
    }
  }

  public boolean reload(final boolean askUserConfirmationIfChanged) {
    boolean reloaded = false;
  
    if (askUserConfirmationIfChanged && isChanged() && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo("File changed", String.format("File '%s' is changed, reload?", (this.associatedFile == null ? "..." : this.associatedFile.getName())))) {
      return reloaded;
    }
    
    final File file = getAssociatedFile();
    if (file.isFile()){
      try{
        this.parent.loadContent(file);
        reloaded = true;
      }catch(IOException ex){
        LOGGER.error("Can't reload file :"+file, ex);
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
    return file == null ? "Untitled" : file.getName();
  }

  private void updateView() {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        titleLabel.setText("<html>" + (changed ? "<b>*<u>" : "") + StringEscapeUtils.escapeHtml(makeName()) + (changed ? "</u></b>" : "") + "</html>");
        revalidate();
      }
    });
  }

}
