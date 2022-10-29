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

package com.igormaznitsa.mindmap.print;

import static java.util.Objects.requireNonNull;

/**
 * Parameters for print.
 *
 * @see MMDPrint
 * @since 1.4.1
 */
public class MMDPrintOptions {

  private ScaleType scaleOption = ScaleType.ZOOM;
  private int horzPages = 1;
  private int vertPages = 1;
  private double scale = 1.0d;
  private boolean drawAsImage = false;

  public MMDPrintOptions() {
  }

  public MMDPrintOptions(final MMDPrintOptions that) {
    this.scaleOption = that.scaleOption;
    this.horzPages = that.horzPages;
    this.vertPages = that.vertPages;
    this.scale = that.scale;
    this.drawAsImage = that.drawAsImage;
  }

  /**
   * Should be printed as image instead of direct drawing.
   *
   * @return true if to print as image, false otherwise
   */
  public boolean isDrawAsImage() {
    return this.drawAsImage;
  }

  /**
   * Set flag to print as image instead of direct drawing.
   *
   * @param flag true if to print as image, false otherwise
   * @return this instance
   */
  public MMDPrintOptions setDrawAsImage(final boolean flag) {
    this.drawAsImage = flag;
    return this;
  }

  /**
   * Get scale, must not be 0.
   *
   * @return the scale, must be great than zero.
   */
  public double getScale() {
    return this.scale;
  }

  /**
   * Set scale.
   *
   * @param value new scale, must be great than zero.
   * @return this instance
   */
  public MMDPrintOptions setScale(final double value) {
    if (value <= 0.0d) {
      throw new IllegalArgumentException("Must be >0.0d");
    }
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
   * Set maximum vertical pages
   *
   * @param pages number of pages in column, must be 1 or great
   * @return this instance
   */
  public MMDPrintOptions setPagesInColumn(final int pages) {
    if (pages < 1) {
      throw new IllegalArgumentException("Must contain pages");
    }
    this.vertPages = pages;
    return this;
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
   * Set maximum horizontal pages
   *
   * @param pages number of pages in row, must be 1 or great
   * @return this instance
   */
  public MMDPrintOptions setPagesInRow(final int pages) {
    if (pages < 1) {
      throw new IllegalArgumentException("Must contain pages");
    }
    this.horzPages = pages;
    return this;
  }

  /**
   * Get selected scale option.
   *
   * @return the selected scale option, must not be null.
   */
  public ScaleType getScaleType() {
    return this.scaleOption;
  }

  /**
   * Set the selected scale option.
   *
   * @param scaleOption option, must not be null
   * @return this instance
   */
  public MMDPrintOptions setScaleType(final ScaleType scaleOption) {
    this.scaleOption = requireNonNull(scaleOption);
    return this;
  }

  public enum ScaleType {
    ZOOM,
    FIT_WIDTH_TO_PAGES,
    FIT_HEIGHT_TO_PAGES,
    FIT_TO_SINGLE_PAGE
  }
}
