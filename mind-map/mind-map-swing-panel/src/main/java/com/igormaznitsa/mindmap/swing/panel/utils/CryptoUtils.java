package com.igormaznitsa.mindmap.swing.panel.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import net.iharder.Base64;

public final class CryptoUtils {
  private CryptoUtils() {

  }

  @Nonnull
  public static byte[] sha256(@Nonnull final byte[] data) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(data);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Nonnull
  public static String encrypt(@Nullable final String pass, @Nonnull final String text) {
    if (pass == null || pass.isEmpty()) {
      return text;
    }
    try {
      final byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
      final byte[] textHash = sha256(textBytes);

      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      outputStream.write(textHash);
      outputStream.write(textBytes);

      final byte[] originalData = outputStream.toByteArray();

      final byte[] key = sha256(pass.getBytes(StandardCharsets.UTF_8));
      Key aesKey = new SecretKeySpec(key, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      final byte[] encodedData = cipher.doFinal(originalData);
      if (Arrays.equals(originalData, encodedData)) {
        throw new IllegalStateException(
            "Data can't be encrypted! Check encryption provider and settings!");
      }
      return Base64.encodeBytes(encodedData);
    } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static boolean decrypt(@Nonnull final String pass, @Nonnull final String text,
                                @Nonnull final StringBuilder output) {
    try {
      final byte[] key = sha256(pass.getBytes(StandardCharsets.UTF_8));
      final Key aesKey = new SecretKeySpec(key, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, aesKey);
      final byte[] decrypted = cipher.doFinal(Base64.decode(text));
      if (decrypted.length < 32) {
        return false;
      }
      final byte[] sha256data = Arrays.copyOfRange(decrypted, 0, 32);
      final byte[] textPart = Arrays.copyOfRange(decrypted, 32, decrypted.length);
      final byte[] calculatedHash = sha256(textPart);
      if (!Arrays.equals(sha256data, calculatedHash)) {
        return false;
      }
      output.append(new String(textPart, StandardCharsets.UTF_8));
      return true;
    } catch (BadPaddingException ex) {
      return false;
    } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
