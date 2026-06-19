package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

/**
 * 字节反转加密算法实现
 * 通过反转字节数组顺序实现简单高效的加密
 * 适用于对性能要求极高的场景
 *
 * @author chancey
 * @date 2024/1/12
 **/
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