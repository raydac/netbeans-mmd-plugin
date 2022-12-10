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

package com.igormaznitsa.mindmap.annotations;

import org.junit.Assert;
import org.junit.Test;

public class MmdColorTest {

  @Test
  public void testHtmlColors() {
    for (final MmdColor color : MmdColor.values()) {
      final String htmlColor = color.getHtmlColor();
      Assert.assertTrue(color.name(), htmlColor.startsWith("#"));
      Assert.assertEquals(7, htmlColor.length());
      try {
        Integer.parseInt(htmlColor.substring(1), 16);
      } catch (NumberFormatException ex) {
        Assert.fail("Error at color: " + color);
      }
    }
  }

}