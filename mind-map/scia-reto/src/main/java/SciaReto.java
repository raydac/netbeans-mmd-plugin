
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
