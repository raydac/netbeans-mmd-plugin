package com.igormaznitsa.nbmindmap.nb.quicksearch;

import com.igormaznitsa.nbmindmap.nb.swing.AboutPanel;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.spi.quicksearch.*;

public class QuickSearchProvider implements SearchProvider {

  private static class SearchedItem {

    private final String displayName;
    private final Runnable runnable;
    private final Pattern pattern;

    private SearchedItem(final String pattern, final String displayName, final Runnable run) {
      this.displayName = displayName;
      this.runnable = run;
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    public Runnable getRunnable() {
      return this.runnable;
    }

    public String getDisplayName() {
      return this.displayName;
    }

    public boolean isSatisfied(final String searchText) {
      return pattern.matcher(searchText).find();
    }
  }

  private static final List<SearchedItem> ITEMS = Arrays.asList(new SearchedItem(".*mind.*|.*map.*|.*", "Mind Map", new Runnable() {
            @Override
            public void run() {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  NbUtils.plainMessageOk(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MMDCfgPanel.buttonAbout.Text"), new AboutPanel());
                }
              });
            }

          }));

  @Override
  public void evaluate(final SearchRequest request, final SearchResponse response) {
    final String text = request.getText();

    for (final SearchedItem item : ITEMS) {
      if (item.isSatisfied(text)) {
        if (!response.addResult(item.getRunnable(), item.getDisplayName())) {
          break;
        }
      }
    }

  }

}
