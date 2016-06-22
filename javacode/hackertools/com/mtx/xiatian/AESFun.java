package com.mtx.xiatian;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESFun {
	 
    String a = "";//略去;
    String b = "";//略去;
    String c = "";//略去;

    Cipher cipher;
    IvParameterSpec spec;
    SecretKeySpec secretKeySpec;
    void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
          spec = new IvParameterSpec(b.getBytes());
          secretKeySpec = new SecretKeySpec(a.getBytes(), "AES");
          cipher = Cipher.getInstance("AES/CBC/NoPadding");
    }

    public final String b(String arg7) throws Exception {
      byte[] array_b1;
      byte[] array_b = null;
      int i = 2;
      String string = null;
      {
          try {
              this.cipher.init(2, secretKeySpec, spec);
              Cipher cipher = this.cipher;
              if(arg7 != null && arg7.length() >= i) {
                  int i1 = arg7.length() / 2;
                  array_b = new byte[i1];
                  int i2;
                  for(i2 = 0; i2 < i1; ++i2) {
                      String string1 = arg7.substring(i2 * 2, i2 * 2 + 2);
                      array_b[i2] = ((byte)Integer.parseInt(string1, 0x10));
                  }
              }

              array_b1 = cipher.doFinal(array_b);
          }
          catch(Exception exception) {
              StringBuilder stringBuilder = new StringBuilder("[decrypt] ");
              string = exception.getMessage();
              StringBuilder stringBuilder1 = stringBuilder.append(string);
              string = stringBuilder1.toString();
              exception.printStackTrace();
              throw new Exception(string);
          }

          string = new String(array_b1);
      }

      return string;
  }
}