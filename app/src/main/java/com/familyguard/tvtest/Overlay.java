package com.familyguard.tvtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

final class Overlay {
    private static View current;
    private static WindowManager currentWm;

    private Overlay() {}

    static synchronized void hide() {
        if (current != null && currentWm != null) {
            try {
                currentWm.removeView(current);
            } catch (Throwable ignore) {
            }
        }
        current = null;
        currentWm = null;
    }

    static synchronized void show(final Context ctx, int type, final int seconds, final String label) {
        try {
            hide();
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            TextView v = new TextView(ctx);
            v.setBackgroundColor(Color.BLACK);
            v.setTextColor(Color.WHITE);
            v.setTextSize(40);
            v.setGravity(Gravity.CENTER);
            v.setText("锁屏遮罩测试\n(" + label + ")\n" + seconds + " 秒后自动消失");
            v.setFocusable(true);
            v.setFocusableInTouchMode(true);
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Store.log(ctx, "遮罩收到按键: " + KeyEvent.keyCodeToString(keyCode));
                    }
                    return true;
                }
            });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    type,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.OPAQUE);
            wm.addView(v, lp);
            v.requestFocus();
            current = v;
            currentWm = wm;
            Store.log(ctx, "遮罩已显示(" + label + ")");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    hide();
                    Store.log(ctx, "遮罩已移除(" + label + ")");
                }
            }, seconds * 1000L);
        } catch (Throwable t) {
            Store.log(ctx, "遮罩显示失败(" + label + "): " + t);
        }
    }
}
