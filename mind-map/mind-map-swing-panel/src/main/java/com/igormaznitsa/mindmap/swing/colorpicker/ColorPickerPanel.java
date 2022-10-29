/*
 * Copyright 2019 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.swing.colorpicker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

final class ColorPickerPanel {

  private final List<Color> predefinedColors;
  private final List<ColorListener> colorListeners = new ArrayList<>();
  private final JPanel panel;
  private int rows = 8;
  private int cols = 8;
  private int selectedRow = -1;
  private int selectedCol = -1;
  private int cellWidth = 24;
  private int cellHeight = 24;
  private int gapHorz = 0;
  private int gapVert = 0;
  private boolean readOnly = false;

  public ColorPickerPanel(
      final JPanel panel,
      final int rows,
      final int cols,
      final int gapHorz,
      final int gapVert,
      final List<Color> predefinedColors
  ) {
    this.panel = panel;
    this.panel.setOpaque(false);

    this.gapHorz = Math.max(0, gapHorz);
    this.gapVert = Math.max(0, gapVert);
    this.predefinedColors = predefinedColors;

    this.rows = Math.min(32, Math.max(1, rows));
    this.cols = Math.min(32, Math.max(1, cols));

    updateColorButtons(this.predefinedColors);
  }

  public void setGaps(final int horz, final int vert) {
    this.gapHorz = Math.max(0, horz);
    this.gapVert = Math.max(0, vert);
    updateColorButtons(this.predefinedColors);
  }

  protected void fireColorSelected(final Color color) {
    for (final ColorListener listener : this.colorListeners) {
      listener.onColorSelected(this, color);
    }
  }

  public void addColorListener(final ColorListener listener) {
    this.colorListeners.add(listener);
  }

  public void removeColorListener(final ColorListener listener) {
    this.colorListeners.remove(listener);
  }

  public void resetSelected() {
    for (final Component c : this.panel.getComponents()) {
      if (c instanceof RadioColorButton) {
        final RadioColorButton button = (RadioColorButton) c;
        if (button.isSelected()) {
          button.setSelected(false, false);
        }
      }
    }
    this.panel.repaint();
  }

  public Color getColor() {
    for (final Component c : this.panel.getComponents()) {
      if (c instanceof RadioColorButton) {
        final RadioColorButton button = (RadioColorButton) c;
        if (button.isSelected()) {
          return button.getBackground();
        }
      }
    }
    return null;
  }

  public void setColor(final Color color) {
    if (color == null) {
      this.resetSelected();
    } else {
      for (final Component c : this.panel.getComponents()) {
        if (c instanceof RadioColorButton) {
          final RadioColorButton button = (RadioColorButton) c;
          if (color.equals(button.getBackground())) {
            button.setSelected(true, true);
            break;
          }
        }
      }
    }
  }

  public boolean isReadOnly() {
    return this.readOnly;
  }

  public void setReadOnly(final boolean value) {
    this.readOnly = value;
    if (this.readOnly) {
      this.selectedRow = -1;
      this.selectedCol = -1;
    }
    this.panel.repaint();
  }

  public int getSelectedRow() {
    return this.selectedRow;
  }

  public void setSelectedRow(final int row) {
    if (!this.readOnly) {
      this.selectedRow = row < 0 ? -1 : (row >= this.rows ? this.rows - 1 : row);
      updateColorButtons(this.predefinedColors);
    }
  }

  public int getSelectedCol() {
    return this.selectedCol;
  }

  public void setSelectedCol(final int col) {
    if (!this.readOnly) {
      this.selectedCol = col < 0 ? -1 : (col >= this.cols ? this.cols - 1 : col);
      updateColorButtons(this.predefinedColors);
    }
  }

  public int getRows() {
    return this.rows;
  }

  public void setRows(final int rows) {
    this.rows = Math.min(32, Math.max(1, rows));
    updateColorButtons(this.predefinedColors);
  }

  public int getCols() {
    return this.cols;
  }

  public void setCols(final int cols) {
    this.cols = Math.min(32, Math.max(1, cols));
    updateColorButtons(this.predefinedColors);
  }

  public int getCellWidth() {
    return this.cellWidth;
  }

  public void setCellWidth(final int width) {
    this.cellWidth = Math.max(4, Math.min(width, 64));
    updateColorButtons(this.predefinedColors);
  }

  public int getCellHeight() {
    return this.cellHeight;
  }

  public void setCellHeight(final int height) {
    this.cellHeight = Math.max(4, Math.min(height, 64));
    updateColorButtons(this.predefinedColors);
  }

  public JPanel getPanel() {
    return this.panel;
  }

  private int ave(int s, int d, float p) {
    return s + java.lang.Math.round(p * (d - s));
  }

  private void updateColorButtons(final List<Color> predefinedColors) {
    this.panel.removeAll();
    this.panel.setLayout(new GridLayout(this.rows, this.cols, this.gapHorz, this.gapVert));

    final int totalCells = this.rows * this.cols;

    if (predefinedColors == null) {
      final float step = 1.0f / Math.max(1, totalCells - 13);

      float hue = 0.0f;

      for (int i = 0; i < totalCells; i++) {
        final RadioColorButton button;
        switch (i) {
          case 0:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.BLACK, false);
            break;
          case 1:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.DARK_GRAY, false);
            break;
          case 2:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.GRAY, false);
            break;
          case 3:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.LIGHT_GRAY, false);
            break;
          case 4:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.WHITE, false);
            break;
          case 5:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.BLUE, false);
            break;
          case 6:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.RED, false);
            break;
          case 7:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.MAGENTA, false);
            break;
          case 8:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.PINK, false);
            break;
          case 9:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.GREEN, false);
            break;
          case 10:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.CYAN, false);
            break;
          case 11:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.YELLOW, false);
            break;
          case 12:
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.ORANGE, false);
            break;
          default: {
            button = new RadioColorButton(this, this.cellWidth, this.cellHeight,
                Color.getHSBColor(hue, i * step, 0.8f),
                false);
            hue += step;
          }
          break;
        }
        this.panel.add(button);
      }
    } else {
      for (int i = 0; i < totalCells; i++) {
        final RadioColorButton button;
        if (i < predefinedColors.size()) {
          button = new RadioColorButton(this, this.cellWidth, this.cellHeight, predefinedColors.get(i), false);
        } else {
          button = new RadioColorButton(this, this.cellWidth, this.cellHeight, Color.LIGHT_GRAY, false);
        }
        this.panel.add(button);
      }
    }
    this.panel.revalidate();
    this.panel.doLayout();
  }

  public interface ColorListener {

    void onColorSelected(ColorPickerPanel source, Color color);
  }

  private static final class RadioColorButton extends JComponent {

    private final ColorPickerPanel parent;
    private boolean selected;

    RadioColorButton(
        final ColorPickerPanel parent,
        final int cellWidth,
        final int cellHeight,
        final Color color,
        final boolean selected
    ) {
      super();
      this.parent = parent;
      this.setBackground(color);
      this.setOpaque(true);

      final Dimension size = new Dimension(cellWidth, cellHeight);
      this.setSize(size);
      this.setPreferredSize(size);
      this.setMinimumSize(size);

      this.setBorder(selected ? BorderFactory.createLineBorder(getContrastColor(this.getBackground()), Math.min(this.getWidth() / 3, 4)) : BorderFactory.createEtchedBorder());

      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
          if (!e.isConsumed() && !parent.isReadOnly()) {
            setSelected(true, true);
          }
        }
      });

      this.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(final KeyEvent e) {
          if (!e.isConsumed() &&
              (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE)) {
            e.consume();
            setSelected(true, !isSelected());
          }
        }

      });

      final String red = Integer.toHexString(color.getRed()).toUpperCase(Locale.ENGLISH);
      final String green = Integer.toHexString(color.getGreen()).toUpperCase(Locale.ENGLISH);
      final String blue = Integer.toHexString(color.getBlue()).toUpperCase(Locale.ENGLISH);

      String buffer = '#' +
          (red.length() < 2 ? "0" : "") + red +
          (green.length() < 2 ? "0" : "") + green +
          (blue.length() < 2 ? "0" : "") + blue;
      this.setToolTipText(buffer);
    }

    public static Color getContrastColor(final Color color) {
      double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
      return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    @Override
    public boolean isFocusable() {
      return true;
    }

    @Override
    public void paintComponent(final Graphics gfx) {
      gfx.setColor(this.getBackground());
      gfx.fill3DRect(0, 0, this.getWidth(), this.getHeight(), true);
    }

    public void setSelected(final boolean removeCurrentSelection, final boolean selected) {
      if (this.selected != selected || this.getBorder() == null) {
        if (removeCurrentSelection) {
          this.parent.resetSelected();
        }

        this.selected = selected;
        this.setBorder(selected ? BorderFactory.createLineBorder(getContrastColor(this.getBackground()), Math.min(this.getWidth() / 3, 4)) : BorderFactory.createEtchedBorder());
        this.revalidate();
        this.repaint();

        if (selected) {
          this.parent.fireColorSelected(this.getBackground());
        }
      }
    }

    public boolean isSelected() {
      return this.selected;
    }
  }

}
