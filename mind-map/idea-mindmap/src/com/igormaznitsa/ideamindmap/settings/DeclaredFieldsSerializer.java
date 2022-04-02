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

package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.util.xmlb.annotations.Property;
import com.igormaznitsa.mindmap.swing.panel.SettingsAccessor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeclaredFieldsSerializer implements Serializable {

  private static final long serialVersionUID = -92387498231123L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DeclaredFieldsSerializer.class);

  @Property
  private final Map<String, String> storage = new TreeMap<>(Comparator.naturalOrder());

  public DeclaredFieldsSerializer() {
  }

  public DeclaredFieldsSerializer(@Nonnull final Object object, @Nullable final Converter converter) {
    visitGetters(object, (settingsName, fieldType, fieldValue) -> {
      try {
        if (fieldType.isPrimitive()) {
          if (fieldType == float.class) {
            storage.put(makeName(settingsName, fieldValue, false), Integer.toString((Float.floatToIntBits((Float) fieldValue))));
          } else if (fieldType == double.class) {
            storage.put(makeName(settingsName, fieldValue, false), Long.toString((Double.doubleToLongBits((Double) fieldValue))));
          } else {
            storage.put(makeName(settingsName, fieldValue, false), String.valueOf(fieldValue));
          }
        } else if (fieldType == String.class) {
          storage.put(makeName(settingsName, fieldValue, false), fieldValue == null ? "" : (String) fieldValue);
        } else {
          if (converter == null) {
            throw new NullPointerException("Unexpected field type " + fieldType.getName() + ", provide converter!");
          } else {
            final String converted = fieldValue == null ? null : converter.asString(fieldType, fieldValue);
            storage.put(makeName(settingsName, converted, true), converted);
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't make data for settings [" + settingsName + ']');
        if (ex instanceof RuntimeException) {
          throw ((RuntimeException) ex);
        } else {
          throw new Error("Can't serialize settings [" + settingsName + ']', ex);
        }
      }
    });
  }

  private static void visitGetters(@Nonnull final Object object, @Nonnull final GetterVisitor visitor) {
    for (final Method method : object.getClass().getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
        final SettingsAccessor accessor = method.getAnnotation(SettingsAccessor.class);
        if (accessor != null) {
          try {
            visitor.visitSettingsValue(accessor.name(), method.getReturnType(), method.invoke(object));
          } catch (Exception ex) {
            throw new RuntimeException("Error during get value: " + method, ex);
          }
        }
      }
    }
  }

  private static void visitSetters(@Nonnull final Object object, @Nonnull final SetterVisitor visitor) {
    for (final Method method : object.getClass().getDeclaredMethods()) {
      if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 1) {
        final SettingsAccessor accessor = method.getAnnotation(SettingsAccessor.class);
        if (accessor != null) {
          try {
            visitor.visitSettingsValue(accessor.name(), object, method);
          } catch (Exception ex) {
            throw new RuntimeException("Error during set value: " + method, ex);
          }
        }
      }
    }
  }

  private static String makeName(final String fieldName, final Object value, final boolean needConverter) {
    final StringBuilder result = new StringBuilder(fieldName);
    if (value == null) {
      result.append('.');
    }
    if (needConverter) {
      result.append('@');
    }

    return result.toString();
  }

  private static boolean isNull(final String fieldName) {
    return fieldName.indexOf('.') >= 0;
  }

  private static boolean doesNeedConverter(final String fieldName) {
    return fieldName.indexOf('@') >= 0;
  }

  @Nullable
  public String get(@Nonnull final String fieldName) {
    final String storageFieldName = findStorageFieldName(fieldName);
    return storageFieldName == null ? null : this.storage.get(storageFieldName);
  }

  @Nullable
  public String findStorageFieldName(@Nonnull final String fieldName) {
    for (final String k : this.storage.keySet()) {
      if (k.equals(fieldName)) {
        return k;
      } else if (k.startsWith(fieldName) && (k.length() - fieldName.length()) < 3) {
        final String rest = k.substring(fieldName.length());
        boolean onlySpecialChars = true;
        for (int i = 0; i < rest.length(); i++) {
          if (".@".indexOf(rest.charAt(i)) < 0) {
            onlySpecialChars = false;
            break;
          }
        }
        if (onlySpecialChars) {
          return k;
        }
      }
    }
    return null;
  }

  public void fill(@Nonnull final Object instance, @Nullable final Converter converter) {
    visitSetters(instance, (fieldName, instance1, setter) -> {
      try {
        final Class<?> fieldType = setter.getParameterTypes()[0];
        final String storageFieldName = findStorageFieldName(fieldName);
        if (storageFieldName == null) {
          if (converter == null) {
            throw new NullPointerException("Needed converter for non-saved field, to provide default value [" + fieldName + ']');
          } else {
            setter.invoke(instance1, converter.provideDefaultValue(fieldName, fieldType));
          }
        } else {
          final String value = get(storageFieldName);
          final boolean isNull = isNull(storageFieldName);
          final boolean needsConverter = doesNeedConverter(storageFieldName);
          if (isNull) {
            setter.invoke(instance1, new Object[]{null});
          } else if (needsConverter) {
            setter.invoke(instance1, converter.fromString(fieldType, value));
          } else {
            if (fieldType == boolean.class) {
              setter.invoke(instance1, Boolean.parseBoolean(value));
            } else if (fieldType == byte.class) {
              setter.invoke(instance1, Byte.parseByte(value));
            } else if (fieldType == char.class) {
              setter.invoke(instance1, (char) Integer.parseInt(value));
            } else if (fieldType == short.class) {
              setter.invoke(instance1, Short.parseShort(value));
            } else if (fieldType == int.class) {
              setter.invoke(instance1, Integer.parseInt(value));
            } else if (fieldType == long.class) {
              setter.invoke(instance1, Long.parseLong(value));
            } else if (fieldType == float.class) {
              setter.invoke(instance1, Float.intBitsToFloat(Integer.parseInt(value)));
            } else if (fieldType == double.class) {
              setter.invoke(instance1, Double.longBitsToDouble(Long.parseLong(value)));
            } else if (fieldType == String.class) {
              setter.invoke(instance1, value);
            } else {
              throw new Error("Unexpected primitive type [" + fieldName + " " + fieldType + ']');
            }
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't fill field by data [" + fieldName + ']', ex);
        if (ex instanceof RuntimeException) {
          throw (RuntimeException) ex;
        } else {
          throw new Error("Unexpected exception for field processing [" + fieldName + ']', ex);
        }
      }
    });
  }

  public interface Converter {
    @Nullable
    Object fromString(@Nonnull Class<?> fieldType, @Nonnull String value);

    @Nonnull
    String asString(@Nonnull Class<?> fieldType, @Nonnull Object value);

    @Nullable
    Object provideDefaultValue(@Nonnull String fieldName, @Nonnull Class<?> fieldType);
  }

  private interface GetterVisitor {
    void visitSettingsValue(@Nonnull String settingsName, @Nonnull Class<?> fieldType, @Nullable Object fieldValue);
  }

  private interface SetterVisitor {
    void visitSettingsValue(@Nonnull String settingsName, @Nonnull Object instance, @Nonnull Method fieldSetter);
  }
}
