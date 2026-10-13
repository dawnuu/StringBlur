package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

import java.nio.charset.StandardCharsets;

/**
 * 轻量流式混淆：用整条 key 的 UTF-8 字节经 FNV-1a 生成 32 位种子，
 * LCG 每步取高 8 位与数据异或。Java int 溢出提供模 2^32 运算。
 * LCG 可预测且有周期，不提供密码学安全性。
 */
public final class LcgStreamEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        if (!hasContent(data) || key == null) return data;
        return xor(data, key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        return xor(data, key);
    }

    private byte[] xor(byte[] data, byte[] key) {
        if (!hasContent(data) || key == null || key.length == 0) return data;

        int state = 0x811c9dc5;
        for (byte value : key) {
            state = (state ^ (value & 0xFF)) * 0x01000193;
        }
        for (int i = 0; i < data.length; i++) {
            state = state * 1664525 + 1013904223;
            data[i] = (byte) (data[i] ^ (state >>> 24));
        }
        return data;
    }
}
