
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SciaReto {

    private SciaReto() {
    }

    /**
     * Copy of similar method in UiUtils because it should be called before Swing activation.
     * @return detected value as string, can be null
     */
    @Nullable
    private static String readUiScaleFactor() {
        String result = PreferencesManager.getInstance().getPreferences().get(SciaRetoStarter.PROPERTY_SCALE_GUI, null);
        if (result != null && !result.matches("\\s*[1-5](?:\\.5)?\\s*")) {
            result = null;
        }
        return result == null ? result : result.trim();
    }

    public static void main(@Nonnull @MustNotContainNull final String... args) {
        final String guiScaleFactor = readUiScaleFactor();
        if (guiScaleFactor != null) {
            System.out.println("UI scale factor: " + guiScaleFactor);
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("sun.java2d.uiScale", guiScaleFactor);
        } else {
            System.out.println("UI scale factor is not set");
        }

        SciaRetoStarter.main(args);
    }
}
