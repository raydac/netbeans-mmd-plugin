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

package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MMapURI implements Serializable {

  public static final long serialVersionUID = 27896411234L;

  private static final Properties EMPTY = new Properties();

  private final URI uri;
  private final Properties parameters;
  private final boolean fileUriFlag;

  public MMapURI(@Nonnull final String uri) throws URISyntaxException {
    this(new URI(uri));
  }

  public MMapURI(@Nonnull final URI uri) {
    this.fileUriFlag = Assertions.assertNotNull(uri).getScheme() == null ||
        uri.getScheme().equalsIgnoreCase("file"); //NOI18N

    final URI preparedURI;

    final String queryString = uri.getRawQuery();
    if (queryString != null) {
      this.parameters = ModelUtils.extractQueryPropertiesFromURI(uri);
      if (this.fileUriFlag) {
        try {
          final String uriAsString = uri.toString();
          final int queryStart = uriAsString.lastIndexOf('?');
          preparedURI =
              new URI(queryStart >= 0 ? uriAsString.substring(0, queryStart) : uriAsString);
        } catch (URISyntaxException ex) {
          throw new Error("Unexpected error", ex);
        }
      } else {
        preparedURI = uri;
      }
    } else {
      this.parameters = EMPTY;
      preparedURI = uri;
    }
    this.uri = preparedURI;
  }

  private MMapURI(@Nonnull final URI uri, final boolean isFile,
                  @Nullable final Properties properties) {
    this.uri = uri;
    this.fileUriFlag = isFile;
    this.parameters = properties == null ? new Properties() : (Properties) properties.clone();
  }

  public MMapURI(@Nullable final File nullableBase, @Nonnull final File file,
                 @Nullable final Properties nullableParameters) {
    this.fileUriFlag = true;
    this.parameters = new Properties();
    if (nullableParameters != null && !nullableParameters.isEmpty()) {
      this.parameters.putAll(nullableParameters);
    }

    final Path filePath = Paths.toPath(file);

    if (nullableBase == null) {
      this.uri = ModelUtils.toURI(filePath);
    } else {
      final Path basePath = Paths.toPath(nullableBase);
      if (basePath.isAbsolute()) {
        final Path path = filePath.startsWith(basePath) ? basePath.relativize(filePath) : filePath;
        this.uri = ModelUtils.toURI(path);
      } else {
        this.uri = ModelUtils.toURI(filePath);
      }
    }
  }

  @Nullable
  private static String extractHost(@Nonnull final URI uri) {
    String host = uri.getHost();
    if (host == null) {
      final String schemeSpecific = uri.getSchemeSpecificPart();
      if (schemeSpecific != null && schemeSpecific.startsWith("//")) {
        host = "";
      }
    }
    return host;
  }

  @SuppressWarnings("ConstantConditions")
  @Nonnull
  public static MMapURI makeFromFilePath(@Nullable final File base, @Nonnull final String filePath,
                                         @Nullable final Properties properties) {
    return new MMapURI(base, ModelUtils.makeFileForPath(filePath), properties);
  }

  @Override
  public int hashCode() {
    return this.uri.hashCode() ^ (this.fileUriFlag ? 1 : 0) ^ (31 * this.parameters.size());
  }

  @Override
  public boolean equals(@Nullable final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof MMapURI) {
      final MMapURI thatURI = (MMapURI) that;
      if (this.parameters.size() != thatURI.parameters.size()) {
        return false;
      }
      for (final String s : this.parameters.stringPropertyNames()) {
        if (!thatURI.parameters.containsKey(s)) {
          return false;
        }
        if (!this.parameters.getProperty(s).equals(thatURI.parameters.getProperty(s))) {
          return false;
        }
      }
      return this.uri.equals(thatURI.uri);
    } else {
      return false;
    }
  }

  @Nonnull
  public MMapURI replaceBaseInPath(final boolean replaceHost, @Nonnull final URI newBase,
                                   int currentNumberOfResourceItemsTheLasIsZero)
      throws URISyntaxException {
    final String newURIPath = newBase.getPath();
    final String[] splittedNewPath = newURIPath.split("\\/");
    final String[] splittedOldPath = this.uri.getPath().split("\\/");

    final List<String> resultPath = new ArrayList<String>();

    resultPath.addAll(Arrays.asList(splittedNewPath));

    currentNumberOfResourceItemsTheLasIsZero = currentNumberOfResourceItemsTheLasIsZero + 1;

    int oldPathIndex = splittedOldPath.length - currentNumberOfResourceItemsTheLasIsZero;

    while (oldPathIndex < splittedOldPath.length) {
      if (oldPathIndex >= 0) {
        resultPath.add(splittedOldPath[oldPathIndex]);
      }
      oldPathIndex++;
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

  @Nonnull
  public MMapURI replaceName(@Nonnull final String newName) throws URISyntaxException {
    final MMapURI result;
    final String normalizedName = ModelUtils.escapeURIPath(newName).replace('\\', '/');

    final String[] parsedNormalized = normalizedName.split("\\/");
    final String[] parsedCurrentPath = this.uri.getPath().split("\\/");

    final int baseLength = Math.max(0, parsedCurrentPath.length - parsedNormalized.length);

    final StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < baseLength; i++) {
      if (i > 0) {
        buffer.append('/');
      }
      buffer.append(parsedCurrentPath[i]);
    }

    for (int i = 0; i < parsedNormalized.length; i++) {
      if ((i == 0 && buffer.length() > 0) || i > 0) {
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

  @Nonnull
  public URI asURI() {
    if (this.fileUriFlag) {
      try {
        return new URI(this.uri.toASCIIString() + (this.parameters.isEmpty() ? "" :
            '?' + ModelUtils.makeQueryStringForURI(this.parameters)));
      } catch (URISyntaxException ex) {
        throw new Error("Unexpected error during URI convertation"); //NOI18N
      }
    } else {
      return this.uri;
    }
  }

  @Nonnull
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

  @Nonnull
  public String asString(final boolean ascII, final boolean addPropertiesAsQuery) {
    if (this.fileUriFlag) {
      return (ascII ? this.uri.toASCIIString() : this.uri.toString()) +
          (!addPropertiesAsQuery || this.parameters.isEmpty() ? "" :
              '?' + ModelUtils.makeQueryStringForURI(this.parameters)); //NOI18N
    } else {
      return ascII ? this.uri.toASCIIString() : this.uri.toString();
    }
  }

  @Nonnull
  public File asFile(@Nullable final File base) {
    final File result;
    if (this.uri.isAbsolute()) {
      result = ModelUtils.toFile(this.uri);
    } else {
      try {
        result = new File(base, URLDecoder.decode(this.uri.getPath(), "UTF-8")); //NOI18N
      } catch (UnsupportedEncodingException ex) {
        throw new Error("Unexpected error", ex); //NOI18N
      }
    }
    return result;
  }

  @Nonnull
  public Properties getParameters() {
    return (Properties) this.parameters.clone();
  }

  public boolean isAbsolute() {
    return this.uri.isAbsolute();
  }

  @Override
  @Nonnull
  public String toString() {
    return asString(false, true);
  }
}
