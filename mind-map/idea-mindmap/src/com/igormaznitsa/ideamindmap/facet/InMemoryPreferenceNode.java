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

package com.igormaznitsa.ideamindmap.facet;

import com.intellij.util.Base64;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringEscapeUtils;

public class InMemoryPreferenceNode extends Preferences {

  private final List<PreferenceChangeListener> preferenceChangeListeners = new ArrayList<>();
  private final List<NodeChangeListener> nodeChangeListeners = new ArrayList<>();

  private final Map<String, String> storage = new HashMap<>();

  private void firePreferenceListeners(final String key, final String newValue) {
    final PreferenceChangeEvent event = new PreferenceChangeEvent(this, key, newValue);
    for (final PreferenceChangeListener l : this.preferenceChangeListeners) {
      l.preferenceChange(event);
    }
  }

  @Override
  public void put(@Nonnull final String key, @Nonnull final String value) {
    this.storage.put(key, value);
    firePreferenceListeners(key, value);
  }

  @Override
  public String get(@Nonnull final String key, @Nullable String def) {
    return this.storage.get(key);
  }

  @Override
  public void remove(@Nonnull final String key) {
    if (this.storage.remove(key) != null) {
      firePreferenceListeners(key, null);
    }
  }

  @Override
  public void clear() throws BackingStoreException {
    final Map<String, String> copy = new HashMap<>(this.storage);
    this.storage.clear();
    for (final String k : copy.keySet()) {
      firePreferenceListeners(k, null);
    }
  }

  @Override
  public void putInt(@Nonnull final String key, final int value) {
    this.put(key, Integer.toString(value));
  }

  @Override
  public int getInt(@Nonnull String key, final int def) {
    final String value = this.get(key, Integer.toString(def));
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  @Override
  public void putLong(@Nonnull final String key, final long value) {
    this.put(key, Long.toString(value));
  }

  @Override
  public long getLong(String key, long def) {
    final String value = this.get(key, Long.toString(def));
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  @Override
  public void putBoolean(String key, boolean value) {
    this.put(key, Boolean.toString(value));
  }

  @Override
  public boolean getBoolean(String key, boolean def) {
    final String value = this.get(key, null);
    return value == null ? def : Boolean.parseBoolean(value);
  }

  @Override
  public void putFloat(String key, float value) {
    this.put(key, Float.toString(value));
  }

  @Override
  public float getFloat(String key, float def) {
    final String value = get(key, Float.toString(def));
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  @Override
  public void putDouble(String key, double value) {
    this.put(key, Double.toString(value));
  }

  @Override
  public double getDouble(String key, double def) {
    final String value = get(key, Double.toString(def));
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  @Override
  public void putByteArray(String key, byte[] value) {
    this.put(key, Base64.encode(value));
  }

  @Override
  public byte[] getByteArray(String key, byte[] def) {
    final String value = this.get(key, null);
    try {
      return Base64.decode(value);
    } catch (Exception ex) {
      return def;
    }
  }

  @Override
  public String[] keys() throws BackingStoreException {
    return this.storage.keySet().toArray(new String[0]);
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    return new String[0];
  }

  @Override
  public Preferences parent() {
    return null;
  }

  @Override
  public Preferences node(final String pathName) {
    return null;
  }

  @Override
  public boolean nodeExists(final String pathName) throws BackingStoreException {
    return false;
  }

  @Override
  public void removeNode() throws BackingStoreException {

  }

  @Override
  public String name() {
    return "..";
  }

  @Override
  public String absolutePath() {
    return "..";
  }

  @Override
  public boolean isUserNode() {
    return false;
  }

  @Override
  public String toString() {
    return InMemoryPreferenceNode.class.getName() + "[" + this.storage.size() + ']';
  }

  @Override
  public void flush() throws BackingStoreException {

  }

  @Override
  public void sync() throws BackingStoreException {

  }

  @Override
  public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
    this.preferenceChangeListeners.add(pcl);
  }

  @Override
  public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
    this.preferenceChangeListeners.remove(pcl);
  }

  @Override
  public void addNodeChangeListener(NodeChangeListener ncl) {
    this.nodeChangeListeners.add(ncl);
  }

  @Override
  public void removeNodeChangeListener(NodeChangeListener ncl) {
    this.nodeChangeListeners.remove(ncl);
  }

  @Override
  public void exportNode(OutputStream os) throws IOException, BackingStoreException {
    final StringBuilder builder = new StringBuilder();
    for (final Map.Entry<String, String> entry : this.storage.entrySet()) {
      builder.append(StringEscapeUtils.escapeCsv(entry.getKey())).append(',').append(StringEscapeUtils.escapeCsv(entry.getValue())).append('\n');
    }
    os.write(builder.toString().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {

  }
}
