/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.model;

/**
 * Collection of string names for standard MMD file attributes.
 */
public interface StandardMmdAttributes {
  /**
   * Version of MMD format. <b>Mandatory.</b>
   */
  String MMD_ATTRIBUTE_VERSION = "__version__";
  /**
   * Boolean flag to show internal jumps between topics. <b>Optional.</b>
   */
  String MMD_ATTRIBUTE_SHOW_JUMPS = "showJumps";
  /**
   * Identifier of application generated the file. <b>Optional.</b>
   */
  String MMD_ATTRIBUTE_GENERATOR_ID = "generatorId";
  /**
   * Flag shows that MMD file internal file links were generated without base folder and all internal links should be MMD file folder aware as base folder. <b>Optional.</b>
   */
  String MMD_ATTRIBUTE_NO_BASE_FOLDER = "noBaseFolder";
}
