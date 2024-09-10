/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ksrender;

import static com.igormaznitsa.ksrender.KdRenderUtils.makePumlMultiline;
import static com.igormaznitsa.ksrender.KdRenderUtils.unicodeString;
import static com.igormaznitsa.ksrender.PartitioningFlag.INHERITED;
import static com.igormaznitsa.ksrender.PartitioningFlag.MAY_ON;
import static com.igormaznitsa.ksrender.PartitioningFlag.ON;

import java.util.regex.Pattern;

public enum KStreamType {
  SOURCE(".*source.*", ".*kstream.*source.*", "node \"%s\" as %s %s",
      INHERITED),
  PRINT("", ".*kstream.*printer.*", "file \"%s\" as %s",
      INHERITED),
  SINK(".*sink.*", ".*kstream.*sink.*", "node \"%s\" as %s %s",
      INHERITED),
  PEEK("", ".*kstream.*peek.*", "file \"%s\" as %s %s", INHERITED),
  FOREACH("", ".*kstream.*foreach.*", "node \"%s\" as %s %s",
      INHERITED),

  PROCESSOR("", ".*kstream.*processor.*", "node \"%s\" as %s %s",
      INHERITED),
  TRANSFORMVALUES("", ".*kstream.*transformvalues.*", "cloud \"%s\" as %s %s",
      INHERITED),
  TRANSFORM("", ".*kstream.*transform.*", "cloud \"%s\" as %s %s",
      ON),

  KEY_SELECT("", ".*kstream.*key.*select.*", "rectangle \"%s\" as %s %s",
      ON),

  FLATMAPVALUES("", ".*kstream.*flatmapvalues.*", "rectangle \"%s\" as %s %s",
      INHERITED),
  FLATMAP("", ".*kstream.*flatmap.*", "rectangle \"%s\" as %s %s",
      ON),
  FILTER("", ".*kstream.*filter.*", "rectangle \"%s\" as %s %s",
      INHERITED),

  MAPVALUES("", ".*kstream.*mapvalues.*", "rectangle \"%s\" as %s %s",
      INHERITED),
  MAP("", ".*kstream.*map.*", "rectangle \"%s\" as %s %s", ON),
  MERGE("", ".*kstream.*merge.*", "usecase \"%s\" as %s %s",
      MAY_ON),

  WINDOWED("", ".*kstream.*windowed.*", "frame \"%s\" as %s %s",
      INHERITED),
  JOINTHIS("", ".*kstream.*jointhis.*", "usecase \"%s\" as %s %s",
      MAY_ON),
  JOINOTHER("", ".*kstream.*joinother.*", "usecase \"%s\" as %s %s",
      MAY_ON),
  JOIN("", ".*kstream.*join.*", "usecase \"%s\" as %s %s", MAY_ON),

  OUTERTHIS("", ".*kstream.*outerthis.*", "usecase \"%s\" as %s %s",
      MAY_ON),
  OUTEROTHER("", ".*kstream.*outerother.*", "usecase \"%s\" as %s %s",
      MAY_ON),

  BRANCHCHILD("", ".*kstream.*branchchild.*", "usecase \"%s\" as %s %s",
      INHERITED),
  BRANCH("", ".*kstream.*branch.*", "usecase \"%s\" as %s %s",
      INHERITED);

  private final Pattern patternType;
  private final Pattern patternId;
  private final String pumlPattern;
  private final PartitioningFlag partitioning;

  KStreamType(final String patternType,
              final String patternId,
              final String pumlPattern,
              final PartitioningFlag partitioning
  ) {
    this.partitioning = partitioning;
    this.patternId = Pattern.compile(patternId, Pattern.CASE_INSENSITIVE);
    this.patternType = Pattern.compile(patternType, Pattern.CASE_INSENSITIVE);
    this.pumlPattern = pumlPattern;
  }

  public static KStreamType find(final KStreamsTopologyDescriptionParser.TopologyElement element) {
    for (final KStreamType t : KStreamType.values()) {
      if (t.patternType.matcher(element.type).matches()) {
        return t;
      }
      if (t.patternId.matcher(element.id).matches()) {
        return t;
      }
    }
    return FILTER;
  }

  public PartitioningFlag getPartitioning() {
    return this.partitioning;
  }

  public String makePuml(
      final KStreamsTopologyDescriptionParser.TopologyElement element,
      final String alias) {
    final String color;
    switch (this.partitioning) {
      case ON:
        color = "#FFDEDE";
        break;
      case MAY_ON:
        color = "#FFFFBB";
        break;
      case INHERITED:
        color = "#BBFFBB";
        break;
      default:
        color = "#BBBBFF";
        break;
    }
    return String.format(this.pumlPattern,
        makePumlMultiline(unicodeString(element.id), 0), alias, color);
  }
}
