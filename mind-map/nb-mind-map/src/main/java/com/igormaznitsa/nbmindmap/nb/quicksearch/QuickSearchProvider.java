/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                  NbUtils.plainMessageOk(null, java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MMDCfgPanel.buttonAbout.Text"), new AboutPanel());
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
