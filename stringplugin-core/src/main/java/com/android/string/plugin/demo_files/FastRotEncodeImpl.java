package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

/**
 * 快速旋转加密算法
 * 基于位旋转的高性能加密算法
 *
 * @author chancey
 * @date 2026/6/19
 **/
public final class FastRotEncodeImpl implements IString {

    @Override
    public byte[] encrypt(byte[] data, String key) {
        if (!overflow(data) || key == null) return data;
        
        int rotation = Math.abs(key.hashCode()) % 7 + 1; // 1-7位的旋转
        
        for (int i = 0; i < data.length; i++) {
            data[i] = rotateLeft(data[i], rotation);
        }
        
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (!overflow(data) || key == null) return data;
        
        int rotation = Math.abs(new String(key).hashCode()) % 7 + 1;
        
        for (int i = 0; i < data.length; i++) {
            data[i] = rotateRight(data[i], rotation);
        }
        
        return data;
    }
    
    /**
     * 向左旋转指定位数
     */
    private byte rotateLeft(byte value, int positions) {
        return (byte) (((value & 0xFF) << positions) | ((value & 0xFF) >>> (8 - positions)));
    }
    
    /**
     * 向右旋转指定位数
     */
    private byte rotateRight(byte value, int positions) {
        return (byte) (((value & 0xFF) >>> positions) | ((value & 0xFF) << (8 - positions)));
    }
}