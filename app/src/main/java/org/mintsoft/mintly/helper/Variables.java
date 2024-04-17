package org.mintsoft.mintly.helper;

import java.util.ArrayList;
import java.util.HashMap;

public class Variables {
    private static HashMap<String, ArrayList<HashMap<String, String>>> arrayHash = new HashMap<>();
    private static HashMap<String, String> hashData = new HashMap<>();
    private static final HashMap<String, String> pHashData = new HashMap<>();
    public static boolean isLive = false;
    public static String locale = null;

    public static ArrayList<HashMap<String, String>> getArrayHash(String key) {
        return arrayHash.containsKey(key) ? arrayHash.get(key) : null;
    }

    public static void setArrayHash(String key, ArrayList<HashMap<String, String>> value) {
        arrayHash.put(key, value);
    }

    public static String getHash(String key) {
        return hashData.containsKey(key) ? hashData.get(key) : null;
    }

    public static void setHash(String key, String value) {
        hashData.put(key, value);
    }

    public static String getPHash(String key) {
        return pHashData.containsKey(key) ? pHashData.get(key) : null;
    }

    public static void setPHash(String key, String value) {
        pHashData.put(key, value);
    }

    public static void reset() {
        arrayHash = new HashMap<>();
        hashData = new HashMap<>();
    }
}