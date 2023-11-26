package com.android.string.plugin.stringblur;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * 用于内部加解密
 */
public class StringEncodeImpl implements IString {
    private static final String CHARSET_NAME_UTF_8 = "utf-8";

    @Override
    public String encrypt(String data, String key) {
        String newData;
        try {
            newData = new String(Base64.encode(encrypt(data.getBytes(CHARSET_NAME_UTF_8), key), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            newData = new String(Base64.encode(encrypt(data.getBytes(), key), Base64.NO_WRAP));
        }
        return newData;
    }

    @Override
    public String decrypt(String data, String key) {
        String newData;
        try {
            newData = new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key), CHARSET_NAME_UTF_8);
        } catch (UnsupportedEncodingException e) {
            newData = new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key));
        }
        return newData;
    }

    @Override
    public boolean overflow(String data, String key) {
        return data != null && !data.isBlank();
    }

    private static byte[] encrypt(byte[] data, String key) {
        int lenKey = key.length();
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] + key.charAt(j));
            j++;
        }
        return data;
    }

    private static byte[] decrypt(byte[] data, String key) {
        int lenKey = key.length();
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] - key.charAt(j));
            j++;
        }
        return data;
    }
}
