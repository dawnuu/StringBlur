package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

import java.nio.charset.StandardCharsets;

/**
 * 基于位旋转与异或的逐字节变换：每个字节的旋转位数和异或掩码都由 key 对应位置的
 * 字节决定，因此用满整条 key，而不是整段只用一个固定旋转位数。encrypt / decrypt 互逆。
 *
 * @author chancey
 * @date 2026/6/19
 **/
public final class FastRotEncodeImpl implements IString {

    @Override
    public byte[] encrypt(byte[] data, String key) {
        if (!hasContent(data) || key == null) return data;
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length == 0) return data;

        int keyLen = keyBytes.length;
        for (int i = 0; i < data.length; i++) {
            int k = keyBytes[i % keyLen] & 0xFF;
            // 先异或掩码，再按 key 字节低 3 位(0-7)左旋
            data[i] = rotateLeft((byte) (data[i] ^ k), k & 7);
        }
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (!hasContent(data) || key == null || key.length == 0) return data;

        int keyLen = key.length;
        for (int i = 0; i < data.length; i++) {
            int k = key[i % keyLen] & 0xFF;
            // 逆序：先右旋复位，再异或还原
            data[i] = (byte) (rotateRight(data[i], k & 7) ^ k);
        }
        return data;
    }

    /**
     * 向左旋转指定位数（0-7）
     */
    private byte rotateLeft(byte value, int positions) {
        int v = value & 0xFF;
        return (byte) ((v << positions) | (v >>> (8 - positions)));
    }

    /**
     * 向右旋转指定位数（0-7）
     */
    private byte rotateRight(byte value, int positions) {
        int v = value & 0xFF;
        return (byte) ((v >>> positions) | (v << (8 - positions)));
    }
}
