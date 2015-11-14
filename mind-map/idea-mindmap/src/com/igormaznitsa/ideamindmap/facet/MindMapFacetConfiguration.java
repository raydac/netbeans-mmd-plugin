package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.util.List;

public class MindMapFacetConfiguration implements FacetConfiguration {

  private final InMemoryPreferenceNode preferences = new InMemoryPreferenceNode();

  private static final String KEY_USE_INSIDE_BROWSER = "useInsideBrowser";
  private static final String KEY_MAKE_RELATIVE_PATH = "makeRelativePath";
  private static final String KEY_COPY_COLOR_INFO_FROM_PARENT= "copyParentColorInfoInNew";
  private static final String KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET= "unfoldCollapsedTopicInDrop";

  public MindMapFacetConfiguration() {
  }

  @Override public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[] { new MindMapFacetEditorTab(this) };
  }

  @Override public void readExternal(final Element element) throws InvalidDataException {
    final List<Element> elements = element.getChildren();
    try {
      this.preferences.clear();
      for (final Element e : elements) {
        this.preferences.put(e.getName(), e.getText());
      }
    }
    catch (Exception ex) {
      throw new InvalidDataException("Can't read preferences",ex);
    }
  }

  @Override public void writeExternal(final Element element) throws WriteExternalException {
    try {
      for (final String key : this.preferences.keys()) {
        Element el = element.getChild(key);
        if (el == null){
          el = new Element(key);
          element.addContent(el);
        }
        el.setText(this.preferences.get(key,null));
      }
    }catch(Exception ex){
      throw new WriteExternalException("Can't write preferences",ex);
    }
  }

  public boolean isUseInsideBrowser() {
    return this.preferences.getBoolean(KEY_USE_INSIDE_BROWSER,false);
  }

  public boolean isCopyColorInformationFromParent() {
    return this.preferences.getBoolean(KEY_COPY_COLOR_INFO_FROM_PARENT,true);
  }

  public void setCopyColorInformationFromParent(final boolean flag) {
    this.preferences.putBoolean(KEY_COPY_COLOR_INFO_FROM_PARENT,flag);
  }

  public boolean isUnfoldTopicWhenItIsDropTarget() {
    return this.preferences.getBoolean(KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET,true);
  }

  public void setUnfoldTopicWhenItIsDropTarget(final boolean flag) {
    this.preferences.putBoolean(KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET,flag);
  }

  public void setUseInsideBrowser(final boolean flag) {
    this.preferences.putBoolean(KEY_USE_INSIDE_BROWSER,flag);
  }

  public boolean isMakeRelativePath() {
    return this.preferences.getBoolean(KEY_MAKE_RELATIVE_PATH,true);
  }

  public void setMakeRelativePath(final boolean flag) {
    this.preferences.putBoolean(KEY_MAKE_RELATIVE_PATH,flag);
  }

}
