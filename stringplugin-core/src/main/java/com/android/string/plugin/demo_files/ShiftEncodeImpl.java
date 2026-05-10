package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

public final class ShiftEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        return shift(data, key.getBytes(), 1);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return shift(data, key, -1);
    }

    private byte[] shift(byte[] data, byte[] key, int direction) {
        int lenKey = key.length;
        for (int i = 0; i < data.length; i++) {
            int offset = key[i % lenKey] & 0x0F;
            data[i] = (byte) (data[i] + direction * offset);
        }
        return data;
    }
}