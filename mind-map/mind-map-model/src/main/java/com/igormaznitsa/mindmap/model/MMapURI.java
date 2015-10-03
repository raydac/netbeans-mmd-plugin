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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MMapURI implements Serializable {

  public static final long serialVersionUID = 27896411234L;

  private static final Properties EMPTY = new Properties();

  private final URI uri;
  private final Properties parameters;
  private final boolean fileUriFlag;

  private static String extractHost(final URI uri) {
    String host = uri.getHost();
    if (host == null) {
      final String schemeSpecific = uri.getSchemeSpecificPart();
      if (schemeSpecific != null && schemeSpecific.startsWith("//")) {
        host = "";
      }
    }
    return host;
  }

  public MMapURI(final String uri) throws URISyntaxException {
    this(new URI(uri));
  }

  public MMapURI(final URI uri) {
    ModelUtils.assertNotNull("URI must not be null", uri); //NOI18N

    this.fileUriFlag = uri.getScheme() == null ? true : uri.getScheme().equalsIgnoreCase("file"); //NOI18N

    final URI preparedURI;

    final String queryString = uri.getRawQuery();
    if (queryString != null) {
      this.parameters = ModelUtils.extractQueryPropertiesFromURI(uri);
      if (this.fileUriFlag) {
        try {
          final String uriAsString = uri.toString();
          final int queryStart = uriAsString.lastIndexOf('?');
          preparedURI = new URI(queryStart >= 0 ? uriAsString.substring(0,queryStart) : uriAsString);
        }
        catch (URISyntaxException ex) {
          throw new Error("Unexpected error", ex);
        }
      }
      else {
        preparedURI = uri;
      }
    }
    else {
      this.parameters = EMPTY;
      preparedURI = uri;
    }
    this.uri = preparedURI;
  }

  private MMapURI(final URI uri, final boolean isFile, final Properties properties) {
    this.uri = uri;
    this.fileUriFlag = isFile;
    this.parameters = properties == null ? new Properties() : (Properties) properties.clone();
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

  public MMapURI replaceBaseInPath(final boolean replaceHost, final URI newBase, int currentNumberOfResourceItemsTheLasIsZero) throws URISyntaxException {
    final String newURIPath = newBase.getPath();
    final String[] splittedNewPath = newURIPath.split("\\/");
    final String[] splittedOldPath = this.uri.getPath().split("\\/");

    final List<String> resultPath = new ArrayList<>();

    for (final String s : splittedNewPath) {
      resultPath.add(s);
    }

    final int firstResourceIndex = resultPath.size();

    currentNumberOfResourceItemsTheLasIsZero = currentNumberOfResourceItemsTheLasIsZero + 1;
  
    int oldPathIndex = splittedOldPath.length - currentNumberOfResourceItemsTheLasIsZero; 
    
    while (oldPathIndex < splittedOldPath.length) {
      if (oldPathIndex>=0){
        resultPath.add(splittedOldPath[oldPathIndex]);
      }
      oldPathIndex ++;
    }

    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < resultPath.size(); i++) {
      if (i > 0) {
        buffer.append('/');
      }
      buffer.append(resultPath.get(i));
    }

    final URI newURI = new URI(replaceHost ? newBase.getScheme() : this.uri.getScheme(),
            replaceHost ? newBase.getUserInfo() : this.uri.getUserInfo(),
            replaceHost ? extractHost(newBase) : extractHost(this.uri),
            replaceHost ? newBase.getPort() : this.uri.getPort(),
            buffer.toString(),
            this.uri.getQuery(),
            this.uri.getFragment()
    );

    return new MMapURI(newURI, this.fileUriFlag, this.parameters);
  }

  public MMapURI replaceName(final String newName) throws URISyntaxException {
    final MMapURI result;
    final String normalizedName = ModelUtils.escapeURIPath(newName).replace('\\', '/');

    final String [] parsedNormalized = normalizedName.split("\\/");
    final String [] parsedCurrentPath = this.uri.getPath().split("\\/");

    final int baseLength = Math.max(0, parsedCurrentPath.length - parsedNormalized.length);
    
    final StringBuilder buffer = new StringBuilder();

    for(int i=0;i<baseLength;i++){
      if (i>0){
        buffer.append('/');
      }
      buffer.append(parsedCurrentPath[i]);
    }
    
    for(int i=0;i<parsedNormalized.length;i++){
      if ((i==0 && buffer.length()>0) || i>0){
        buffer.append('/');
      }
      buffer.append(parsedNormalized[i]);
    }
    
    result = new MMapURI(new URI(
            this.uri.getScheme(),
            this.uri.getUserInfo(),
            extractHost(this.uri),
            this.uri.getPort(),
            buffer.toString(),
            this.uri.getQuery(),
            this.uri.getFragment()), this.fileUriFlag, parameters);
    return result;
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
