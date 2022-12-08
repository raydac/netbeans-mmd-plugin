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

package com.igormaznitsa.mindmap.print;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class MMDPrintPanel extends JPanel implements HasPreferredFocusComponent {

  static final Color BORDER_COLOR = Color.GRAY;
  static final Stroke BORDER_STYLE = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {1f, 3f}, 0f);
  private static final Icon ICO_PRINTER = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_PRINTER);
  private static final Icon ICO_PAGE = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_PAGE);
  private static final Icon ICO_OPTIONS = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_OPTIONS);
  private static final long serialVersionUID = -2588424836865316862L;
  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrintPanel.class);
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  private final Pages previewContainer;
  private final JComponent preferredFocusedComponent;
  private final PrintableObject printableObject;
  private final Adaptor theAdaptor;
  private final JCheckBox checkBoxDrawBorder;
  private final JCheckBox checkBoxDrawAsImage;
  private final DialogProvider dialogProvider;
  private final int SCROLL_UNIT = 16;
  private final int SCROLL_BLOCK = SCROLL_UNIT * 8;
  private PageFormat pageFormat;
  private double pageZoomFactor;
  private PrintPage[][] pages;
  private MMDPrintOptions options = new MMDPrintOptions();

  private final ResourceBundle resourceBundle = MmdI18n.getInstance().findBundle();

  public MMDPrintPanel(final DialogProvider dialogProvider, final Adaptor adaptor,
                       final PrintableObject printableObject) {
    super(new BorderLayout());
    this.dialogProvider = dialogProvider;

    this.theAdaptor = adaptor == null ? new DefaultMMDPrintPanelAdaptor() : adaptor;
    this.printableObject = printableObject;

    final JScrollPane scrollPane = UI_COMPO_FACTORY.makeScrollPane();
    scrollPane.getHorizontalScrollBar().setBlockIncrement(SCROLL_BLOCK);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT);
    scrollPane.getVerticalScrollBar().setBlockIncrement(SCROLL_BLOCK);
    scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);

    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setJobName("Mind map print job");

    final JToolBar toolBar = UI_COMPO_FACTORY.makeToolBar();
    toolBar.setOrientation(JToolBar.HORIZONTAL);
    toolBar.setFloatable(false);

    final JButton buttonPrint = UI_COMPO_FACTORY.makeButton();
    buttonPrint.setText(this.resourceBundle.getString("MMDPrintPanel.PrintPages"));
    buttonPrint.setIcon(ICO_PRINTER);

    buttonPrint.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        splitToPagesForCurrentFormat();
        final PageFormat format = pageFormat;
        final int numOfPages = countPages();
        final boolean drawBorder = checkBoxDrawBorder.isSelected();

        printerJob.setPageable(new Pageable() {
          @Override
          public int getNumberOfPages() {
            return numOfPages;
          }

          @Override
          public PageFormat getPageFormat(final int pageIndex) throws IndexOutOfBoundsException {
            final PrintPage thepage = findPageForIndex(pageIndex);
            if (thepage == null) {
              throw new IndexOutOfBoundsException();
            }
            return format;
          }

          @Override
          public Printable getPrintable(final int pageIndex) throws IndexOutOfBoundsException {
            final PrintPage thePage = findPageForIndex(pageIndex);
            if (thePage == null) {
              throw new IndexOutOfBoundsException();
            }
            return (graphics, format1, pageIndex1) -> {
              final Graphics2D gfx = (Graphics2D) graphics;

              gfx.translate((int) format1.getImageableX(), (int) format1.getImageableY());
              thePage.print(gfx);

              if (drawBorder) {
                final Stroke stroke = gfx.getStroke();
                gfx.setStroke(BORDER_STYLE);
                gfx.draw(new Rectangle2D.Double(0d, 0d, format1.getImageableWidth(),
                    format1.getImageableHeight()));
                gfx.setColor(BORDER_COLOR);
                gfx.setStroke(stroke);
              }
              gfx.translate(-(int) format1.getImageableX(), -(int) format1.getImageableY());
              return Printable.PAGE_EXISTS;
            };
          }
        });

        if (printerJob.printDialog()) {
          theAdaptor.startBackgroundTask(MMDPrintPanel.this,
              resourceBundle.getString("MMDPrintPanel.JobTitle"),
              () -> {
                try {
                  LOGGER.info("Start print job");
                  printerJob.print();
                } catch (PrinterException ex) {
                  LOGGER.error("Print error", ex);
                  throw new RuntimeException("Error during print job", ex);
                }
              });
          theAdaptor.onPrintTaskStarted(MMDPrintPanel.this);
        }
      }
    });
    toolBar.add(buttonPrint);

    final JButton buttonPageSetup = UI_COMPO_FACTORY.makeButton();
    buttonPageSetup.setText(this.resourceBundle.getString("MMDPrintPanel.PageSetup"));
    buttonPageSetup.setIcon(ICO_PAGE);
    buttonPageSetup.addActionListener(e -> {
      pageFormat = printerJob.pageDialog(pageFormat);
      splitToPagesForCurrentFormat();
      scrollPane.revalidate();
      scrollPane.getViewport().revalidate();
      scrollPane.repaint();
    });
    toolBar.add(buttonPageSetup);

    final JButton buttonPrintOptions = UI_COMPO_FACTORY.makeButton();
    buttonPrintOptions.setText(this.resourceBundle.getString("MMDPrintPanel.PrintOptions"));
    buttonPrintOptions.setIcon(ICO_OPTIONS);
    buttonPrintOptions.addActionListener(e -> {
      final MMDPrintOptionsPanel panel = new MMDPrintOptionsPanel(options);
      if (dialogProvider.msgOkCancel(MMDPrintPanel.this,
          this.resourceBundle.getString("MMDPrintOptionsPanel.Title"),
          panel)) {
        options = panel.getOptions();
        splitToPagesForCurrentFormat();
        scrollPane.revalidate();
        scrollPane.getViewport().revalidate();
        scrollPane.repaint();
      }
    });
    toolBar.add(buttonPrintOptions);

    final List<String> scalesList = new ArrayList<>();
    scalesList.add("10 %");
    for (int i = 25; i < 225; i += 25) {
      scalesList.add(i + " %");
    }

    final String[] scales = scalesList.toArray(new String[0]);
    final JComboBox comboBoxScale = UI_COMPO_FACTORY.makeComboBox();
    comboBoxScale.setModel(new DefaultComboBoxModel(scales));
    comboBoxScale.setSelectedItem("100 %");
    this.pageZoomFactor = 1.0d;
    comboBoxScale.setEditable(false);

    comboBoxScale.addActionListener(e -> {
      final int percent =
          Integer.parseInt(comboBoxScale.getSelectedItem().toString().split("\\s")[0]);
      pageZoomFactor = (double) percent / 100d;
      splitToPagesForCurrentFormat();
      scrollPane.revalidate();
      scrollPane.getViewport().revalidate();
      scrollPane.repaint();
    });
    comboBoxScale.setMaximumSize(comboBoxScale.getPreferredSize());
    toolBar.addSeparator();
    toolBar.add(comboBoxScale);

    toolBar.addSeparator();

    this.checkBoxDrawBorder = UI_COMPO_FACTORY.makeCheckBox();
    this.checkBoxDrawBorder.setSelected(true);
    this.checkBoxDrawBorder.setText(this.resourceBundle.getString("MMDPrintPanel.DrawBorder"));
    this.checkBoxDrawBorder.addActionListener(e -> scrollPane.repaint());
    toolBar.add(this.checkBoxDrawBorder);

    this.checkBoxDrawAsImage = UI_COMPO_FACTORY.makeCheckBox();
    this.checkBoxDrawAsImage.setSelected(this.options.isDrawAsImage() || printableObject.isImage());
    this.checkBoxDrawAsImage.setEnabled(!printableObject.isImage());
    this.checkBoxDrawAsImage.setText(this.resourceBundle.getString("MMDPrintPanel.DrawAsImage"));
    this.checkBoxDrawAsImage.addActionListener(e -> {
      options.setDrawAsImage(checkBoxDrawAsImage.isSelected());
      splitToPagesForCurrentFormat();
      scrollPane.revalidate();
      scrollPane.getViewport().revalidate();
      scrollPane.repaint();
    });
    toolBar.add(this.checkBoxDrawAsImage);

    this.add(toolBar, BorderLayout.NORTH);

    this.pageFormat = printerJob.defaultPage();
    this.splitToPagesForCurrentFormat();
    this.previewContainer = new Pages(this);
    scrollPane.getViewport().setView(this.previewContainer);

    this.preferredFocusedComponent = scrollPane;

    this.add(scrollPane, BorderLayout.CENTER);

    doLayout();
    super.setPreferredSize(this.theAdaptor.getPreferredSizeOfPanel(this));
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.preferredFocusedComponent;
  }

  PageFormat getPageFormat() {
    return this.pageFormat;
  }

  int countPages() {
    int result = 0;
    for (final PrintPage[] p : this.pages) {
      result += p.length;
    }
    return result;
  }

  PrintPage[][] getPages() {
    return this.pages;
  }

  PrintPage findPageForIndex(final int value) {
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

  double getScale() {
    return this.pageZoomFactor;
  }

  private void splitToPagesForCurrentFormat() {
    final MMDPrint printer = new MMDPrint(this.printableObject, (int) this.pageFormat.getImageableWidth(),
        (int) this.pageFormat.getImageableHeight(), this.options);
    this.pages = printer.getPages();
  }

  boolean isDrawBorder() {
    return this.checkBoxDrawBorder.isSelected();
  }

  boolean isDarkTheme() {
    return this.theAdaptor.isDarkTheme(this);
  }

  public enum IconId {
    PRINTER,
    PAGE
  }

  public interface Adaptor {

    void startBackgroundTask(MMDPrintPanel source, String name, Runnable task);

    boolean isDarkTheme(MMDPrintPanel source);

    void onPrintTaskStarted(MMDPrintPanel source);

    Dimension getPreferredSizeOfPanel(MMDPrintPanel source);
  }
}
