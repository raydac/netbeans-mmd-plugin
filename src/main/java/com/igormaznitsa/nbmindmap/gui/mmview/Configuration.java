/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.gui.mmview;

import com.igormaznitsa.nbmindmap.gui.MindMapPanel;
import java.awt.Color;
import java.awt.Font;

public class Configuration {
  private int collapsatorSize = 16;
  private int textMargins = 10;
  private int otherLevelVerticalInset = 16;
  private int otherLevelHorizontalInset = 32;
  private int firstLevelVerticalInset = 32;
  private int firstLevelHorizontalInset = 48;
  private int paperMargins = 20;
  private int selectLineGap = 5;
  
  private boolean drawBackground = true;
  private Color paperColor = new Color(0x617B94);
  private Color gridColor = paperColor.darker();
  private boolean showGrid = true;
  private int gridStep = 32;
  private Color rootBackgroundColor = new Color(0x031A31);
  private Color firstLevelBackgroundColor = new Color(0xB1BFCC);
  private Color otherLevelBackgroundColor = new Color(0xFDFDFD);
  private Color rootTextColor = Color.WHITE;
  private Color firstLevelTextColor = Color.BLACK;
  private Color otherLevelTextColor = Color.BLACK;
  private Color elementBorderColor = Color.BLACK;
  private Color connectorColor = Color.WHITE;
  private Color shadowColor = new Color(0x30000000, true);
  private Color collapsatorBorderColor = Color.DARK_GRAY;
  private Color collapsatorBackgroundColor = Color.WHITE;
  private Color selectLineColor = Color.ORANGE;

  private float elementBorderWidth = 1.0f;
  private float collapsatorBorderWidth = 1.0f;
  private float connectorWidth = 1.5f;
  private float selectLineWidth = 2.0f;

  private Font font = new Font("Arial", Font.BOLD, 14);
  private float scale = 1.0f;
  private boolean dropShadow = true;

  private final MindMapPanel panel;
  
  public Configuration(final MindMapPanel panel){
    this.panel = panel;
  }
  
  public MindMapPanel getPanel(){
    return this.panel;
  }

  public float getSelectLineWidth(){
    return this.selectLineWidth;
  }
  
  public void setSelectLineWidth(final float f){
    this.selectLineWidth = f;
  }
  
  public Color getSelectLineColor(){
    return this.selectLineColor;
  }
  
  public void setSelectLineColor(final Color color){
    this.selectLineColor = color;
  }
  
  public void setPaperMargins(final int size){
    this.paperMargins = size;
  }
  
  public int getPaperMargins(){
    return this.paperMargins;
  }
  
  public boolean isDrawBackground(){
    return this.drawBackground;
  }

  public void setDrawBackground(final boolean flag){
    this.drawBackground = flag;
  }
  
  public void setOtherLevelVerticalInset(final int value) {
    this.otherLevelVerticalInset = value;
  }

  public int getOtherLevelVerticalInset() {
    return this.otherLevelVerticalInset;
  }
  
  public void setOtherLevelHorizontalInset(final int value) {
    this.otherLevelHorizontalInset = value;
  }
  
  public int getOtherLevelHorizontalInset(){
    return this.otherLevelHorizontalInset;
  }
  
  public void setFirstLevelVerticalInset(final int value) {
    this.firstLevelVerticalInset = value;
  }

  public int getFirstLevelVerticalInset() {
    return this.firstLevelVerticalInset;
  }
  
  public void setFirstLevelHorizontalInset(final int value) {
    this.firstLevelHorizontalInset = value;
  }
  
  public int getFirstLevelHorizontalInset(){
    return this.firstLevelHorizontalInset;
  }
  
  public Color getPaperColor(){
    return this.paperColor;
  }
  
  public void setPaperColor(final Color color){
    this.paperColor = color;
  }
  
  public void setGridColor(final Color color){
    this.gridColor = color;
  }
  
  public Color getGridColor(){
    return this.gridColor;
  }
  
