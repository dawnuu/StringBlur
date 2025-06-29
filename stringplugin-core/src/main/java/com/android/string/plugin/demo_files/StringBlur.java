package com.android.string.plugin.demo_files;
//package applicationId.stringblur;

/**
 * @author chancey
 * @date 2025/6/19
 **/
public final class StringBlur {
    private static final DefaultEncodeImpl IMPL = new DefaultEncodeImpl();
//    private static final XorEncodeImpl IMPL = new XorEncodeImpl();

    public static String decrypt(String value, String key) {
        return IMPL.decryptString(value, key);
    }

    public static String decrypt(byte[] value, byte[] key) {
        return IMPL.decryptBytes(value, key);
    }
}

