package com.igormaznitsa.sciareto.ui.editors;

import java.util.Locale;
import javax.annotation.Nullable;

public enum PlantUmlSecurityProfile {
  LEGACY,
  UNSECURE,
  INTERNET,
  //ALLOWLIST,
  SANDBOX;

  @Nullable
  public static PlantUmlSecurityProfile findForText(@Nullable String text, @Nullable
  final PlantUmlSecurityProfile defaultProfile) {
    PlantUmlSecurityProfile result = defaultProfile;
    if (text != null) {
      final String normalized = text.trim().toUpperCase(Locale.ENGLISH);
      for (final PlantUmlSecurityProfile p : PlantUmlSecurityProfile.values()) {
        if (p.name().equals(normalized)) {
          result = p;
          break;
        }
      }
    }
    return result;
  }
}
