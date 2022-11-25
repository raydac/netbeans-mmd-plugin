
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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PrefUtils;

import static com.igormaznitsa.sciareto.preferences.PrefUtils.ENV_PLANTUML_LIMIT_SIZE;
import static com.igormaznitsa.sciareto.preferences.PrefUtils.ENV_PLANTUML_SECURITY_PROFILE;

import com.igormaznitsa.sciareto.preferences.PreferencesManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SciaReto {

  private SciaReto() {
  }

  /**
   * Copy of similar method in UiUtils because it should be called before Swing activation.
   *
   * @return detected value as string, can be null
   */
  @Nullable
  private static String readUiScaleFactor() {
    String result = PreferencesManager.getInstance().getPreferences()
        .get(SciaRetoStarter.PROPERTY_SCALE_GUI, null);
    if (result != null && !result.matches("\\s*[1-5](?:\\.5)?\\s*")) {
      result = null;
    }
    return result == null ? result : result.trim();
  }

  private static void processUiScale() {
    final String guiScaleFactor = readUiScaleFactor();
    if (guiScaleFactor != null) {
      System.out.println("UI scale factor: " + guiScaleFactor);
      System.setProperty("sun.java2d.uiScale.enabled", "true");
      System.setProperty("sun.java2d.uiScale", guiScaleFactor);
    } else {
      System.out.println("UI scale factor is not set");
    }
  }

  public static void main(@Nonnull @MustNotContainNull final String... args) {
    if (System.getProperty(ENV_PLANTUML_LIMIT_SIZE) == null) {
      System.setProperty(ENV_PLANTUML_LIMIT_SIZE, Integer.toString(32768));
    }

    if (System.getProperty(ENV_PLANTUML_SECURITY_PROFILE) == null) {
      PrefUtils.setPlantUmlSecurityProfileAsSystemProperty();
    }

    final String presentedScale = System.getProperty("sun.java2d.uiScale", null);
    if (presentedScale == null) {
      processUiScale();
    } else {
      System.out.println(
          "Detected presented sun.java2d.uiScale among System properties: " + presentedScale);
    }

    SciaRetoStarter.main(args);
  }
}