  public void setShowGrid(final boolean flag){
    this.showGrid = flag;
  }
  
  public boolean isShowGrid(){
    return this.showGrid;
  }
  
  public void setGridStep(final int step){
    this.gridStep = step;
  }
  
  public int getGridStep(){
    return this.gridStep;
  }
  
  public void setRootBackgroundColor(final Color color){
    this.rootBackgroundColor = color;
  }
  
  public Color getRootBackgroundColor(){
    return this.rootBackgroundColor;
  }
  
  public Color getFirstLevelBacgroundColor(){
    return this.firstLevelBackgroundColor;
  }
  
  public void setFirstLevelBackgroundColor(final Color color){
    this.firstLevelBackgroundColor = color;
  }
  
  public void setOtherLevelBackgroundColor(final Color color){
    this.otherLevelBackgroundColor = color;
  }
  
  public Color getOtherLevelBackgroundColor(){
    return this.otherLevelBackgroundColor;
  }
  
  public Color getRootTextColor(){
    return this.rootTextColor;
  }
  
  public void setRootTextColor(final Color color){
    this.rootTextColor = color;
  }
  
  public void setFirstLevelTextColor(final Color color){
    this.firstLevelTextColor = color;
  }
  
  public Color getFirstLevelTextColor(){
    return this.firstLevelTextColor;
  }
  
  public Color getOtherLeveltextColor(){
    return this.otherLevelTextColor;
  }
  
  public void setOtherLevelTextColor(final Color color){
    this.otherLevelTextColor =color; 
  }
  
  public Color getElementBorderColor(){
    return this.elementBorderColor;
  }
  
  public void setElementBorderColor(final Color color){
    this.elementBorderColor = color;
  }
  
  public void setConnectorColor(final Color color){
    this.connectorColor = color;
  }
  
  public Color getConnectorColor(){
    return this.connectorColor;
  }
  
  public void setShadowColor(final Color color){
    this.shadowColor = color;
  }
  
  public Color getShadowColor(){
    return this.shadowColor;
  }
  
  public Color getCollapsatorBorderColor(){
    return this.collapsatorBorderColor;
  }
  
  public void setCollapsatorBorderColor(final Color color){
    this.collapsatorBorderColor = color;
  }
  
  public Color getCollapsatorBackgroundColor(){
    return this.collapsatorBackgroundColor;
  }
  
  public void setCollapsatorBackgroundColor(final Color color){
    this.collapsatorBackgroundColor = color;
  }
  
  public void setElementBorderWidth(final float value){
    this.elementBorderWidth = value;
  }
  
  public float getElementBorderWidth(){
    return this.elementBorderWidth;
  }
  
  public float getCollapsatorBorderWidth(){
    return this.collapsatorBorderWidth;
  }
  
  public void setCollapsatorBorderWidth(final float width){
    this.collapsatorBorderWidth = width;
  }
  
  public float getConnectorWidth(){
    return this.connectorWidth;
  }
  
  public void setConnectorWidth(final float value){
    this.connectorWidth = value;
  }
  
  public void setFont(final Font f){
    this.font = f;
  }
  
  public Font getFont(){
    return this.font;
  }
  
  public float getScale(){
    return this.scale;
  }
  
  public void setScale(final float value){
    this.scale = Math.max(0.3f, Math.min(15f,value));
  }
  
  public boolean isDropShadow(){
    return this.dropShadow;
  }
  
  public void setDropShadow(final boolean value){
    this.dropShadow = value;
  }
  
  public int getCollapsatorSize(){
    return this.collapsatorSize;
  }
  
  public void setCollapsatorSize(final int size){
    this.collapsatorSize = size;
  }
  
  public int getTextMargins(){
    return this.textMargins;
  }
  
  public void setTextMargins(final int value){
    this.textMargins = value;
  }

  public int getSelectLineGap() {
    return this.selectLineGap;
  }
  
  public void setSelectLineGap(final int value) {
    this.selectLineGap = value;
  }
  
  
}
