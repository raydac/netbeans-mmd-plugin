package com.igormaznitsa.mindmap.swing.panel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

public class CryptoUtilsTest {

  @Test
  public void testEncrypt() {
    final String encryptedBase64String = CryptoUtils.encrypt("hello", "Hello Crypto-World");
    assertEquals(
        "gJLc5oWXTyeLeu24WhyqdlDFoGMgvuTvTzOx4hdCRx8JYjXMUoziQFR+fyiO3/rtRiiy2BVXTM04CUbp8dkb5A==",
        encryptedBase64String);
  }

  @Test
  public void testDecryptEmpty() {
    final StringBuilder buffer = new StringBuilder();
    assertFalse(CryptoUtils.decrypt("hello", "", buffer));
  }

  @Test
  public void testDecryptOk() {
    final StringBuilder buffer = new StringBuilder();
    assertTrue(CryptoUtils.decrypt("hello",
        "gJLc5oWXTyeLeu24WhyqdlDFoGMgvuTvTzOx4hdCRx8JYjXMUoziQFR+fyiO3/rtRiiy2BVXTM04CUbp8dkb5A==",
        buffer));
    assertEquals("Hello Crypto-World", buffer.toString());
  }

  @Test
  public void testDecryptWrongPass() {
    final StringBuilder buffer = new StringBuilder();
    assertFalse(CryptoUtils.decrypt("hello1",
        "gJLc5oWXTyeLeu24WhyqdlDFoGMgvuTvTzOx4hdCRx8JYjXMUoziQFR+fyiO3/rtRiiy2BVXTM04CUbp8dkb5A==",
        buffer));
    assertFalse(CryptoUtils.decrypt("",
        "gJLc5oWXTyeLeu24WhyqdlDFoGMgvuTvTzOx4hdCRx8JYjXMUoziQFR+fyiO3/rtRiiy2BVXTM04CUbp8dkb5A==",
        buffer));
  }
}