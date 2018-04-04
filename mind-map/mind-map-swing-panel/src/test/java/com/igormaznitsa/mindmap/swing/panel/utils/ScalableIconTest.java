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

import java.awt.Image;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScalableIconTest {
  
  @Test
  public void testIcons() {
    assertNotNull(ScalableIcon.FILE.getImage(1.0f));
    assertNotNull(ScalableIcon.FILE_WARN.getImage(1.0f));
    assertNotNull(ScalableIcon.LINK.getImage(1.0f));
    assertNotNull(ScalableIcon.LINK_EMAIL.getImage(1.0f));
    assertNotNull(ScalableIcon.TOPIC.getImage(1.0f));
    assertNotNull(ScalableIcon.TEXT.getImage(1.0f));
    assertNotNull(ScalableIcon.FILE_MMD.getImage(1.0f));
    assertNotNull(ScalableIcon.FILE_MMD_WARN.getImage(1.0f));
  }

  @Test
  public void testIconScale_1x() {
    final Image img = ScalableIcon.FILE.getImage(1.0f);
    assertEquals(16, img.getWidth(null));
    assertEquals(16, img.getHeight(null));
  }

  @Test
  public void testIconScale_2x() {
    final Image img = ScalableIcon.FILE.getImage(2.0f);
    assertEquals(32, img.getWidth(null));
    assertEquals(32, img.getHeight(null));
  }

  @Test
  public void testIconScale_3x() {
    final Image img = ScalableIcon.FILE.getImage(3.0f);
    assertEquals(48, img.getWidth(null));
    assertEquals(48, img.getHeight(null));
  }
  
}
