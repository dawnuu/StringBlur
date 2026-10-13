package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

import java.nio.charset.StandardCharsets;

/**
 * 链式 XOR 混淆：C[i] = P[i] ^ key[i % key.length] ^ C[i-1]，初始前值为 0。
 * 解密时保存当前密文字节，再原地还原明文，供下一字节使用。
 */
public final class XorChainEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        if (!hasContent(data) || key == null) return data;
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length == 0) return data;

        int previous = 0;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length] ^ previous);
            previous = data[i];
        }
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (!hasContent(data) || key == null || key.length == 0) return data;

        int previous = 0;
        for (int i = 0; i < data.length; i++) {
            int current = data[i];
            data[i] = (byte) (current ^ key[i % key.length] ^ previous);
            previous = current;
        }
        return data;
    }
}
