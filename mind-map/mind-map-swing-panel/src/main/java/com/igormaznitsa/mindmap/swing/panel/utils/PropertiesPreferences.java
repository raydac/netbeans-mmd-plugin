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
package com.igormaznitsa.mindmap.swing.panel.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.GetUtils;

/**
 * Auxiliary class implementing Preferences, based on Properties. 
 * @since 1.3.1
 */
public class PropertiesPreferences extends Preferences {

  private final Properties storage = new Properties();
  private final List<PreferenceChangeListener> listeners = new ArrayList<PreferenceChangeListener>();
  private final String comment;
  
  public PropertiesPreferences(@Nullable final String comment, @Nonnull final String text) throws IOException {
    this(comment);
    this.storage.load(new StringReader(text));
  }
  
  public PropertiesPreferences(@Nullable final String comment){
    super();
    this.comment = comment;
  }
  
  @Override
  public void put(@Nonnull final String key, @Nonnull final String value) {
    this.storage.setProperty(key, value);
    fireListeners(key, value);
  }

  @Override
  @Nullable
  public String get(@Nonnull final String key, @Nullable final String def) {
    return this.storage.containsKey(key) ? this.storage.getProperty(key) : def;
  }

  @Override
  public void remove(@Nonnull final String key) {
    this.storage.remove(key);
    fireListeners(key, null);
  }

  @Override
  public void clear() throws BackingStoreException {
    this.storage.clear();
    fireListeners("", null);
  }

  @Override
  public void putInt(@Nonnull final String key, final int value) {
    final String newvalue = Integer.toString(value);
    this.storage.setProperty(key, newvalue);
    fireListeners(key, newvalue);
  }

  @Override
  public int getInt(@Nonnull final String key, final int def) {
    final String value = this.storage.getProperty(key);
    return value == null ? def : Integer.parseInt(value);
  }

  @Override
  public void putLong(@Nonnull final String key, final long value) {
    final String newvalue = Long.toString(value);
    this.storage.setProperty(key, newvalue);
    fireListeners(key, newvalue);
  }

  @Override
  public long getLong(@Nonnull final String key, final long def) {
    final String value = this.storage.getProperty(key);
    return value == null ? def : Long.parseLong(value);
  }

  @Override
  public void putBoolean(@Nonnull final String key, final boolean value) {
    final String newvalue = Boolean.toString(value);
    this.storage.setProperty(key, newvalue);
    fireListeners(key, newvalue);
  }

  @Override
  public boolean getBoolean(@Nonnull final String key, final boolean def) {
    final String value = this.storage.getProperty(key);
    return value == null ? def : Boolean.parseBoolean(value);
  }

  @Override
  public void putFloat(@Nonnull final String key, final float value) {
    final String newvalue = Float.toString(value);
    this.storage.setProperty(key, newvalue);
    fireListeners(key, newvalue);
  }

  @Override
  public float getFloat(@Nonnull final String key, final float def) {
    final String value = this.storage.getProperty(key);
    return value == null ? def : Float.parseFloat(value);
  }

  @Override
  public void putDouble(@Nonnull final String key, final double value) {
    final String newvalue = Double.toString(value);
    this.storage.setProperty(key, newvalue);
    fireListeners(key, newvalue);
  }

  @Override
  public double getDouble(@Nonnull final String key, final double def) {
    final String value = this.storage.getProperty(key);
    return value == null ? def : Double.parseDouble(value);
  }

  @Override
  public void putByteArray(@Nonnull final String key, @Nonnull final byte[] value) {
    final String data = Utils.base64encode(value);
    this.storage.setProperty(key, data);
    fireListeners(key, data);
  }

  @Override
  @Nullable
  public byte[] getByteArray(@Nonnull final String key, @Nullable final byte[] def) {
    final String found = this.storage.getProperty(key);
    try{
      return found == null ? def : Utils.base64decode(found);
    }catch(final IOException ex) {
      throw new RuntimeException("Error during BASE64 decode",ex);
    }
  }

  @Override
  @Nonnull
  @MustNotContainNull
  public String[] keys() throws BackingStoreException {
    final Set<String> keys = this.storage.stringPropertyNames();
    return keys.toArray(new String[keys.size()]);
  }

  @Override
  @Nullable
  @MustNotContainNull
  public String[] childrenNames() throws BackingStoreException {
    return null;
  }

  @Override
  @Nullable
  public Preferences parent() {
    return null;
  }

  @Override
  @Nullable
  public Preferences node(@Nonnull final String pathName) {
    return null;
  }

  @Override
  public boolean nodeExists(@Nonnull final String pathName) throws BackingStoreException {
    return false;
  }

  @Override
  public void removeNode() throws BackingStoreException {
  }

  @Override
  @Nonnull
  public String name() {
    return "/";
  }

  @Override
  @Nonnull
  public String absolutePath() {
    return "/";
  }

  @Override
  public boolean isUserNode() {
    return false;
  }

  @Override
  @Nonnull
  public String toString() {
    final StringWriter writer = new StringWriter();
    try{
      this.storage.store(writer, GetUtils.ensureNonNull(this.comment, ""));
      return writer.toString();
    }catch(IOException ex){
      throw new Error("Unexpected error",ex);
    }
  }

  @Override
  public void flush() throws BackingStoreException {
  }

  @Override
  public void sync() throws BackingStoreException {
  }

  @Override
  public void addPreferenceChangeListener(@Nonnull final PreferenceChangeListener pcl) {
    this.listeners.add(pcl);
  }

  @Override
  public void removePreferenceChangeListener(@Nonnull final PreferenceChangeListener pcl) {
    this.listeners.remove(pcl);
  }

  @Override
  public void addNodeChangeListener(@Nonnull final NodeChangeListener ncl) {
  }

  @Override
  public void removeNodeChangeListener(@Nonnull final NodeChangeListener ncl) {
  }

  @Override
  public void exportNode(@Nonnull final OutputStream os) throws IOException, BackingStoreException {
    this.exportSubtree(os);
  }

  @Override
  public void exportSubtree(@Nonnull final OutputStream os) throws IOException, BackingStoreException {
    this.storage.store(os, GetUtils.ensureNonNull(this.comment,""));
  }
  
  private void fireListeners(@Nonnull final String key, @Nullable final String newValue){
    final PreferenceChangeEvent event = new PreferenceChangeEvent(this, key, newValue);
    for(final PreferenceChangeListener l : this.listeners){
      l.preferenceChange(event);
    }
  }
}
