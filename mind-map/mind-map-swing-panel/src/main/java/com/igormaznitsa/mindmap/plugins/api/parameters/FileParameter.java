/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.plugins.api.parameters;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileFilter;

public class FileParameter extends AbstractParameter<File> {

  public interface FileChooserParamsProvider {
    String getTitle();

    FileFilter [] getFileFilters();

    String getApproveText();

    boolean isFilesOnly();
  }

  private final FileChooserParamsProvider fileChooserParamsProvider;

  public FileParameter(final String id, final String title, final String comment,
                       final File defaultValue, final FileChooserParamsProvider fileChooserParamsProvider) {
    this(id, title, comment, defaultValue, fileChooserParamsProvider, 0);
  }

  public FileParameter(final String id, final String title, final String comment,
                       final File defaultValue, final FileChooserParamsProvider fileChooserParamsProvider, final int order) {
    super(id, title, comment, defaultValue, order);
    this.fileChooserParamsProvider = requireNonNull(fileChooserParamsProvider);
  }

  public FileChooserParamsProvider getFileChooserParamsProvider(){
    return this.fileChooserParamsProvider;
  }

  @Override
  public void fromString(final String value) {
    this.setValue(new File(value));
  }
}
