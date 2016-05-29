package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.swing.services.IDEInfoProvider;
import com.intellij.openapi.application.ApplicationInfo;

import javax.annotation.Nonnull;

public class IdeaIDEInfoProvider implements IDEInfoProvider {

  private final Version ideVersion;

  public IdeaIDEInfoProvider(){
    final ApplicationInfo info = ApplicationInfo.getInstance();
    final long major = safeNumberExtraction(info.getMajorVersion());
    final long minor = safeNumberExtraction(info.getMinorVersion());
    final long micro = safeNumberExtraction(info.getMicroVersion());
    this.ideVersion = new Version("intellij",new long[]{major,minor,micro},info.getVersionName());
  }

  private static long safeNumberExtraction(final String data){
    if (data == null) return 0L;
    final StringBuilder buffer = new StringBuilder();
    for(final char c : data.toCharArray()){
      if (Character.isDigit(c)) buffer.append(c);
    }
    try {
      return Long.parseLong(buffer.toString());
    }catch(NumberFormatException ex){
      return 0L;
    }
  }

  @Nonnull @Override public Version getIDEVersion() {
    return this.ideVersion;
  }
}
