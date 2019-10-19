/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.swing.panel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.Test;

public class UtilsTest {

  @Test
  public void testsUriCorrect() {
    assertTrue(Utils.isUriCorrect("mailto:max@provider.com"));
    assertTrue(Utils.isUriCorrect("http://huzzaa.com/jjj?sdsd=2323&weew=%443"));
    assertFalse(Utils.isUriCorrect("helloworld"));
    assertFalse(Utils.isUriCorrect(":helloworld:"));
    assertFalse(Utils.isUriCorrect("://helloworld:"));
    assertFalse(Utils.isUriCorrect(""));
  }

  @Test
  public void testStrip() {
    assertEquals("", Utils.strip("", true));
    assertEquals("", Utils.strip("", false));
    assertEquals("", Utils.strip("   ", true));
    assertEquals("", Utils.strip("   ", false));
    assertEquals("huz aa", Utils.strip("huz aa", true));
    assertEquals("huz aa", Utils.strip("huz aa", false));
    assertEquals("huz aa", Utils.strip("   huz aa", true));
    assertEquals("   huz aa", Utils.strip("   huz aa", false));
    assertEquals("huz aa   ", Utils.strip("huz aa   ", true));
    assertEquals("huz aa", Utils.strip("huz aa   ", false));
    assertEquals("huz aa   ", Utils.strip("    huz aa   ", true));
    assertEquals("    huz aa", Utils.strip("    huz aa   ", false));
  }

  @Test
  public void testConvertCamelCasedToHumanForm() {
    assertEquals("Hello world and universe", Utils.convertCamelCasedToHumanForm("helloWorldAndUniverse", true));
    assertEquals("hello world and universe", Utils.convertCamelCasedToHumanForm("helloWorldAndUniverse", false));
  }

  @Test
  public void testFindRectEdgeIntersection() {
    final Rectangle2D rect = new Rectangle2D.Double(50, 50, 100, 50);

    assertEquals(new Point2D.Double(100d, 50d), Utils.findRectEdgeIntersection(rect, 100d, 25d));
    assertEquals(new Point2D.Double(100d, 100d), Utils.findRectEdgeIntersection(rect, 100d, 125d));
    assertEquals(new Point2D.Double(50d, 75d), Utils.findRectEdgeIntersection(rect, 10d, 75d));
    assertEquals(new Point2D.Double(150d, 75d), Utils.findRectEdgeIntersection(rect, 200d, 75d));

    final Rectangle2D rect2 = new Rectangle2D.Double(550, 650, 100, 50);
    assertEquals(550d, Utils.findRectEdgeIntersection(rect2, 10d, 640d).getX(), 0.0d);
    assertTrue(rect2.getCenterY() > Utils.findRectEdgeIntersection(rect2, 10d, 640d).getY());
    assertTrue(rect2.getCenterY() < Utils.findRectEdgeIntersection(rect2, 10d, 710d).getY());

    assertEquals(650d, Utils.findRectEdgeIntersection(rect2, 1500d, 640d).getX(), 0.0d);
    assertTrue(rect2.getCenterY() > Utils.findRectEdgeIntersection(rect2, 1500d, 640d).getY());
    assertTrue(rect2.getCenterY() < Utils.findRectEdgeIntersection(rect2, 1500d, 710d).getY());

    assertEquals(650d, Utils.findRectEdgeIntersection(rect2, 590d, 10d).getY(), 0.0d);
    assertEquals(700d, Utils.findRectEdgeIntersection(rect2, 590d, 10000d).getY(), 0.0d);
    assertTrue(rect2.getCenterX() > Utils.findRectEdgeIntersection(rect2, 520d, 10d).getX());
    assertTrue(rect2.getCenterX() < Utils.findRectEdgeIntersection(rect2, 660d, 10d).getX());
  }

}
