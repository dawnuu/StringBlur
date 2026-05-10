package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

public final class XorShiftEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        return shift(xor(data, key.getBytes()), key.getBytes(), 1);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return xor(shift(data, key, -1), key);
    }

    private byte[] xor(byte[] data, byte[] key) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return data;
    }

    private byte[] shift(byte[] data, byte[] key, int direction) {
        for (int i = 0; i < data.length; i++) {
            int offset = key[i % key.length] & 0x0F;
            data[i] = (byte) (data[i] + direction * offset);
        }
        return data;
    }
}