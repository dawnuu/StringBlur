package com.android.string.plugin.stringblur;

interface IString {
    String encrypt(String data, String key);

    String decrypt(String data, String key);

    boolean overflow(String data);
}
