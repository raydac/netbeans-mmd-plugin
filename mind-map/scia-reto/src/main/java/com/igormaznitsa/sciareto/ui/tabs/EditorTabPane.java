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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.AbstractEditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EditorTabPane extends JTabbedPane implements Iterable<TabTitle> {

  private static final long serialVersionUID = -8971773653667281550L;

  private static final Logger LOGGER = LoggerFactory.getLogger(EditorTabPane.class);

  private final Context context;

  private boolean enabledNotificationAboutChange;

  private final List<ActionListener> maxMinEditorListeners = new CopyOnWriteArrayList<>();

  public EditorTabPane(@Nonnull final Context context) {
    super(JTabbedPane.TOP);
    this.context = context;
    this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        processPopup(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        processPopup(e);
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (!processPopup(e) && e.getClickCount() > 1) {
          final ActionEvent event = new ActionEvent(EditorTabPane.this, 0, "MAXMINEDITOR");
          for (final ActionListener l : maxMinEditorListeners) {
            l.actionPerformed(event);
          }
        }
      }

      private boolean processPopup(@Nonnull final MouseEvent e) {
        boolean popup = false;
        if (e.isPopupTrigger()) {
          final JPopupMenu menu = makePopupMenu();
          if (menu != null) {
            menu.show(e.getComponent(), e.getX(), e.getY());
            e.consume();
            popup = true;
          }
        }
        return popup;
      }
    });

    this.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(@Nonnull final ChangeEvent e) {
        if (enabledNotificationAboutChange) {
          ((MainFrame) context).processTabChanged(getCurrentTitle());
        }
      }
    });

  }

  public void addMaxMinEditorListener(@Nonnull final ActionListener l) {
    this.maxMinEditorListeners.add(l);
  }

  public void removeMaxMinEditorListener(@Nonnull final ActionListener l) {
    this.maxMinEditorListeners.remove(l);
  }

  public void setNotifyForTabChanged(final boolean enable) {
    this.enabledNotificationAboutChange = enable;
  }

  public boolean hasEditableAndChangedDocument() {
    boolean result = false;

    for (final TabTitle t : this) {
      if (t != null && t.isChanged()) {
        result = true;
        break;
      }
    }

    return result;
  }

  @Nullable
  public TabTitle getCurrentTitle() {
    final int index = this.getSelectedIndex();
    return index < 0 ? null : (TabTitle) this.getTabComponentAt(index);
  }

  @Nonnull
  @MustNotContainNull
  public List<TabTitle> findListOfRelatedTabs(@Nonnull final File file) {
    final List<TabTitle> result = new ArrayList<>();
    for (final TabTitle t : this) {
      if (t.belongFolderOrSame(file)) {
        result.add(t);
      }
    }
    return result;
  }

  public boolean replaceFileLink(@Nonnull final File oldFile, @Nonnull final File newFile) {
    boolean replaced = false;
    int index = 0;
    for (final TabTitle title : this) {
      if (oldFile.equals(title.getAssociatedFile())) {
        title.setAssociatedFile(newFile);
        this.setToolTipTextAt(index, title.getToolTipText());
        replaced |= true;
      }
      index++;
    }
    return replaced;
  }

  public boolean isEmpty() {
    return this.getTabCount() == 0;
  }

  @Nullable
  private JPopupMenu makePopupMenu() {
    final EditorTabPane theInstance = this;
    final int selected = this.getSelectedIndex();
    JPopupMenu result = null;
    if (selected >= 0) {
      final TabTitle title = (TabTitle) this.getTabComponentAt(selected);
      result = new JPopupMenu();

      if (title.isChanged()) {
        final JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            try {
              title.save();
            } catch (IOException ex) {
              LOGGER.error("Can't save file", ex); //NOI18N
              DialogProviderManager.getInstance().getDialogProvider()
                  .msgError(SciaRetoStarter.getApplicationFrame(),
                      "Can't save document, may be it is read-only! See log!");
            }
          }
        });
        result.add(saveItem);
      }

      if (title.getProvider().isSaveable()) {
        final JMenuItem saveAsItem = new JMenuItem("Save As..");
        saveAsItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            try {
              title.saveAs();
            } catch (IOException ex) {
              LOGGER.error("Can't save file", ex); //NOI18N
              DialogProviderManager.getInstance().getDialogProvider()
                  .msgError(SciaRetoStarter.getApplicationFrame(),
                      "Can't save document, may be it is read-only! See log!");
            }
          }
        });
        result.add(saveAsItem);
      }
      result.add(new JSeparator());

      final JMenuItem closeItem = new JMenuItem("Close");
      closeItem.addActionListener((@Nonnull final ActionEvent e) -> {
        title.doSafeClose();
      });
      result.add(closeItem);

      final JMenuItem closeOthers = new JMenuItem("Close Others");
      closeOthers.addActionListener((@Nonnull ActionEvent e) -> {
        final List<TabTitle> list = new ArrayList<>();
        for (final TabTitle t : theInstance) {
          if (title != t) {
            list.add(t);
          }
        }
        safeCloseTabs(list.toArray(new TabTitle[list.size()]));
      });
      result.add(closeOthers);

      final JMenuItem closeAll = new JMenuItem("Close All");
      closeAll.addActionListener((@Nonnull ActionEvent e) -> {
        final List<TabTitle> list = new ArrayList<>();
        for (final TabTitle t : theInstance) {
          list.add(t);
        }
        safeCloseTabs(list.toArray(new TabTitle[list.size()]));
      });
      result.add(closeAll);

      result.add(new JSeparator());

      final JMenuItem showInTree = new JMenuItem("Select in Tree");
      showInTree.addActionListener((ActionEvent e) -> {
        context.focusInTree(title);
      });
      result.add(showInTree);

      final JMenuItem openInSystem = new JMenuItem("Open in System");
      openInSystem.addActionListener((ActionEvent e) -> {
        final File file = title.getAssociatedFile();
        if (file != null && file.exists()) {
          UiUtils.openInSystemViewer(file);
        }
      });
      result.add(openInSystem);
    }
    return result;
  }

  private void safeCloseTabs(@Nonnull @MustNotContainNull final TabTitle... titles) {
    boolean foundUnsaved = false;
    for (final TabTitle t : titles) {
      foundUnsaved |= t.isChanged();
    }
    if (!foundUnsaved
        || DialogProviderManager.getInstance().getDialogProvider()
        .msgConfirmOkCancel(SciaRetoStarter.getApplicationFrame(),
            "Detected unsaved", "Detected unsaved documents! Close anyway?")) {
      this.context.closeTab(titles);
    }
  }

  @Nullable
  public AbstractEditor getCurrentEditor() {
    AbstractEditor result = null;

    final int selected = this.getSelectedIndex();

    if (selected >= 0) {
      result = ((TabTitle) this.getTabComponentAt(selected)).getProvider().getEditor();
    }

    return result;
  }

  public void createTab(@Nonnull final TabProvider panel) {
    super.addTab("...", panel.getEditor().getContainerToShow()); //NOI18N
    final int count = this.getTabCount() - 1;
    final TabTitle tabTitle = panel.getTabTitle();
    this.setTabComponentAt(count, tabTitle);
    SwingUtilities.invokeLater(() -> {
      panel.getMainComponent().requestFocus();
    });
    this.setSelectedIndex(count);
    this.setToolTipTextAt(count, tabTitle.getToolTipText());
  }

  public boolean focusToFile(@Nonnull final File file, final int line) {
    for (int i = 0; i < this.getTabCount(); i++) {
      final TabTitle title = (TabTitle) this.getTabComponentAt(i);
      if (file.equals(title.getAssociatedFile())) {
        this.setSelectedIndex(i);
        ((TabTitle) this.getTabComponentAt(i)).getProvider().focusToEditor(line);
        return true;
      }
    }
    return false;
  }

  public boolean removeTab(@Nonnull final TabTitle title) {
    int index = -1;
    for (int i = 0; i < this.getTabCount(); i++) {
      if (this.getTabComponentAt(i) == title) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      try {
        this.removeTabAt(index);
      } finally {
        title.dispose();
      }
      return true;
    }
    return false;
  }

  @Override
  @Nonnull
  public Iterator<TabTitle> iterator() {
    final List<TabTitle> result = new ArrayList<>();
    for (int i = 0; i < this.getTabCount(); i++) {
      result.add((TabTitle) this.getTabComponentAt(i));
    }
    return result.iterator();
  }

}
