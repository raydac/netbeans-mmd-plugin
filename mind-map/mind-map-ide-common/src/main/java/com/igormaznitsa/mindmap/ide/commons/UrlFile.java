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
package com.igormaznitsa.mindmap.ide.commons;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;

public final class UrlFile {

  private final Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();

  private static final Pattern SECTION_NAME = Pattern.compile("^\\s*\\[([^\\]]*?)\\]\\s*$");
  private static final Pattern NAME_VALUE = Pattern.compile("^([^=\\s]+?)\\=(.*)$");

  private final int savedPairs;
  
  public UrlFile(@Nonnull final File file) throws IOException {
    this(FileUtils.readFileToString(file, "Cp1252"));
  }
  
  public UrlFile(@Nonnull final String text) throws IOException {
    Map<String,String> currentSection = null;
    
    int counter = 0;
    
    for (final String splittedLine : text.split("\\r\\n")) {
      if (splittedLine.trim().isEmpty()) {
        continue;
      }
      final Matcher head = SECTION_NAME.matcher(splittedLine);
      if (head.matches()) {
        final String sectionName = head.group(1);
        if (currentSection!=null) counter+=currentSection.size();
        currentSection = new HashMap<String,String>();
        this.sections.put(sectionName, currentSection);
      } else {
        final Matcher keyvalue = NAME_VALUE.matcher(splittedLine);
        if (keyvalue.matches()){
          final String key = keyvalue.group(1);
          final String value = keyvalue.group(2);
          if (currentSection!=null){
            currentSection.put(key, value);
          }
        }
      }
    }
    
    if (currentSection!=null) counter+=currentSection.size();
    
    this.savedPairs = counter;
  }
  
  public int size(){
    return this.savedPairs;
  }
  
  @Nullable
  public String getURL(){
    return this.getValue("InternetShortcut", "URL");
  }
  
  @Nullable
  public String getValue(@Nonnull final String section, @Nonnull final String key) {
    final Map<String,String> sectionMap = this.sections.get(section);
    return sectionMap == null ? null : sectionMap.get(key);
  }
  
}
