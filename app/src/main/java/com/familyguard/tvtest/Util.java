package com.familyguard.tvtest;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

final class Util {
    static final int PORT = 8765;

    private Util() {}

    static String ip() {
        StringBuilder sb = new StringBuilder();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (!a.isLoopbackAddress() && a instanceof Inet4Address) {
                        if (sb.length() > 0) sb.append(" / ");
                        sb.append(a.getHostAddress());
                    }
                }
            }
        } catch (Throwable ignore) {
        }
        return sb.length() == 0 ? "未获取到" : sb.toString();
    }

    static String statusText(Context c) {
        long bootMs = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        String bootStr = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(bootMs));
        boolean overlay = Settings.canDrawOverlays(c);
        boolean a11y = GuardAccessibilityService.instance != null;
        return "设备: " + Build.MANUFACTURER + " " + Build.MODEL
                + "   Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + "\n本次开机时间(估算): " + bootStr
                + "\n悬浮窗权限: " + (overlay ? "已开启" : "未开启")
                + "\n无障碍服务: " + (a11y ? "已连接" : "未连接")
                + "\n守护服务: " + (GuardService.running ? "运行中" : "未运行")
                + "\n收到开机广播次数: " + Store.getInt(c, "boot_count")
                + "\n守护服务被创建次数: " + Store.getInt(c, "svc_created")
                + "\n电视局域网地址: " + ip() + "  端口 " + PORT;
    }
}
