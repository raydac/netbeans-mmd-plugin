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
package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.UiUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class AboutPanel extends javax.swing.JPanel implements JHtmlLabel.LinkListener {

  private static final long serialVersionUID = -3231534203788095969L;

  private static final Logger LOGGER = LoggerFactory.getLogger(AboutPanel.class);
  
  private static class ThirdPartLicense {

    private final String library;
    private final String libraryUrl;
    private final String license;
    private final String licenseUrl;
    
    ThirdPartLicense(@Nonnull final String library, @Nonnull final String libraryUrl, @Nonnull final String license, @Nonnull final String licenseUrl) {
      this.library = library;
      this.license = license;
      this.libraryUrl = libraryUrl;
      this.licenseUrl = licenseUrl;
    }
  }

  private final List<ThirdPartLicense> thirdParts = new ArrayList<>();
  
  public AboutPanel() {
    initComponents();

    this.tabbedPaneContent.putClientProperty("JTabbedPane.hasFullBorder", false);
    
    final Image loadedSplash = UiUtils.loadIcon("splash.png");
    int maxWidth = Integer.MAX_VALUE;
    if (loadedSplash != null) {
      final Image scaled = Utils.scaleImage(loadedSplash, 0.75d, 0.75d, 1.0d);
      this.labelSplash.setIcon(new ImageIcon(scaled));
      maxWidth = scaled.getWidth(null)+16;
    }

    this.tabbedPaneContent.setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
    this.tabbedPaneContent.setMinimumSize(new Dimension(maxWidth, 32));
    this.tabbedPaneContent.setPreferredSize(new Dimension(maxWidth, 320));
    
    final String pluginAPIVersion = MindMapPlugin.API.toString();
    final String formatVersion = MindMap.FORMAT_VERSION;

    final Properties props = new Properties();
    props.setProperty("plugin.api", pluginAPIVersion); //NOI18N
    props.setProperty("format.version", formatVersion); //NOI18N
    props.setProperty("ideversion", SciaRetoStarter.IDE_VERSION.toString()); //NOI18N

    this.thirdParts.add(new ThirdPartLicense("FatCow Farm-Fresh Web Icons", "http://www.fatcow.com/free-icons",  "CC BY 3.0", "https://creativecommons.org/licenses/by/3.0/"));
    this.thirdParts.add(new ThirdPartLicense("Java Universal Network/Graph Framework", "https://github.com/jrtom/jung", "BSD 3", "https://raw.githubusercontent.com/jrtom/jung/master/LICENSE"));
    this.thirdParts.add(new ThirdPartLicense("RSynaxTextArea", "https://github.com/bobbylight/RSyntaxTextArea", "modified BSD license", "https://raw.githubusercontent.com/bobbylight/RSyntaxTextArea/master/src/main/dist/RSyntaxTextArea.License.txt"));
    this.thirdParts.add(new ThirdPartLicense("FlatLaf", "https://github.com/JFormDesigner/FlatLaf", "Apache License, Version 2.0", "https://raw.githubusercontent.com/JFormDesigner/FlatLaf/main/LICENSE"));
    this.thirdParts.add(new ThirdPartLicense("PlantUML", "http://plantuml.com/", "GPL License", "https://www.gnu.org/copyleft/gpl.html"));
    this.thirdParts.add(new ThirdPartLicense("JLaTeXMath Library", "https://github.com/opencollab/jlatexmath", "GPL License", "https://github.com/opencollab/jlatexmath/blob/master/LICENSE"));
    this.thirdParts.add(new ThirdPartLicense("Apache Batik", "https://xmlgraphics.apache.org/batik/", "Apache License, Version 2.0", "https://xmlgraphics.apache.org/batik/license.html"));
    
    this.tableThirdPartLibraries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N

    this.tableThirdPartLibraries.setModel(new TableModel() {
      @Override
      public int getRowCount() {
        return thirdParts.size();
      }

      @Override
      public int getColumnCount() {
        return 2;
      }

      @Override
      @Nonnull
      public String getColumnName(final int columnIndex) {
        switch(columnIndex) {
          case 0 : return bundle.getString("AboutPanel.grid.title.component");
          case 1 : return bundle.getString("AboutPanel.grid.title.license");
          default:return "<unknown>";
        }
      }

      @Override
      @Nonnull
      public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
      }

      @Override
      public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
      }

      @Override
      @Nonnull
      public Object getValueAt(final int rowIndex, final int columnIndex) {
        final ThirdPartLicense lic = thirdParts.get(rowIndex);
        switch(columnIndex){
          case 0 : return lic.library;
          case 1 : return lic.license;
          default: return "...";
        }
      }

      @Override
      public void setValueAt(@Nonnull final Object aValue, final int rowIndex, final int columnIndex) {
      }

      @Override
      public void addTableModelListener(@Nonnull final TableModelListener l) {
      }

      @Override
      public void removeTableModelListener(@Nonnull final TableModelListener l) {
      }
    });
    
    this.scrollPanelThirdParts.setPreferredSize(new Dimension(100,196));
    
    this.tableThirdPartLibraries.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (!e.isConsumed() && e.getClickCount() > 1) {
          e.consume();
          openSelectedProductUrl();
        }
      }
    });
    
    this.tableThirdPartLibraries.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
          openSelectedProductUrl();
        }
      }
            
      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
      }
            
    });
    
    this.textLabel.replaceMacroses(props);
    this.textLabel.addLinkListener(this);
    this.textLabel.setShowLinkAddressInTooltip(true);
    
    this.textLabelDonation.replaceMacroses(props);
    this.textLabelDonation.addLinkListener(this);
    this.textLabelDonation.setShowLinkAddressInTooltip(true);
    
    this.doLayout();
  }

  private void openSelectedProductUrl() {
    final int row = this.tableThirdPartLibraries.getSelectedRow();
    if (row >= 0) {
      final ThirdPartLicense rec = this.thirdParts.get(row);
      try {
        UiUtils.browseURI(new URI(rec.libraryUrl), false);
      } catch (final URISyntaxException ex) {
        LOGGER.warn("Can't open url " + rec.libraryUrl + " : " + ex.getMessage());
      }
    }
  }
  
  @Override
  public void onLinkActivated(final JHtmlLabel source, final String href) {
    try{
      UiUtils.browseURI(new URI(href), false);
    }catch(Exception ex){
      LOGGER.error("Can't process link in 'About'", ex); //NOI18N
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        labelSplash = new javax.swing.JLabel();
        tabbedPaneContent = new javax.swing.JTabbedPane();
        textLabel = new com.igormaznitsa.sciareto.ui.misc.JHtmlLabel();
        scrollPanelThirdParts = new javax.swing.JScrollPane();
        tableThirdPartLibraries = new javax.swing.JTable();
        panelDonate = new javax.swing.JPanel();
        textLabelDonation = new com.igormaznitsa.sciareto.ui.misc.JHtmlLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));

        setLayout(new java.awt.GridBagLayout());

        labelSplash.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSplash.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.weighty = 1000.0;
        add(labelSplash, gridBagConstraints);

        textLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        textLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
        textLabel.setText(bundle.getString("AboutPanel.labelInfo.text")); // NOI18N
        textLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        tabbedPaneContent.addTab(bundle.getString("AboutPanel.textLabel.TabConstraints.tabTitle"), textLabel); // NOI18N

        scrollPanelThirdParts.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tableThirdPartLibraries.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tableThirdPartLibraries.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableThirdPartLibraries.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        scrollPanelThirdParts.setViewportView(tableThirdPartLibraries);

        tabbedPaneContent.addTab(bundle.getString("AboutPanel.scrollPanelThirdParts.TabConstraints.tabTitle"), scrollPanelThirdParts); // NOI18N

        panelDonate.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelDonate.setLayout(new java.awt.BorderLayout());

        textLabelDonation.setText(bundle.getString("AboutPanel.textLabelDonation.text")); // NOI18N
        textLabelDonation.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        textLabelDonation.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        panelDonate.add(textLabelDonation, java.awt.BorderLayout.CENTER);

        tabbedPaneContent.addTab(bundle.getString("AboutPanel.panelDonate.TabConstraints.tabTitle"), panelDonate); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(tabbedPaneContent, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 999999.0;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel labelSplash;
    private javax.swing.JPanel panelDonate;
    private javax.swing.JScrollPane scrollPanelThirdParts;
    private javax.swing.JTabbedPane tabbedPaneContent;
    private javax.swing.JTable tableThirdPartLibraries;
    private com.igormaznitsa.sciareto.ui.misc.JHtmlLabel textLabel;
    private com.igormaznitsa.sciareto.ui.misc.JHtmlLabel textLabelDonation;
    // End of variables declaration//GEN-END:variables
}
