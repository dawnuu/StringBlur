package com.android.string.plugin;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

/**
 * @author chancey
 * @date 2024/1/12   12:03
 **/
public interface IString {
    default byte[] encryptBytes(String data, String key) {
        return Base64.encode(encrypt(data.getBytes(StandardCharsets.UTF_8), key), Base64.NO_WRAP);
    }

    default String encryptString(String data, String key) {
        return new String(encryptBytes(data, key), StandardCharsets.UTF_8);
    }

    default String decryptBytes(byte[] data, byte[] key) {
        return new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key), StandardCharsets.UTF_8);
    }

    default String decryptString(String data, String key) {
        return decryptBytes(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
    }

    default boolean overflow(byte[] data) {
        return data != null && data.length != 0;
    }

    byte[] encrypt(byte[] data, String key);

    byte[] decrypt(byte[] data, byte[] key);
}
