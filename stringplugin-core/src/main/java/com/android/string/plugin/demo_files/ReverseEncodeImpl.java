package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

public final class ReverseEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        return reverse(data);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return reverse(data);
    }

    private byte[] reverse(byte[] data) {
        int left = 0;
        int right = data.length - 1;
        while (left < right) {
            byte temp = data[left];
            data[left] = data[right];
            data[right] = temp;
            left++;
            right--;
        }
        return data;
    }
}