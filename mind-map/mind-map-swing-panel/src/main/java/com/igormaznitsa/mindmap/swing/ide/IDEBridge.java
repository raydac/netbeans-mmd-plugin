/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.swing.ide;

import javax.annotation.Nonnull;
import com.igormaznitsa.commons.version.Version;

/**
 * It describes some bridge to internal features of IDEs.
 */
public interface IDEBridge {
  /**
   * Allows to get the IDE version info.
   * @return the IDE version info as version object, must not be null
   * @since 1.2.0
   */
  @Nonnull
  Version getIDEVersion();
  
  /**
   * Show notification in IDE.
   * @param title the title of notification, must not be null, html tags will not be processed.
   * @param message the message, must not be null. html tags will not be processed
   * @param type type of the notification, must not be null
   * 
   * @since 1.2.0
   */
  void showIDENotification(@Nonnull String title, @Nonnull String message, @Nonnull NotificationType type);
}
