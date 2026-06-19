package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

/**
 * 位移加密算法实现
 * 根据密钥的低位值对字节进行位移操作
 * 适合中等安全性和性能要求的场景
 *
 * @author chancey
 * @date 2024/1/12
 **/
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