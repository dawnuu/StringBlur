package com.android.string.plugin.demo_files;

import com.android.string.plugin.IString;

/**
 * SIMD优化的批量XOR加密算法
 * 针对性能进行了特别优化，适合处理大量字符串
 *
 * @author chancey
 * @date 2026/6/19
 **/
public final class XorSimdEncodeImpl implements IString {

    @Override
    public byte[] encrypt(byte[] data, String key) {
        if (!overflow(data) || key == null) return data;
        
        byte[] keyBytes = key.getBytes();
        if (keyBytes.length == 0) return data;
        
        // SIMD风格的批量处理优化
        int dataLen = data.length;
        int keyLen = keyBytes.length;
        
        // 使用long类型进行批量处理（8字节一组）
        int longLen = dataLen / 8;
        if (longLen > 0 && keyLen >= 8) {
            long[] dataLong = new long[longLen];
            long[] keyPattern = generateKeyPattern(keyBytes);
            
            // 批量处理8字节块
            for (int i = 0; i < longLen; i++) {
                int offset = i * 8;
                long dataChunk = ((long) data[offset] & 0xFF) |
                               (((long) data[offset + 1] & 0xFF) << 8) |
                               (((long) data[offset + 2] & 0xFF) << 16) |
                               (((long) data[offset + 3] & 0xFF) << 24) |
                               (((long) data[offset + 4] & 0xFF) << 32) |
                               (((long) data[offset + 5] & 0xFF) << 40) |
                               (((long) data[offset + 6] & 0xFF) << 48) |
                               (((long) data[offset + 7] & 0xFF) << 56);
                
                long encrypted = dataChunk ^ keyPattern[i % keyPattern.length];
                
                data[offset] = (byte) (encrypted & 0xFF);
                data[offset + 1] = (byte) ((encrypted >> 8) & 0xFF);
                data[offset + 2] = (byte) ((encrypted >> 16) & 0xFF);
                data[offset + 3] = (byte) ((encrypted >> 24) & 0xFF);
                data[offset + 4] = (byte) ((encrypted >> 32) & 0xFF);
                data[offset + 5] = (byte) ((encrypted >> 40) & 0xFF);
                data[offset + 6] = (byte) ((encrypted >> 48) & 0xFF);
                data[offset + 7] = (byte) ((encrypted >> 56) & 0xFF);
            }
        }
        
        // 处理剩余字节（传统XOR）
        for (int i = longLen * 8; i < dataLen; i++) {
            data[i] = (byte) (data[i] ^ keyBytes[i % keyLen]);
        }
        
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        // XOR解密与加密相同，但需要处理key为byte[]的情况
        if (!overflow(data) || key == null) return data;
        return encrypt(data, new String(key));
    }
    
    /**
     * 生成8字节的key模式用于批量XOR
     */
    private long[] generateKeyPattern(byte[] keyBytes) {
        int patternCount = Math.max(1, 256 / keyBytes.length);
        long[] patterns = new long[patternCount];
        
        for (int i = 0; i < patternCount; i++) {
            long pattern = 0;
            for (int j = 0; j < 8; j++) {
                pattern |= ((long) keyBytes[(i * 8 + j) % keyBytes.length] & 0xFF) << (j * 8);
            }
            patterns[i] = pattern;
        }
        
        return patterns;
    }
}