/*
 * Copyright 2023 Igor Maznitsa.
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
package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.ideamindmap.swing.AboutForm;
import com.igormaznitsa.ideamindmap.swing.DonateButton;
import com.igormaznitsa.mindmap.ide.commons.preferences.AbstractPreferencesPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.openapi.util.IconLoader;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;

public class PreferencesPanel extends AbstractPreferencesPanel {

    private final MindMapSettingsComponent settingsComponent;
    
    public PreferencesPanel(
            final MindMapSettingsComponent settingsComponent,
            final UIComponentFactory uiComponentFactory, 
            final DialogProvider dialogProvider) {
        super(uiComponentFactory, dialogProvider);
        this.settingsComponent = settingsComponent;
        this.getPanel().setPreferredSize(new Dimension(300,300));
    }

    @Override
    protected JButton processColorButton(final JButton button) {
        button.setMargin(new Insets(3,8,3,0));
        return button;
    }

    @Override
    protected void beforePanelsCreate(final UIComponentFactory uiComponentFactory) {
        
    }

    @Override
    protected int getButtonsAlign() {
        return JButton.CENTER;
    }
    
    private Image loadImage(final String name) {
        try {
            final Icon icon = IconLoader.getIcon("/icons/" + name, this.getClass());
            final BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D gfx = image.createGraphics();
            try {
                icon.paintIcon(this.getPanel(), gfx, 0, 0);
            }finally {
                gfx.dispose();
            }
            return image;
        } catch (Exception ex) {
            LOGGER.error("Error during load image: " + name, ex);
            return null;
        }
    }

    
    @Override
    public List<ButtonInfo> findButtonInfo(final UIComponentFactory uiComponentFactory, final DialogProvider dialogProvider) {
        final List<ButtonInfo> result = new ArrayList<>();
        
        result.add(ButtonInfo.from(loadImage("info.png"), "About", e -> AboutForm.show(this.getPanel())));
        result.add(ButtonInfo.from(null, null, null, null, () -> new DonateButton()));
        result.add(ButtonInfo.splitter());

        result.add(ButtonInfo.from(loadImage("document_import.png"), "Import", e -> this.importFromFileDialog(()-> this.getPanel())));
        result.add(ButtonInfo.from(loadImage("document_export.png"), "Export", e -> this.exportAsFileDialog(()-> this.getPanel())));
        result.add(ButtonInfo.from(loadImage("stop.png"), "Default", e -> this.resetToDefault()));
        
        return result;
    }

    private void resetToDefault() {
        this.load(new MindMapPanelConfig());
    }
    
    @Override
    public List<JComponent> findMiscComponents(final UIComponentFactory componentFactory) {
        return Collections.emptyList();
    }

    @Override
    public List<JComponent> findFeaturesComponents(final UIComponentFactory componentFactory) {
        return Collections.emptyList();
    }

    @Override
    public void onSave(final MindMapPanelConfig config) {
    }

    @Override
    public void onLoad(final MindMapPanelConfig config) {
    }
}
