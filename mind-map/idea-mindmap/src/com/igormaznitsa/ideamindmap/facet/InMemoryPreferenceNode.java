package com.igormaznitsa.ideamindmap.facet;

import com.intellij.util.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class InMemoryPreferenceNode extends Preferences {

  private final List<PreferenceChangeListener> preferenceChangeListeners = new ArrayList<PreferenceChangeListener>();
  private final List<NodeChangeListener> nodeChangeListeners = new ArrayList<NodeChangeListener>();

  private final Map<String,String> storage = new HashMap<String,String>();

  private void firePreferenceListeners(final String key, final String newValue){
    final PreferenceChangeEvent event = new PreferenceChangeEvent(this,key,newValue);
    for(final PreferenceChangeListener l : this.preferenceChangeListeners){
      l.preferenceChange(event);
    }
  }

  @Override public void put(@NotNull final String key, @NotNull final String value) {
    this.storage.put(key,value);
    firePreferenceListeners(key,value);
  }

  @Override public String get(@NotNull final String key, @Nullable String def) {
    return this.storage.get(key);
  }

  @Override public void remove(@NotNull final String key) {
    if (this.storage.remove(key)!=null){
      firePreferenceListeners(key,null);
    }
  }

  @Override public void clear() throws BackingStoreException {
    final Map<String,String> copy = new HashMap<String,String>(this.storage);
    this.storage.clear();
    for(final String k : copy.keySet()){
      firePreferenceListeners(k,null);
    }
  }

  @Override public void putInt(@NotNull final String key, final int value) {
    this.put(key,Integer.toString(value));
  }

  @Override public int getInt(@NotNull String key, final int def) {
    final String value = this.get(key,Integer.toString(def));
    try{
      return Integer.parseInt(value);
    }catch(NumberFormatException ex){
      return def;
    }
  }

  @Override public void putLong(@NotNull final String key, final long value) {
    this.put(key,Long.toString(value));
  }

  @Override public long getLong(String key, long def) {
    final String value = this.get(key,Long.toString(def));
    try{
      return Long.parseLong(value);
    }catch(NumberFormatException ex){
      return def;
    }
  }

  @Override public void putBoolean(String key, boolean value) {
    this.put(key,Boolean.toString(value));
  }

  @Override public boolean getBoolean(String key, boolean def) {
    final String value = this.get(key,null);
    return value == null ? def : Boolean.parseBoolean(value);
  }

  @Override public void putFloat(String key, float value) {
    this.put(key,Float.toString(value));
  }

  @Override public float getFloat(String key, float def) {
    final String value = get(key,Float.toString(def));
    try{
      return Float.parseFloat(value);
    }catch(NumberFormatException ex){
      return def;
    }
  }

  @Override public void putDouble(String key, double value) {
    this.put(key,Double.toString(value));
  }

  @Override public double getDouble(String key, double def) {
    final String value = get(key,Double.toString(def));
    try{
      return Double.parseDouble(value);
    }catch(NumberFormatException ex){
      return def;
    }
  }

  @Override public void putByteArray(String key, byte[] value) {
    this.put(key, Base64.encode(value));
  }

  @Override public byte[] getByteArray(String key, byte[] def) {
    final String value = this.get(key,null);
    try{
      return Base64.decode(value);
    }catch(Exception ex){
      return def;
    }
  }

  @Override public String[] keys() throws BackingStoreException {
    return this.storage.keySet().toArray(new String[this.storage.size()]);
  }

  @Override public String[] childrenNames() throws BackingStoreException {
    return new String[0];
  }

  @Override public Preferences parent() {
    return null;
  }

  @Override public Preferences node(final String pathName) {
    return null;
  }

  @Override public boolean nodeExists(final String pathName) throws BackingStoreException {
    return false;
  }

  @Override public void removeNode() throws BackingStoreException {

  }

  @Override public String name() {
    return "..";
  }

  @Override public String absolutePath() {
    return "..";
  }

  @Override public boolean isUserNode() {
    return false;
  }

  @Override public String toString() {
    return InMemoryPreferenceNode.class.getName()+"["+this.storage.size()+']';
  }

  @Override public void flush() throws BackingStoreException {

  }

  @Override public void sync() throws BackingStoreException {

  }

  @Override public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
    this.preferenceChangeListeners.add(pcl);
  }

  @Override public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
    this.preferenceChangeListeners.remove(pcl);
  }

  @Override public void addNodeChangeListener(NodeChangeListener ncl) {
    this.nodeChangeListeners.add(ncl);
  }

  @Override public void removeNodeChangeListener(NodeChangeListener ncl) {
    this.nodeChangeListeners.remove(ncl);
  }

  @Override public void exportNode(OutputStream os) throws IOException, BackingStoreException {
    final StringBuilder builder = new StringBuilder();
    for(final String key : this.storage.keySet()){
      final String value = this.storage.get(key);
      builder.append(StringEscapeUtils.escapeCsv(key)).append(',').append(StringEscapeUtils.escapeCsv(value)).append('\n');
    }
    os.write(builder.toString().getBytes("UTF-8"));
  }

  @Override public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {

  }
}
