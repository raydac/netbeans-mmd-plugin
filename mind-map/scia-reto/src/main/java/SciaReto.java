import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.SciaRetoStarter;

import javax.annotation.Nonnull;

public final class SciaReto {

  private SciaReto() {
  }

  public static void main(@Nonnull @MustNotContainNull final String... args) {
    SciaRetoStarter.main(args);
  }
}
