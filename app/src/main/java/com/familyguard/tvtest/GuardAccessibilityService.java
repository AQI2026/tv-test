package com.familyguard.tvtest;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class GuardAccessibilityService extends AccessibilityService {
    static volatile GuardAccessibilityService instance;
    private String lastPkg = "";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Store.log(this, "无障碍服务已连接");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String p = event.getPackageName().toString();
            if (!p.equals(lastPkg)) {
                lastPkg = p;
                Store.log(this, "前台窗口: " + p);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        Store.log(this, "无障碍服务已断开");
        return super.onUnbind(intent);
    }
}
