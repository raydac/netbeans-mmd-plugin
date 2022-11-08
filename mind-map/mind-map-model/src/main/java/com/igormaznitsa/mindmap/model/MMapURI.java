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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * URI wrapper to be used in bounds of mind map.
 */
public class MMapURI implements Serializable {

  public static final long serialVersionUID = 27896411234L;

  private static final Properties EMPTY = new Properties();

  private final URI uri;
  private final Properties parameters;
  private final boolean fileUriFlag;

  /**
   * Constructor.
   *
   * @param uri uri as string to be wrapped, must not be null
   * @throws URISyntaxException thrown if malformed URI format
   */
  public MMapURI(final String uri) throws URISyntaxException {
    this(new URI(uri));
  }

  /**
   * Constructor.
   *
   * @param uri uri to be wrapped, must not be null
   * @throws URISyntaxException thrown if malformed URI format
   */
  public MMapURI(final URI uri) throws URISyntaxException {
    this.fileUriFlag = requireNonNull(uri).getScheme() == null ||
        uri.getScheme().equalsIgnoreCase("file");

    final URI preparedURI;

    final String queryString = uri.getRawQuery();
    if (queryString != null) {
      this.parameters = ModelUtils.extractQueryPropertiesFromURI(uri);
      if (this.fileUriFlag) {
        final String uriAsString = uri.toString();
        final int queryStart = uriAsString.lastIndexOf('?');
        preparedURI =
            new URI(queryStart >= 0 ? uriAsString.substring(0, queryStart) : uriAsString);
      } else {
        preparedURI = uri;
      }
    } else {
      this.parameters = EMPTY;
      preparedURI = uri;
    }
    this.uri = preparedURI;
  }

  private MMapURI(
      final URI uri,
      final boolean isFile,
      final Properties properties
  ) {
    this.uri = uri;
    this.fileUriFlag = isFile;
    this.parameters = properties == null ? new Properties() : (Properties) properties.clone();
  }

  /**
   * Create from file.
   *
   * @param baseFolder base folder, can be null
   * @param file       target file, must not be null
   * @param parameters optional parameters for URI, can be null
   */
  public MMapURI(
      final File baseFolder,
      final File file,
      final Properties parameters
  ) {
    this.fileUriFlag = true;
    this.parameters = new Properties();
    if (parameters != null && !parameters.isEmpty()) {
      this.parameters.putAll(parameters);
    }

    final Path filePath = file.toPath();

    if (baseFolder == null) {
      this.uri = ModelUtils.toURI(filePath);
    } else {
      final Path basePath = baseFolder.toPath();
      if (basePath.isAbsolute()) {
        final Path path = filePath.startsWith(basePath) ? basePath.relativize(filePath) : filePath;
        this.uri = ModelUtils.toURI(path);
      } else {
        this.uri = ModelUtils.toURI(filePath);
      }
    }
  }

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

  /**
   * Create from file path
   *
   * @param baseFolder base folder, can be null
   * @param filePath   file path, must not be null
   * @param properties optional properties, can be null
   * @return created URI link, must not be null
   * @throws URISyntaxException thrown if errors with URI format
   */
  @SuppressWarnings("ConstantConditions")
  public static MMapURI makeFromFilePath(
      final File baseFolder,
      final String filePath,
      final Properties properties
  ) throws URISyntaxException {
    return new MMapURI(baseFolder, ModelUtils.makeFileForPath(filePath), properties);
  }

  @Override
  public int hashCode() {
    return this.uri.hashCode() ^ (this.fileUriFlag ? 1 : 0) ^ (31 * this.parameters.size());
  }

  @Override
  public boolean equals(final Object that) {
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

  /**
   * Make URI link with replaced base in path.
   *
   * @param replaceHost           if true then host should be replaced
   * @param newBase               new base for the link
   * @param numberOfResourceItems number of items to replace , last is zero
   * @return new URI with replaced base, must not be null
   * @throws URISyntaxException thrown if problem with URI
   */
  public MMapURI replaceBaseInPath(
      final boolean replaceHost,
      final URI newBase,
      int numberOfResourceItems
  )
      throws URISyntaxException {
    final String newUriPath = newBase.getPath();
    final String[] splitNewPath = newUriPath.split("\\/");
    final String[] splitOldPath = this.uri.getPath().split("\\/");

    final List<String> resultPath = new ArrayList<>(Arrays.asList(splitNewPath));

    numberOfResourceItems = numberOfResourceItems + 1;

    int oldPathIndex = splitOldPath.length - numberOfResourceItems;

    while (oldPathIndex < splitOldPath.length) {
      if (oldPathIndex >= 0) {
        resultPath.add(splitOldPath[oldPathIndex]);
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

  /**
   * Make new URI with replaced name.
   *
   * @param newName new name, must not be null
   * @return new URI, must not be null
   * @throws URISyntaxException if problems with URI syntax
   */
  public MMapURI replaceName(final String newName) throws URISyntaxException {
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

  /**
   * Convert into URI.
   *
   * @return converted URI, must not be null
   */
  public URI asURI() {
    if (this.fileUriFlag) {
      try {
        return new URI(this.uri.toASCIIString() + (this.parameters.isEmpty() ? "" :
            '?' + ModelUtils.makeQueryStringForURI(this.parameters)));
      } catch (URISyntaxException ex) {
        throw new Error("Unexpected error during URI conversion");
      }
    } else {
      return this.uri;
    }
  }

  /**
   * Get resource extension
   *
   * @return extension of the resource, can't be null, be empty if there is no extension
   */
  public String getExtension() {
    String text = this.uri.getPath();
    final int lastSlash = text.lastIndexOf('/');
    if (lastSlash >= 0) {
      text = text.substring(lastSlash + 1);
    }
    String result = "";
    if (!text.isEmpty()) {
      final int dotIndex = text.lastIndexOf('.');
      if (dotIndex >= 0) {
        result = text.substring(dotIndex + 1);
      }
    }
    return result;
  }

  /**
   * Convert int pstring.
   *
   * @param ascII                if true then convert into ASCII string
   * @param addPropertiesAsQuery if true then properties will be added as query
   * @return string created from the URI object, must not be null
   */
  public String asString(final boolean ascII, final boolean addPropertiesAsQuery) {
    if (this.fileUriFlag) {
      return (ascII ? this.uri.toASCIIString() : this.uri.toString()) +
          (!addPropertiesAsQuery || this.parameters.isEmpty() ? "" :
              '?' + ModelUtils.makeQueryStringForURI(this.parameters));
    } else {
      return ascII ? this.uri.toASCIIString() : this.uri.toString();
    }
  }

  /**
   * Get as file.
   *
   * @param baseFolder base folder if presented, can be null
   * @return file representation, must not be null
   */
  public File asFile(final File baseFolder) {
    final File result;
    if (this.uri.isAbsolute()) {
      result = ModelUtils.toFile(this.uri);
    } else {
      try {
        result = new File(baseFolder,
            URLDecoder.decode(this.uri.getPath(), StandardCharsets.UTF_8.name()));
      } catch (UnsupportedEncodingException ex) {
        throw new Error("Unexpected error", ex);
      }
    }
    return result;
  }

  /**
   * Get URI parameters.
   *
   * @return parameters, must be null
   */
  public Properties getParameters() {
    return this.parameters;
  }

  /**
   * Check that URI is absolute one
   *
   * @return true if the URI is absolute one, false for relative one
   */
  public boolean isAbsolute() {
    return this.uri.isAbsolute();
  }

  @Override
  public String toString() {
    return asString(false, true);
  }
}
