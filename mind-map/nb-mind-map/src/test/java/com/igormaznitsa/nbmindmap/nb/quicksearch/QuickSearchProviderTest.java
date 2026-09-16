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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuickSearchProviderTest {

  private final QuickSearchProvider provider = new QuickSearchProvider();

  @Test
  public void testMatchesMindAndMapTerms() {
    assertTrue(this.provider.matchesQuery("mind"));
    assertTrue(this.provider.matchesQuery("MAP"));
    assertTrue(this.provider.matchesQuery("mind map"));
    assertTrue(this.provider.matchesQuery("something about maps"));
  }

  @Test
  public void testIgnoresUnrelatedQueries() {
    assertFalse(this.provider.matchesQuery(""));
    assertFalse(this.provider.matchesQuery("java"));
    assertFalse(this.provider.matchesQuery("refactor"));
  }
}
