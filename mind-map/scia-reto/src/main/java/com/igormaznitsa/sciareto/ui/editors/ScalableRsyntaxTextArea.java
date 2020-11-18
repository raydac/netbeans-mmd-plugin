/*
 * Copyright (C) 2019 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import javax.annotation.Nonnull;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public final class ScalableRsyntaxTextArea extends RSyntaxTextArea {
  
  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
  
  private float fontScale = 1.0f;
  private float fontOriginalSize;
  
  private static final float SCALE_STEP = 0.5f;
  private static final float SCALE_MIN = 0.03f;
  private static final float SCALE_MAX = 10.0f;

  private MindMapPanelConfig config;

  public ScalableRsyntaxTextArea(@Nonnull final MindMapPanelConfig mmConfig) {
    super();

    this.config = mmConfig;

    this.setFont(PreferencesManager.getInstance()
            .getFont(PreferencesManager.getInstance().getPreferences(),
                    SpecificKeys.PROPERTY_TEXT_EDITOR_FONT,
                    DEFAULT_FONT));
    this.fontOriginalSize = this.getFont().getSize2D();
    
    this.addMouseWheelListener((@Nonnull final MouseWheelEvent e) -> {
      if (!e.isConsumed() && ((e.getModifiers() & this.config.getScaleModifiers()) == this.config.getScaleModifiers())) {
        e.consume();
        this.fontScale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, this.fontScale + SCALE_STEP * -e.getWheelRotation()));
        updateFontForScale();
      } else {
        this.getParent().dispatchEvent(e);
      }
    });
    
    updateFontForScale();
  }
  
  public void doZoomIn() {
    this.fontScale = Math.min(SCALE_MAX, this.fontScale + SCALE_STEP);
    updateFontForScale();
  }
  
  public void doZoomOut() {
    this.fontScale = Math.max(SCALE_MIN, this.fontScale - SCALE_STEP);
    updateFontForScale();
  }
  
  public void doZoomReset() {
    this.fontScale = 1.0f;
    updateFontForScale();
  }
  
  private void updateFontForScale() {
    final Font newFont = this.getFont().deriveFont(this.fontScale * this.fontOriginalSize);
    if (newFont.getSize() > 0) {
      this.setFont(newFont);
    } else {
      this.setFont(this.getFont().deriveFont(1.0f));
    }
  }
  
  public void updateConfig(@Nonnull final MindMapPanelConfig mmConfig) {
    this.config = mmConfig;
    this.setFont(PreferencesManager.getInstance().getFont(PreferencesManager.getInstance().getPreferences(), SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, DEFAULT_FONT));
    this.fontOriginalSize = this.getFont().getSize2D();
    updateFontForScale();
    this.revalidate();
    this.repaint();
  }
  
}
