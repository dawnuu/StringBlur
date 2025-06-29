package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

/**
 * @author chancey
 * @date 2025/6/29
 **/
public class XorEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        return xor(data, key.getBytes());
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return xor(data, key);
    }

    private byte[] xor(byte[] data, byte[] key) {
        int len = data.length;
        int lenKey = key.length;
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key[j]);
            i++;
            j++;
        }
        return data;
    }
}
