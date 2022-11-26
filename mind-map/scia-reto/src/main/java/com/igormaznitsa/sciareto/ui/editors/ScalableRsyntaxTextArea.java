/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.swing.event.UndoableEditEvent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RUndoManager;

public final class ScalableRsyntaxTextArea extends RSyntaxTextArea {

    public static final Font DEFAULT_FONT = MindMapPanelConfig.findDefaultFont(Font.PLAIN, 14);
    private static final float SCALE_STEP = 0.5f;
    private static final float SCALE_MIN = 0.03f;
    private static final float SCALE_MAX = 10.0f;
    private static final Theme THEME_DARK = loadTheme("dark.xml");
    private static final Theme THEME_LIGHT = loadTheme("default.xml");
    private float fontScale = 1.0f;
    private float fontOriginalSize;
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

        if (UiUtils.figureOutThatDarkTheme()) {
            THEME_DARK.apply(this);
        } else {
            THEME_LIGHT.apply(this);
        }

        updateFontForScale();
    }

    @Nonnull
    private static Theme loadTheme(@Nonnull final String name) {
      try (final InputStream in = ScalableRsyntaxTextArea.class.getResourceAsStream(
          "/org/fife/ui/rsyntaxtextarea/themes/" + name)) {
        return Theme.load(in, DEFAULT_FONT);
      } catch (Exception ex) {
        throw new Error("Can't load theme: " + name);
      }
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

    @Nonnull
    @Override
    protected RUndoManager createUndoManager() {
        return new RUndoManager(this) {
            @Override
            public void undoableEditHappened(@Nonnull final UndoableEditEvent e) {
                this.addEdit(e.getEdit());
                this.updateActions();
            }

        };
    }

}
