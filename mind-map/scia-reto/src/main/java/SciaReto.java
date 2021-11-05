
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

import javax.annotation.Nonnull;

public final class SciaReto {

    private SciaReto() {
    }

    public static void main(@Nonnull @MustNotContainNull final String... args) {
        final int guiScaleFactor = PreferencesManager.getInstance().getPreferences().getInt(SciaRetoStarter.PROPERTY_SCALE_GUI, -1);
        System.out.println("UI scale factor: " + guiScaleFactor);
        if (guiScaleFactor > 0) {
                System.setProperty("sun.java2d.uiScale", Integer.toString(Math.max(1, Math.min(5, guiScaleFactor))));
                System.setProperty("sun.java2d.dpiaware", "true");
        }

        SciaRetoStarter.main(args);
    }
}
