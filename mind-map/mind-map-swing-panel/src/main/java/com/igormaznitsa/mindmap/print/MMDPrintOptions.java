/*
 * Copyright 2017 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.print;

import javax.annotation.Nonnull;
import com.igormaznitsa.meta.common.utils.Assertions;

/**
 * Parameters for print.
 *
 * @see MMDPrint
 * @since 1.4.1
 */
public class MMDPrintOptions {
  public enum ScaleType {
    ZOOM,
    FIT_WIDTH_TO_PAGES,
    FIT_HEIGHT_TO_PAGES,
    FIT_TO_SINGLE_PAGE;
  }

  private ScaleType scaleOption = ScaleType.ZOOM;
  private int horzPages = 1;
  private int vertPages = 1;
  private double scale = 1.0d;

  public MMDPrintOptions() {
  }

  public MMDPrintOptions(@Nonnull final MMDPrintOptions that) {
    this.scaleOption = that.scaleOption;
    this.horzPages = that.horzPages;
    this.vertPages = that.vertPages;
    this.scale = that.scale;
  }

  /**
   * Get scale, must not be 0.
   * @return the scale, must be great than zero.
   */
  public double getScale() {
    return this.scale;
  }

  /**
   * Set scale.
   * @param value new scale, must be great than zero.
   * @return this instance
   */
  @Nonnull
  public MMDPrintOptions setScale(final double value) {
    Assertions.assertTrue("Must be >0.0d",value > 0.0d);
    this.scale = value;
    return this;
  }
  
  /**
   * Get number of pages in column.
   *
   * @return max pages, if 0 or less than zero then not defined
   */
  public int getPagesInColumn() {
    return this.vertPages;
  }

  /**
   * Get number of pages in row.
   *
   * @return max pages, if 0 or less than zero then not defined
   */
  public int getPagesInRow() {
    return this.horzPages;
  }

  /**
   * Get selected scale option.
   * @return the selected scale option, must not be null.
   */
  @Nonnull
  public ScaleType getScaleType() {
    return this.scaleOption;
  }
  
  /**
   * Set the selected scale option.
   * @param scaleOption option, must not be null
   * @return this instance
   */
  @Nonnull
  public MMDPrintOptions setScaleType(@Nonnull final ScaleType scaleOption) {
    this.scaleOption = Assertions.assertNotNull(scaleOption);
    return this;
  }
  
  /**
   * Set maximum vertical pages
   *
   * @param pages number of pages in column, must be 1 or great
   * @return this instance
   */
  @Nonnull
  public MMDPrintOptions setPagesInColumn(final int pages) {
    Assertions.assertTrue("Must be >=1", pages >= 1);
    this.vertPages = pages;
    return this;
  }

  /**
   * Set maximum horizontal pages
   *
   * @param pages number of pages in row, must be 1 or great
   * @return this instance
   */
  @Nonnull
  public MMDPrintOptions setPagesInRow(final int pages) {
    Assertions.assertTrue("Must be >=1", pages >= 1);
    this.horzPages = pages;
    return this;
  }
}
