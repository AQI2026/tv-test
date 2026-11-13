package com.familyguard.tvtest;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class Store {
    private static final String PREF = "probe";

    private Store() {}

    static synchronized void log(Context c, String msg) {
        SharedPreferences sp = c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String old = sp.getString("log", "");
        String line = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "  " + msg;
        String[] arr = old.isEmpty() ? new String[0] : old.split("\n");
        int start = Math.max(0, arr.length - 59);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            sb.append(arr[i]).append('\n');
        }
        sb.append(line);
        sp.edit().putString("log", sb.toString()).commit();
    }

    static synchronized String getLog(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("log", "(空)");
    }

    static synchronized int incr(Context c, String key) {
        SharedPreferences sp = c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        int v = sp.getInt(key, 0) + 1;
        sp.edit().putInt(key, v).commit();
        return v;
    }

    static synchronized int getInt(Context c, String key) {
        return c.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(key, 0);
    }
}
