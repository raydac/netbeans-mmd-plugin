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
package com.igormaznitsa.sciareto.ui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.AbstractEditor;

public class EditorTabPane extends JTabbedPane implements Iterable<TabTitle> {

  private static final long serialVersionUID = -8971773653667281550L;

  private static final Logger LOGGER = LoggerFactory.getLogger(EditorTabPane.class);

  private final Context context;

  private boolean enabledNotificationAboutChange;

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
        processPopup(e);
      }

      private void processPopup(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          final JPopupMenu menu = makePopupMenu();
          if (menu != null) {
            menu.show(e.getComponent(), e.getX(), e.getY());
            e.consume();
          }
        }
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
            }
            catch (IOException ex) {
              LOGGER.error("Can't save file", ex); //NOI18N
              DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't save document, may be it is read-only! See log!");
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
            }
            catch (IOException ex) {
              LOGGER.error("Can't save file", ex); //NOI18N
              DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't save document, may be it is read-only! See log!");
            }
          }
        });
        result.add(saveAsItem);
      }
      result.add(new JSeparator());

      final JMenuItem closeItem = new JMenuItem("Close");
      closeItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          title.doSafeClose();
        }
      });
      result.add(closeItem);

      final JMenuItem closeOthers = new JMenuItem("Close Other");
      closeOthers.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull ActionEvent e) {
          final List<TabTitle> list = new ArrayList<>();
          for (final TabTitle t : theInstance) {
            if (title != t) {
              list.add(t);
            }
          }
          safeCloseTabs(list.toArray(new TabTitle[list.size()]));
        }
      });
      result.add(closeOthers);

      final JMenuItem closeAll = new JMenuItem("Close All");
      closeAll.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull ActionEvent e) {
          final List<TabTitle> list = new ArrayList<>();
          for (final TabTitle t : theInstance) {
            list.add(t);
          }
          safeCloseTabs(list.toArray(new TabTitle[list.size()]));
        }
      });
      result.add(closeAll);

      result.add(new JSeparator());

      final JMenuItem showInTree = new JMenuItem("Select in Tree");
      showInTree.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          context.focusInTree(title);
        }
      });
      result.add(showInTree);

      final JMenuItem openInSystem = new JMenuItem("Open in System");
      openInSystem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final File file = title.getAssociatedFile();
          if (file != null && file.exists()) {
            UiUtils.openInSystemViewer(file);
          }
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
    if (!foundUnsaved || DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, "Detected unsaved", "Detected unsaved documents! Close anyway?")) {
      this.context.closeTab(titles);
    }
  }

  @Nullable
  public AbstractEditor getCurrentEditor(){
    AbstractEditor result = null;
    
    final int selected = this.getSelectedIndex();
    
    if (selected>=0){
      result = ((TabTitle)this.getTabComponentAt(selected)).getProvider().getEditor();
    }
    
    return result;
  }
  
  public void createTab(@Nonnull final TabProvider panel) {
    super.addTab("...", panel.getEditor().getContainerToShow()); //NOI18N
    final int count = this.getTabCount() - 1;
    final TabTitle tabTitle = panel.getTabTitle();
    this.setTabComponentAt(count, tabTitle);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        panel.getMainComponent().requestFocus();
      }
    });
    this.setSelectedIndex(count);
    this.setToolTipTextAt(count, tabTitle.getToolTipText());
  }

  public boolean focusToFile(@Nonnull final File file) {
    for (int i = 0; i < this.getTabCount(); i++) {
      final TabTitle title = (TabTitle) this.getTabComponentAt(i);
      if (file.equals(title.getAssociatedFile())) {
        this.setSelectedIndex(i);
        ((TabTitle) this.getTabComponentAt(i)).getProvider().focusToEditor();
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
      }
      finally {
        title.disposeEditor();
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
