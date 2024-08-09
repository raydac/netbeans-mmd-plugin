/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.annotations.processor;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.swing.ide.IDEBridge;
import com.igormaznitsa.mindmap.swing.ide.NotificationType;
import javax.swing.Icon;

public class ProcessorBridge implements IDEBridge {
  @Override
  public Version getIDEVersion() {
    return MmdAnnotationProcessor.VERSION;
  }

  @Override
  public String getIDEGeneratorId() {
    return "mindmap-annotation-processor";
  }

  @Override
  public void showIDENotification(String title, String message, NotificationType type) {

  }

  @Override
  public void notifyRestart() {

  }

  @Override
  public Icon loadIcon(String path, Class<?> baseClassToLoad) {
    return null;
  }
}
