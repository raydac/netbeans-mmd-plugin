package com.igormaznitsa.ideamindmap.utils;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.util.Base64;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class PluginPreferences extends Preferences {

    private static final PropertiesComponent component = PropertiesComponent.getInstance();

    private static String fullKey(final String key){
       return "nb-idea-mind-map."+key;
    }

    @Override
    public void put(String key, String value) {
        component.setValue(fullKey(key),value);
    }

    @Override
    public String get(String key, String def) {
        return component.getValue(fullKey(key),def);
    }

    @Override
    public void remove(String key) {
        component.setValue(fullKey(key),null);
    }


    @Override
    public void putInt(final String key, int value) {
        component.setValue(fullKey(key),value,Integer.MIN_VALUE);
    }

    @Override
    public int getInt(String key, int def) {
        return component.getInt(fullKey(key),def);
    }

    @Override
    public void putLong(String key, long value) {
        component.setValue(fullKey(key),Long.toString(value));
    }

    @Override
    public long getLong(String key, long def) {
        final String val = component.getValue(fullKey(key),Long.toString(def));
        return Long.parseLong(val);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        component.setValue(fullKey(key),value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return component.getBoolean(fullKey(key),def);
    }

    @Override
    public void putFloat(String key, float value) {
        component.setValue(fullKey(key),value,Float.MIN_VALUE);
    }

    @Override
    public float getFloat(String key, float def) {
        return component.getFloat(fullKey(key),def);
    }

    @Override
    public void putDouble(String key, double value) {
        component.setValue(fullKey(key),Double.toString(value));
    }

    @Override
    public double getDouble(String key, double def) {
        final String val = component.getValue(fullKey(key),Double.toString(def));
        return Double.parseDouble(val);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        component.setValue(fullKey(key), Base64.encode(value));
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        final String str = component.getValue(fullKey(key), def == null ? null : Base64.encode(def));
        return str == null ? null : Base64.decode(str);
    }

    @Override
    public String[] keys() throws BackingStoreException {
        return new String[0];
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
    public Preferences node(String pathName) {
        return null;
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeNode() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String absolutePath() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isUserNode() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String toString() {
        return PluginPreferences.class.toString();
    }

    @Override
    public void flush() throws BackingStoreException {
    }

    @Override
    public void sync() throws BackingStoreException {
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void clear() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported");
    }
}
