/*
 * Copyright 2015-2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.print;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;

import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.BasicStroke;

import javax.swing.JButton;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;

public class MMDPrintPanel extends JPanel implements HasPreferredFocusComponent {

  static final Color BORDER_COLOR = Color.GRAY;
  static final Stroke BORDER_STYLE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{1f, 3f}, 0f);

  private static final Icon ICO_PRINTER = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_PRINTER);
  private static final Icon ICO_PAGE = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_PAGE);

  
  public enum IconId {
    PRINTER,
    PAGE
  }

  public interface Adaptor {

    void startBackgroundTask (@Nonnull MMDPrintPanel source, @Nonnull String name, @Nonnull Runnable task);

    boolean isDarkTheme (@Nonnull MMDPrintPanel source);

    void onPrintTaskStarted (@Nonnull MMDPrintPanel source);
    
    @Nonnull
    Dimension getPreferredSizeOfPanel(@Nonnull MMDPrintPanel source);
  }

  private static final long serialVersionUID = -2588424836865316862L;

  protected static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");
  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrintPanel.class);
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  
  private PageFormat pageFormat;
  private final Pages previewContainer;
  private double pageZoomFactor;
  private final JComponent preferredFocusedComponent;
  
  private final MindMapPanel mmdPanel;
  private final Adaptor theAdaptor;
  private final JCheckBox checkBoxDrawBorder;
  private PrintPage[][] pages;

  public MMDPrintPanel (@Nullable final Adaptor adaptor, @Nonnull final MindMapPanel mindMapPanel) {
    super(new BorderLayout());
    this.theAdaptor = adaptor == null ? new DefaultMMDPrintPanelAdaptor() : adaptor;
    this.mmdPanel = mindMapPanel;

    super.setPreferredSize(this.theAdaptor.getPreferredSizeOfPanel(this));

    final JScrollPane scrollPane = UI_COMPO_FACTORY.makeScrollPane();
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setJobName("Mind map print job");

    final JToolBar toolBar = UI_COMPO_FACTORY.makeToolBar();
    toolBar.setOrientation(JToolBar.HORIZONTAL);
    toolBar.setFloatable(false);
    final JButton buttonPrint = UI_COMPO_FACTORY.makeButton();
    buttonPrint.setText(BUNDLE.getString("MMDPrintPanel.PrintPages"));
    buttonPrint.setIcon(ICO_PRINTER);
    
    final MMDPrintPanel theInstance = this;

    buttonPrint.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed (@Nonnull final ActionEvent e) {
        splitToPagesForCurrentFormat();
        final PageFormat format = pageFormat;
        final int numOfPages = countPages();
        final boolean drawBorder = checkBoxDrawBorder.isSelected();
        
        printerJob.setPageable(new Pageable() {
          @Override
          public int getNumberOfPages () {
            return numOfPages;
          }

          @Override
          @Nonnull
          public PageFormat getPageFormat (final int pageIndex) throws IndexOutOfBoundsException {
            final PrintPage thepage = findPageForIndex(pageIndex);
            if (thepage == null) throw new IndexOutOfBoundsException();
            return format;
          }

          @SuppressWarnings("unchecked")
          @Override
          @Nonnull
          public Printable getPrintable (final int pageIndex) throws IndexOutOfBoundsException {
            final PrintPage thePage = findPageForIndex(pageIndex);
            if (thePage == null) throw new IndexOutOfBoundsException();
            return new Printable() {
              @SuppressWarnings("unchecked")
              @Override
              public int print (@Nonnull final Graphics graphics, @Nonnull final PageFormat format, final int pageIndex) throws PrinterException {
                final Graphics2D gfx = (Graphics2D) graphics;

                if (thePage == null) {
                  return Printable.NO_SUCH_PAGE;
                }
                else {
                  gfx.translate((int) format.getImageableX(), (int) format.getImageableY());
                  thePage.print(gfx);

                  if (drawBorder) {
                    final Stroke stroke = gfx.getStroke();
                    gfx.setStroke(BORDER_STYLE);
                    gfx.draw(new Rectangle2D.Double(0d, 0d, format.getImageableWidth(), format.getImageableHeight()));
                    gfx.setColor(BORDER_COLOR);
                    gfx.setStroke(stroke);
                  }
                  gfx.translate(-(int) format.getImageableX(), -(int) format.getImageableY());
                  return Printable.PAGE_EXISTS;
                }
              }
            };
          }
        });

        
        if (printerJob.printDialog()) {
          theAdaptor.startBackgroundTask(theInstance, BUNDLE.getString("MMDPrintPanel.JobTitle"), new Runnable() {
            @Override
            public void run () {
              try {
                LOGGER.info("Start print job");
                printerJob.print();
              }
              catch (PrinterException ex) {
                LOGGER.error("Print error", ex);
                throw new RuntimeException("Error during print job", ex);
              }
            }
          });
          theAdaptor.onPrintTaskStarted(theInstance);
        }
      }
    });
    toolBar.add(buttonPrint);

    final JButton buttonPrintOptions = UI_COMPO_FACTORY.makeButton();
    buttonPrintOptions.setText(BUNDLE.getString("MMDPrintPanel.PageSetup"));
    buttonPrintOptions.setIcon(ICO_PAGE);
    buttonPrintOptions.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed (@Nonnull final ActionEvent e) {
        pageFormat = printerJob.pageDialog(pageFormat);
        splitToPagesForCurrentFormat();
        scrollPane.revalidate();
        scrollPane.repaint();
      }
    });
    toolBar.add(buttonPrintOptions);

    final List<String> scalesList = new ArrayList<String>();
    scalesList.add("10 %");
    for (int i = 25; i < 225; i += 25) {
      scalesList.add(Integer.toString(i) + " %");
    }

    final String[] scales = scalesList.toArray(new String[scalesList.size()]);
    final JComboBox comboBoxScale = UI_COMPO_FACTORY.makeComboBox();
    comboBoxScale.setModel(new DefaultComboBoxModel(scales));
    comboBoxScale.setSelectedItem("100 %");
    this.pageZoomFactor = 1.0d;
    comboBoxScale.setEditable(false);

    comboBoxScale.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed (@Nonnull final ActionEvent e) {
        final int percent = Integer.parseInt(comboBoxScale.getSelectedItem().toString().split("\\s")[0]);
        pageZoomFactor = (double) percent / 100d;
        splitToPagesForCurrentFormat();
        scrollPane.revalidate();
        scrollPane.getViewport().revalidate();
        scrollPane.repaint();
      }
    });
    comboBoxScale.setMaximumSize(comboBoxScale.getPreferredSize());
    toolBar.addSeparator();
    toolBar.add(comboBoxScale);

    toolBar.addSeparator();
    this.checkBoxDrawBorder = UI_COMPO_FACTORY.makeCheckBox();
    this.checkBoxDrawBorder.setSelected(true);
    this.checkBoxDrawBorder.setText(BUNDLE.getString("MMDPrintPanel.DrawBorder"));
    this.checkBoxDrawBorder.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed (@Nonnull ActionEvent e) {
        scrollPane.repaint();
      }
    });
    toolBar.add(this.checkBoxDrawBorder);

    this.add(toolBar, BorderLayout.NORTH);

    this.pageFormat = printerJob.defaultPage();
    this.splitToPagesForCurrentFormat();
    this.previewContainer = new Pages(this);
    scrollPane.getViewport().setView(this.previewContainer);

    this.preferredFocusedComponent = scrollPane;


    this.add(scrollPane, BorderLayout.CENTER);
  }

  @Override
  @Nonnull
  public JComponent getComponentPreferredForFocus () {
    return this.preferredFocusedComponent;
  }
  
  @Nonnull
  PageFormat getPageFormat () {
    return this.pageFormat;
  }

  int countPages () {
    int result = 0;
    for (final PrintPage[] p : this.pages) {
      result += p.length;
    }
    return result;
  }

  @Nonnull
  @MustNotContainNull
  PrintPage[][] getPages () {
    return this.pages;
  }

  @Nullable
  PrintPage findPageForIndex (final int value) {
    int i = 0;
    for (final PrintPage[] row : this.pages) {
      for (final PrintPage page : row) {
        if (i == value) {
          return page;
        }
        i++;
      }
    }
    return null;
  }

  double getScale () {
    return this.pageZoomFactor;
  }

  private void splitToPagesForCurrentFormat () {
    final MMDPrint printer = new MMDPrint(this.mmdPanel, (int) this.pageFormat.getImageableWidth(),
        (int) this.pageFormat.getImageableHeight(), 1.0d);
    this.pages = printer.getPages();
  }

  boolean isDrawBorder () {
    return this.checkBoxDrawBorder.isSelected();
  }

  boolean isDarkTheme () {
    return this.theAdaptor.isDarkTheme(this);
  }
}
