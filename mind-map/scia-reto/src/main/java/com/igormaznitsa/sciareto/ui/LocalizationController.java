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

package com.igormaznitsa.sciareto.ui;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class LocalizationController implements SpecificKeys {
  public static final Logger LOGGER = LoggerFactory.getLogger(LocalizationController.class);

  private static final LocalizationController INSTANCE = new LocalizationController();
  private Language currentLanguage;

  private LocalizationController() {
    final Language hostLanguage;
    if (!"ru".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
      hostLanguage = Language.ENGLISH;
    } else {
      hostLanguage = Language.RUSSIAN;
    }

    final String language = PreferencesManager.getInstance().getPreferences()
        .get(PROPERTY_LANGUAGE, hostLanguage.name());
    final Language selectedLanguage =
        Stream.of(Language.values()).filter(x -> x.name().equalsIgnoreCase(language)).findFirst()
            .orElse(hostLanguage);
    this.currentLanguage = selectedLanguage;
    LOGGER.info("Initial set language: " + selectedLanguage.name());
    selectedLanguage.activate();
  }

  @Nonnull
  public static LocalizationController getInstance() {
    return INSTANCE;
  }

  @Nonnull
  public synchronized Language getLanguage() {
    return this.currentLanguage;
  }

  public synchronized void setLanguage(@Nonnull final Language language) {
    if (this.currentLanguage != language) {
      LOGGER.info("Change language to: " + language.name());
      PreferencesManager.getInstance().getPreferences()
          .put(PROPERTY_LANGUAGE, language.name());
      this.currentLanguage = language;
      this.currentLanguage.activate();
    }
  }

  public enum Language {
    ENGLISH("English", Locale.ENGLISH, e -> {
      updateUiForBundle(SrI18n.getInstance().findBundle(e.locale));
    }),
    RUSSIAN("Русский", new Locale("ru"), e -> {
      updateUiForBundle(SrI18n.getInstance().findBundle(e.locale));
    });

    private static void updateUiForBundle(final ResourceBundle resourceBundle) {
      UIManager.put("OptionPane.okButtonText", resourceBundle.getString("messageDialog.button.ok"));
      UIManager.put("OptionPane.cancelButtonText", resourceBundle.getString("messageDialog.button.cancel"));
      UIManager.put("OptionPane.yesButtonText", resourceBundle.getString("messageDialog.button.yes"));
      UIManager.put("OptionPane.noButtonText", resourceBundle.getString("messageDialog.button.no"));
      
      UIManager.put("FileChooser.openButtonText", resourceBundle.getString("fileChooser.button.open"));
      UIManager.put("FileChooser.saveButtonText", resourceBundle.getString("fileChooser.button.save"));
      UIManager.put("FileChooser.cancelButtonText", resourceBundle.getString("fileChooser.cancelButtonText"));
      UIManager.put("FileChooser.fileNameLabelText",resourceBundle.getString("fileChooser.fileNameLabelText"));
      UIManager.put("FileChooser.filesOfTypeLabelText",resourceBundle.getString("fileChooser.filesOfTypeLabelText"));
      UIManager.put("FileChooser.lookInLabelText", resourceBundle.getString("fileChooser.lookInLabelText"));
      UIManager.put("FileChooser.newFolderButtonText",resourceBundle.getString("fileChooser.newFolderButtonText"));
      UIManager.put("FileChooser.newFolderToolTipText",resourceBundle.getString("fileChooser.newFolderButtonText"));

      UIManager.put("FileChooser.openDialogTitleText", resourceBundle.getString("fileChooser.openDialogTitleText"));
      UIManager.put("FileChooser.openButtonToolTipText", resourceBundle.getString("fileChooser.openButtonToolTipText"));
      UIManager.put("FileChooser.cancelButtonToolTipText", resourceBundle.getString("fileChooser.cancelButtonText"));
      UIManager.put("FileChooser.fileNameHeaderText",  resourceBundle.getString("fileChooser.fileNameHeaderText"));
      UIManager.put("FileChooser.upFolderToolTipText", resourceBundle.getString("fileChooser.upFolderToolTipText"));
      UIManager.put("FileChooser.homeFolderToolTipText", resourceBundle.getString("fileChooser.homeFolderToolTipText"));
      UIManager.put("FileChooser.listViewButtonToolTipText", resourceBundle.getString("fileChooser.listViewButtonToolTipText"));
      UIManager.put("FileChooser.renameFileButtonText", resourceBundle.getString("fileChooser.renameFileButtonText"));
      UIManager.put("FileChooser.deleteFileButtonText", resourceBundle.getString("fileChooser.deleteFileButtonText"));
      UIManager.put("FileChooser.filterLabelText", resourceBundle.getString("fileChooser.filterLabelText"));
      UIManager.put("FileChooser.detailsViewButtonToolTipText", resourceBundle.getString("fileChooser.detailsViewButtonToolTipText"));
      UIManager.put("FileChooser.fileSizeHeaderText", resourceBundle.getString("fileChooser.fileSizeHeaderText"));
      UIManager.put("FileChooser.fileDateHeaderText", resourceBundle.getString("fileChooser.fileDateHeaderText"));
      UIManager.put("FileChooser.acceptAllFileFilterText",resourceBundle.getString("fileChooser.acceptAllFileFilterText"));
    }

    private final String title;
    private final Locale locale;

    private final Consumer<Language> activator;

    Language(@Nonnull final String title, @Nonnull final Locale locale,
             @Nonnull final Consumer<Language> activator) {
      this.title = title;
      this.locale = locale;
      this.activator = activator;
    }

    @Nonnull
    public String getTitle() {
      return this.title;
    }

    @Nonnull
    public Locale getLocale() {
      return this.locale;
    }

    public void activate() {
      if (SwingUtilities.isEventDispatchThread()) {
        this.activator.accept(this);
      } else {
        SwingUtilities.invokeLater(() -> this.activator.accept(this));
      }
    }
  }
}
