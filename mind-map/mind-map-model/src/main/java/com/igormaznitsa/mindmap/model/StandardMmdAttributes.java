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
