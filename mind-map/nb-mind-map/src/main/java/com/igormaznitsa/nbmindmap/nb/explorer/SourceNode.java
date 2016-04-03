package com.igormaznitsa.nbmindmap.nb.explorer;

import com.igormaznitsa.nbmindmap.utils.BadgeIcons;

import java.awt.Image;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.netbeans.api.project.Project;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.util.lookup.ProxyLookup;

class SourceNode extends AbstractMMFilter {

  private volatile Image icon;
  private volatile Image iconOpen;
  
  SourceNode(@Nonnull final Project project, @Nonnull final DataFolder folder, @Nonnull final DataFilter filter, final String name) {
    this(project, folder, new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(filter)), name);
  }

  private SourceNode(@Nonnull final Project project, @Nonnull final DataFolder folder, @Nonnull final FilterNode node, @Nonnull final String name) {
    super(node, org.openide.nodes.Children.createLazy(new Callable<org.openide.nodes.Children>() {
      @Override
      public org.openide.nodes.Children call() throws Exception {
        return new FolderChildren(project, node);
      }
    }), new ProxyLookup(folder.getNodeDelegate().getLookup()));

    disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_GET_ACTIONS);
    setDisplayName(name);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(@Nullable final Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    if (obj instanceof SourceNode){
      return super.equals(obj);
    }
    return false;
  }

  public void setIcons(@Nullable final Image icon, @Nullable final Image iconOpen){
    this.icon = icon;
    this.iconOpen = iconOpen;
  }
  
  @Override
  @Nonnull
  public String getShortDescription() {
    return this.getOriginal().getShortDescription();
  }

  @Override
  @Nonnull
  public Image getIcon(final int type) {
    return  this.icon == null ? BadgeIcons.getTreeFolderIcon(false) : this.icon;
  }

  @Override
  @Nonnull
  public Image getOpenedIcon(final int type) {
    return this.iconOpen == null ? BadgeIcons.getTreeFolderIcon(true) : this.iconOpen;
  }

  @Override
  public boolean canCopy() {
    return false;
  }

  @Override
  public boolean canCut() {
    return false;
  }

  @Override
  public boolean canRename() {
    return false;
  }

  @Override
  public boolean canDestroy() {
    return false;
  }

}
