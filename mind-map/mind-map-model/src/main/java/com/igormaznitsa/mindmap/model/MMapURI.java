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
package com.igormaznitsa.mindmap.model;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Properties;

public class MMapURI implements Serializable {

  public static final long serialVersionUID = 27896411234L;
  
  private static final Properties EMPTY = new Properties();

  private final URI uri;
  private final Properties parameters;
  private final boolean fileUriFlag;

  public MMapURI(final String uri) throws URISyntaxException {
    this(new URI(uri));
  }

  public MMapURI(final URI uri) {
    ModelUtils.assertNotNull("URI must not be nukk", uri); //NOI18N

    this.fileUriFlag = uri.getScheme() == null ? true : uri.getScheme().equalsIgnoreCase("file"); //NOI18N

    final URI preparedURI;

    final String queryString = uri.getRawQuery();
    if (queryString != null) {
      this.parameters = ModelUtils.extractQueryPropertiesFromURI(uri);
      if (this.fileUriFlag){
      try {
        preparedURI = new URI(uri.getScheme(), null, uri.getHost(), -1, uri.getPath(), null, null);
      }
      catch (URISyntaxException ex) {
        throw new Error("Unexpected error", ex);
      }
      }else{
        preparedURI = uri;
      }
    }
    else {
      this.parameters = EMPTY;
      preparedURI = uri;
    }
    this.uri = preparedURI;
  }

  public MMapURI(final File nullableBase, final File file, final Properties nullableParameters) {
    this.fileUriFlag = true;
    this.parameters = new Properties();
    if (nullableParameters != null && !nullableParameters.isEmpty()) {
      this.parameters.putAll(nullableParameters);
    }

    final Path filePath = file.toPath();

    if (nullableBase == null) {
      this.uri = ModelUtils.toURI(filePath);
    }
    else {
      final Path basePath = nullableBase.toPath();
      if (basePath.isAbsolute()) {
        final Path path = filePath.startsWith(basePath) ? basePath.relativize(filePath) : filePath;
        this.uri = ModelUtils.toURI(path);
      }
      else {
        this.uri = ModelUtils.toURI(filePath);
      }
    }
  }

  public static MMapURI makeFromFilePath(final File base, final String filePath, final Properties properties) {
    return new MMapURI(base, ModelUtils.makeFileForPath(filePath), properties);
  }

  public URI asURI() {
    if (this.fileUriFlag) {
      try {
        return new URI(this.uri.toASCIIString() + (this.parameters.isEmpty() ? "" : '?' + ModelUtils.makeQueryStringForURI(this.parameters)));
      }
      catch (URISyntaxException ex) {
        throw new Error("Unexpected error during URI convertation"); //NOI18N
      }
    }
    else {
      return this.uri;
    }
  }

  public String getExtension() {
    String text = this.uri.getPath();
    final int lastSlash = text.lastIndexOf('/');
    if (lastSlash >= 0) {
      text = text.substring(lastSlash + 1);
    }
    String result = ""; //NOI18N
    if (!text.isEmpty()) {
      final int dotIndex = text.lastIndexOf('.');
      if (dotIndex >= 0) {
        result = text.substring(dotIndex + 1);
      }
    }
    return result;
  }

  public String asString(final boolean ascII, final boolean addPropertiesAsQuery) {
    if (this.fileUriFlag) {
      return (ascII ? this.uri.toASCIIString() : this.uri.toString()) + (!addPropertiesAsQuery || this.parameters.isEmpty() ? "" : '?' + ModelUtils.makeQueryStringForURI(this.parameters)); //NOI18N
    }
    else {
      return ascII ? this.uri.toASCIIString() : this.uri.toString();
    }
  }

  public File asFile(final File base) {
    final File result;
    if (this.uri.isAbsolute()) {
      result = ModelUtils.toFile(this.uri);
    }
    else {
      try {
        result = new File(base, URLDecoder.decode(this.uri.getPath(), "UTF-8")); //NOI18N
      }
      catch (UnsupportedEncodingException ex) {
        throw new Error("Unexpected error", ex); //NOI18N
      }
    }
    return result;
  }

  public Properties getParameters() {
    return (Properties) this.parameters.clone();
  }

  public boolean isAbsolute() {
    return this.uri.isAbsolute();
  }

  @Override
  public String toString() {
    return asString(true, true);
  }
}
