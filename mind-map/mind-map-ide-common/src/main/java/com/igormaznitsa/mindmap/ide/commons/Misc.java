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

/**
 * Misc functions and flags.
 * @since 1.4.2
 */
public final class Misc {

  private Misc() {
  }

  /**
   * Session key to keep last selected folder for added file into mind map node.
   * Object is File.
   */
  public static final String SESSIONKEY_ADD_FILE_LAST_FOLDER = "file.add.last.folder";
  
  /**
   * Session key to keep last selected flag to open in system viewer for added file into mind map node.
   * Object is Boolean
   */
  public static final String SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM = "file.add.open_in_system";
  
}
