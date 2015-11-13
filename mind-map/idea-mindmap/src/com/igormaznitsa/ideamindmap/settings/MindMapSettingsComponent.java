package com.igormaznitsa.ideamindmap.settings;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class MindMapSettingsComponent extends ConfigurableProvider implements Configurable,ApplicationComponent{

  private static final Logger LOGGER = Logger.getInstance(MindMapSettingsComponent.class);

  private static MindMapSettingsComponent instance;

  private MindMapSettingsPanel uiPanel;

  public MindMapSettingsComponent getInstance(){
    if (instance == null){
      instance = new MindMapSettingsComponent();
    }

    return instance;
  }

  @Override public void initComponent() {

  }

  @Override public void disposeComponent() {
    this.uiPanel = null;
  }

  @Nls @Override public String getDisplayName() {
    return "NB Mind Map";
  }

  @Nullable @Override public String getHelpTopic() {
    return null;
  }

  @Nullable @Override public Configurable createConfigurable() {
    return getInstance();
  }

  @NotNull @Override public String getComponentName() {
    return "NBMindMapSettingsComponent";
  }

  @Nullable @Override public JComponent createComponent() {
    if (this.uiPanel == null){
      this.uiPanel = new MindMapSettingsPanel(this);
    }
    this.uiPanel.reset(MindMapApplicationSettings.findInstance().getConfig());
    return this.uiPanel.getPanel();
  }

  @Override public boolean isModified() {
    return this.uiPanel == null ? false : this.uiPanel.isModified();
  }

  @Override public void apply() throws ConfigurationException {
    if (this.uiPanel!=null){
      MindMapApplicationSettings.findInstance().fillBy(this.uiPanel.makeConfig());
    }
  }

  @Override public void reset() {
    if (this.uiPanel!=null){
      this.uiPanel.reset(MindMapApplicationSettings.findInstance().getConfig());
    }
  }

  @Override public void disposeUIResources() {
    this.uiPanel = null;
  }

}
