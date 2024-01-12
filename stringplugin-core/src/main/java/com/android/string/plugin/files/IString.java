package com.android.string.plugin.files;

/**
 * @author chancey
 * @date 2024/1/12   12:03
 **/
abstract class IString {

    public abstract byte[] encryptBytes(String data, String key);

    public abstract String encryptString(String data, String key);

    public abstract String decryptBytes(byte[] data, byte[] key);

    public abstract String decryptString(String data, String key);

    public boolean overflow(byte[] data) {
        return data != null && data.length != 0;
    }

    protected byte[] encrypt(byte[] data, String key) {
        int lenKey = key.length();
        int j = 0;
        for (int i = 0; i < data.length; i++) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] + key.charAt(j));
            j++;
        }
        return data;
    }

    protected byte[] decrypt(byte[] data, byte[] key) {
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
