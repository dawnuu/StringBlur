package com.android.string.plugin.files;

import android.util.Base64;

/**
 * @author chancey
 * @date 2024/1/12   12:08
 **/
public class StringEncodeImpl extends IString {
    @Override
    public byte[] encryptBytes(String data, String key) {
        return Base64.encode(encrypt(data.getBytes(), key), Base64.NO_WRAP);
    }

    @Override
    public String encryptString(String data, String key) {
        return new String(encryptBytes(data, key));
    }

    @Override
    public String decryptBytes(byte[] data, byte[] key) {
        return new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key));
    }

    @Override
    public String decryptString(String data, String key) {
        return decryptBytes(Base64.decode(data, Base64.NO_WRAP), key.getBytes());
    }
}
