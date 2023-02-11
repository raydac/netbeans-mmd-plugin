package com.igormaznitsa.mindmap.ide.commons.editors;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;

import com.igormaznitsa.mindmap.ide.commons.SortedTreeModelWrapper;
import com.igormaznitsa.mindmap.ide.commons.preferences.MmcI18n;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class AbstractMindMapTreePanel implements HasPreferredFocusComponent {

  private static final Map<IconId, Icon> ICON_CACHE = new EnumMap<>(IconId.class);
  private final SortedTreeModelWrapper sortedModel;
  private final UIComponentFactory uiComponentFactory;
  private final ResourceBundle resourceBundle;
  private JPanel panel;
  private JButton buttonCollapseAll;
  private JButton buttonExpandAll;
  private JButton buttonUnselect;
  private JPanel toolBarPanel;
  private JTree treeMindMap;
  private JScrollPane treeScrollPane;

  public AbstractMindMapTreePanel(
      final UIComponentFactory uiComponentFactoryProvider,
      final MindMap map,
      final ExtraTopic selectedTopicUid,
      final boolean expandAll,
      final ActionListener listener
  ) {
    this.uiComponentFactory = uiComponentFactoryProvider;
    this.resourceBundle = MmcI18n.getInstance().findBundle();
    initComponents();

    this.treeMindMap.setCellRenderer(this.makeTreeCellRenderer(this::prepareTreeCellRender));
    this.treeMindMap.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    if (map != null) {
      this.sortedModel = new SortedTreeModelWrapper(map,
          (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(o1),
              String.valueOf(o2)));
      this.treeMindMap.setModel(this.sortedModel);
      if (selectedTopicUid != null) {
        final Topic topic = map.findTopicForLink(selectedTopicUid);
        if (topic != null) {
          this.treeMindMap.setSelectionPath(new TreePath(topic.getPath()));
        }
      }
    } else {
      this.sortedModel = null;
    }

    this.treeMindMap.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() > 1) {
          if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, "doubleClick"));
          }
        }
      }
    });


    this.panel.setPreferredSize(new Dimension(450, 400));

    if (expandAll) {
      Utils.foldUnfoldTree(this.treeMindMap, true);
    }
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.treeMindMap;
  }

  protected JButton preprocessToolbarButton(final JButton button) {
    return button;
  }

  public JPanel getPanel() {
    return this.panel;
  }

  private Icon getIcon(final IconId id) {
    return ICON_CACHE.computeIfAbsent(id, this::findIcon);
  }

  protected Icon findIcon(final IconId iconId) {
    return null;
  }

  public JTree getTree() {
    return this.treeMindMap;
  }

  public Topic getSelectedTopic() {
    final TreePath selected = this.treeMindMap.getSelectionPath();
    return selected == null ? null : (Topic) selected.getLastPathComponent();
  }

  private void initComponents() {
    this.panel = this.uiComponentFactory.makePanel();

    final GridBagConstraints toolBarConstraints = new GridBagConstraints();
    toolBarConstraints.gridy = 0;
    toolBarConstraints.weightx = 1;
    toolBarConstraints.weighty = 1;
    toolBarConstraints.fill = GridBagConstraints.BOTH;
    toolBarConstraints.insets = new Insets(2, 2, 2, 2);

    this.treeScrollPane = this.uiComponentFactory.makeScrollPane();
    this.treeMindMap = this.uiComponentFactory.makeTree();

    this.toolBarPanel = this.uiComponentFactory.makePanel();
    this.toolBarPanel.setLayout(new GridBagLayout());

    this.buttonExpandAll = this.uiComponentFactory.makeButton();
    this.buttonCollapseAll = this.uiComponentFactory.makeButton();
    this.buttonUnselect = this.uiComponentFactory.makeButton();

    this.panel.setLayout(new BorderLayout());

    this.treeScrollPane.setViewportView(treeMindMap);

    this.panel.add(this.treeScrollPane, CENTER);

    this.buttonExpandAll.setIcon(this.getIcon(IconId.BUTTON_UNFOLD_ALL));
    this.buttonExpandAll.setText(
        this.resourceBundle.getString("MindMapTreePanel.buttonExpandAll.text_1"));
    this.buttonExpandAll.setFocusable(false);
    this.buttonExpandAll.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonExpandAll.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonExpandAll.addActionListener(this::buttonExpandAllActionPerformed);
    this.toolBarPanel.add(this.preprocessToolbarButton(this.buttonExpandAll), toolBarConstraints);

    this.buttonCollapseAll.setIcon(this.getIcon(IconId.BUTTON_FOLD_ALL));
    this.buttonCollapseAll.setText(
        this.resourceBundle.getString("MindMapTreePanel.buttonCollapseAll.text_1"));
    this.buttonCollapseAll.setFocusable(false);
    this.buttonCollapseAll.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonCollapseAll.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonCollapseAll.addActionListener(this::buttonCollapseAllActionPerformed);
    this.toolBarPanel.add(this.preprocessToolbarButton(this.buttonCollapseAll), toolBarConstraints);

    this.buttonUnselect.setIcon(this.getIcon(IconId.BUTTON_SELECT_NONE));
    this.buttonUnselect.setText(
        this.resourceBundle.getString("MindMapTreePanel.buttonUnselect.text_1"));
    this.buttonUnselect.setFocusable(false);
    this.buttonUnselect.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonUnselect.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonUnselect.addActionListener(this::buttonUnselectActionPerformed);
    this.toolBarPanel.add(this.preprocessToolbarButton(this.buttonUnselect), toolBarConstraints);

    toolBarConstraints.weightx = 1000;
    this.toolBarPanel.add(Box.createHorizontalGlue(), toolBarConstraints);

    this.panel.add(toolBarPanel, NORTH);
  }

  private void buttonUnselectActionPerformed(ActionEvent evt) {
    this.treeMindMap.setSelectionPath(null);
  }

  private void buttonExpandAllActionPerformed(ActionEvent evt) {
    Utils.foldUnfoldTree(this.treeMindMap, true);
  }

  private void buttonCollapseAllActionPerformed(ActionEvent evt) {
    Utils.foldUnfoldTree(this.treeMindMap, false);
  }

  public void dispose() {
    if (this.sortedModel != null) {
      this.sortedModel.dispose();
    }
  }

  protected TreeCellRenderer makeTreeCellRenderer(
      BiFunction<Object, Component, Component> cellRenderFunction) {
    return new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(
          final JTree tree,
          final Object value,
          final boolean selected,
          final boolean expanded,
          final boolean leaf,
          final int row,
          final boolean hasFocus) {
        return cellRenderFunction.apply(value,
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus));
      }
    };
  }

  protected Component prepareTreeCellRender(final Object value, final Component component) {
    final JLabel result = (JLabel) component;
    if (value instanceof Topic) {
      result.setIcon(getIconForTopic((Topic) value));
      result.setText(this.extractTextFromTopic((Topic) value));
    }
    return result;
  }

  protected Icon getIconForTopic(final Topic topic) {
    switch (topic.getTopicLevel()) {
      case 0:
        return this.getIcon(IconId.TREE_DOCUMENT);
      case 1:
        return this.getIcon(IconId.BALL_BLUE);
      default:
        return this.getIcon(IconId.BALL_GOLD);
    }
  }

  protected String extractTextFromTopic(final Topic topic) {
    return Utils.makeShortTextVersion(Utils.getFirstLine(topic.getText()), 20);
  }

  public enum IconId {
    TREE_DOCUMENT,
    BALL_BLUE,
    BALL_GOLD,
    BUTTON_FOLD_ALL,
    BUTTON_UNFOLD_ALL,
    BUTTON_SELECT_NONE
  }

}
