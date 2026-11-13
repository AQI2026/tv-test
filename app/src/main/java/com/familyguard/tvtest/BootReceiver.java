package com.familyguard.tvtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        Store.incr(c, "boot_count");
        Store.log(c, "收到广播: " + (i == null ? "null" : i.getAction()));
        try {
            c.startForegroundService(new Intent(c, GuardService.class));
            Store.log(c, "开机启动守护服务: 请求已发出");
        } catch (Throwable t) {
            Store.log(c, "开机启动守护服务失败: " + t);
        }
        try {
            Intent m = new Intent(c, MainActivity.class);
            m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(m);
            Store.log(c, "开机拉起界面: 请求已发出");
        } catch (Throwable t) {
            Store.log(c, "开机拉起界面失败: " + t);
        }
    }
}
