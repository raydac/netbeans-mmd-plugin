package com.igormaznitsa.ideamindmap.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class DeclaredFieldsSerializer implements Serializable {

  private static final long serialVersionUID = -92387498231123L;

  private static final Logger LOGGER = Logger.getInstance(DeclaredFieldsSerializer.class);

  @Property
  private final Map<String, String> storage = new TreeMap<String, String>(new Comparator<String>() {
    @Override public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  });

  public interface Converter {
    @Nullable Object fromString(@NotNull Class<?> fieldType, @NotNull String value);

    @NotNull String asString(@NotNull Class<?> fieldType, @NotNull Object value);

    @Nullable Object provideDefaultValue(@NotNull String fieldName, @NotNull Class<?> fieldType);
  }

  private interface FieldVisitor {
    void visitField(@NotNull Object instance, @NotNull Field field, @NotNull String fieldName, @NotNull Class<?> fieldType);
  }

  public DeclaredFieldsSerializer() {
  }

  private static void visitFields(@NotNull final Object object, @NotNull final FieldVisitor visitor) {
    for (final Field f : object.getClass().getDeclaredFields()) {
      if ((f.getModifiers() & (Modifier.FINAL | Modifier.NATIVE | Modifier.STATIC | Modifier.TRANSIENT)) == 0) {
        f.setAccessible(true);
        visitor.visitField(object, f, f.getName(), f.getType());
      }
    }
  }

  private static String makeName(final String fieldName, final Object value, final boolean needConverter) {
    final StringBuilder result = new StringBuilder(fieldName);
    if (value == null)
      result.append('.');
    if (needConverter)
      result.append('@');

    return result.toString();
  }

  public DeclaredFieldsSerializer(@NotNull final Object object, @Nullable final Converter converter) {
    visitFields(object, new FieldVisitor() {
      @Override public void visitField(@NotNull Object instance, @NotNull Field field, @NotNull String fieldName, @NotNull Class<?> fieldType) {
        try {
          final Object value = field.get(instance);

          if (fieldType.isPrimitive()) {
            if (fieldType == float.class) {
              storage.put(makeName(fieldName,value,false),Integer.toString((Float.floatToIntBits((Float)value))));
            }
            else if (fieldType == double.class) {
              storage.put(makeName(fieldName,value,false),Long.toString((Double.doubleToLongBits((Double)value))));
            }
            else
              storage.put(makeName(fieldName, value, false), value.toString());
          }
          else if (fieldType == String.class) {
            storage.put(makeName(fieldName, value, false), value == null ? "" : (String) value);
          }
          else {
            if (converter == null) {
              throw new NullPointerException("Unexpected field type " + fieldType.getName() + ", provide converter!");
            }
            else {
              final String converted = value == null ? null : converter.asString(fieldType, value);
              storage.put(makeName(fieldName, converted, true), converted);
            }
          }
        }
        catch (Exception ex) {
          LOGGER.error("Can't make data for field [" + fieldName + ']');
          if (ex instanceof RuntimeException) {
            throw ((RuntimeException) ex);
          }
          else
            throw new Error("Can't serialize field [" + fieldName + ']', ex);
        }
      }
    });
  }

  @Nullable
  public String get(@NotNull final String fieldName) {
    final String storageFieldName = findStorageFieldName(fieldName);
    return storageFieldName == null ? null : this.storage.get(storageFieldName);
  }

  @Nullable
  public String findStorageFieldName(@NotNull final String fieldName) {
    for (final String k : this.storage.keySet()) {
      if (k.equals(fieldName)) {
        return k;
      }
      else if (k.startsWith(fieldName) && (k.length() - fieldName.length()) < 3) {
        final String rest = k.substring(fieldName.length());
        boolean onlySpecialChars = true;
        for (int i = 0; i < rest.length(); i++) {
          if (".@".indexOf(rest.charAt(i)) < 0) {
            onlySpecialChars = false;
            break;
          }
        }
        if (onlySpecialChars)
          return k;
      }
    }
    return null;
  }

  private static boolean isNull(final String fieldName) {
    return fieldName.indexOf('.') >= 0;
  }

  private static boolean doesNeedConverter(final String fieldName) {
    return fieldName.indexOf('@') >= 0;
  }

  public void fill(@NotNull final Object instance, @Nullable final Converter converter) {
    visitFields(instance, new FieldVisitor() {
      @Override public void visitField(@NotNull Object instance, @NotNull Field field, @NotNull String fieldName, @NotNull Class<?> fieldType) {
        try {
          final String storageFieldName = findStorageFieldName(fieldName);
          if (storageFieldName == null) {
            if (converter == null) {
              throw new NullPointerException("Needed converter for non-saved field, to provide default value [" + fieldName + ']');
            }
            else {
              field.set(instance, converter.provideDefaultValue(fieldName, fieldType));
            }
          }
          else {
            final String value = get(storageFieldName);
            final boolean isNull = isNull(storageFieldName);
            final boolean needsConverter = doesNeedConverter(storageFieldName);
            if (isNull) {
              field.set(instance, null);
            }
            else if (needsConverter) {
              field.set(instance, converter.fromString(fieldType, value));
            }
            else {
              if (fieldType == boolean.class) {
                field.set(instance, Boolean.parseBoolean(value));
              }
              else if (fieldType == byte.class) {
                field.set(instance, Byte.parseByte(value));
              }
              else if (fieldType == char.class) {
                field.set(instance, (char) Integer.parseInt(value));
              }
              else if (fieldType == short.class) {
                field.set(instance, Short.parseShort(value));
              }
              else if (fieldType == int.class) {
                field.set(instance, Integer.parseInt(value));
              }
              else if (fieldType == long.class) {
                field.set(instance, Long.parseLong(value));
              }
              else if (fieldType == float.class) {
                field.set(instance, Float.intBitsToFloat(Integer.parseInt(value)));
              }
              else if (fieldType == double.class) {
                field.set(instance, Double.longBitsToDouble(Long.parseLong(value)));
              }
              else if (fieldType == String.class) {
                field.set(instance, value);
              }
              else
                throw new Error("Unexpected primitive type [" + fieldName + " " + fieldType + ']');
            }
          }
        }
        catch (Exception ex) {
          LOGGER.error("Can't fill field by data [" + fieldName + ']');
          if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
          }
          else {
            throw new Error("Unexpected exception for field processing [" + field + ']', ex);
          }
        }
      }
    });
  }
}
