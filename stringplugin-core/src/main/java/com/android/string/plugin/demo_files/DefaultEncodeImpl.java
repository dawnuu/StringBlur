package com.android.string.plugin.demo_files;
//package applicationId.stringblur;

import java.nio.charset.StandardCharsets;
import com.android.string.plugin.IString;

/**
 * 字符串加解密实现类
 *
 * @author chancey
 * @date 2024/1/12   12:08
 **/
public final class DefaultEncodeImpl implements IString {
    @Override
    public byte[] encrypt(byte[] data, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        int lenKey = keyBytes.length;
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] + keyBytes[j]);
            j++;
        }
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        int lenKey = key.length;
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] - key[j]);
            j++;
        }
        return data;
    }
}
