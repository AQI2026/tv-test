package com.familyguard.tvtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private TextView logView;
    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refresh();
            h.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.parseColor("#0F172A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 40, 60, 40);
        sv.addView(root);

        TextView title = new TextView(this);
        title.setText("TV能力检测 v0.2");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        root.addView(title);

        status = new TextView(this);
        status.setTextColor(Color.parseColor("#E2E8F0"));
        status.setTextSize(18);
        status.setPadding(0, 20, 0, 20);
        root.addView(status);

        Button first = addBtn(root, "1  授予悬浮窗权限", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri pkg = Uri.parse("package:" + getPackageName());
                tryOpen("悬浮窗权限页",
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg),
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkg));
            }
        });
        addBtn(root, "2  打开无障碍设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryOpen("无障碍设置页",
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                        new Intent(Settings.ACTION_SETTINGS));
            }
        });
        addBtn(root, "3  启动守护服务", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startForegroundService(new Intent(MainActivity.this, GuardService.class));
                    Store.log(MainActivity.this, "手动启动守护服务: 请求已发出");
                } catch (Throwable t) {
                    Store.log(MainActivity.this, "手动启动守护服务失败: " + t);
                }
            }
        });
        addBtn(root, "4  10秒后显示黑色遮罩（悬浮窗方式）", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlayTest(0);
            }
        });
        addBtn(root, "5  10秒后显示黑色遮罩（无障碍方式）", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlayTest(1);
            }
        });
        addBtn(root, "6  5秒后无障碍“回主页”测试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store.log(MainActivity.this, "5 秒后将执行无障碍回主页");
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GuardAccessibilityService s = GuardAccessibilityService.instance;
                        if (s == null) {
                            Store.log(MainActivity.this, "回主页失败: 无障碍服务未连接");
                        } else {
                            boolean ok = s.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME);
                            Store.log(MainActivity.this, "无障碍回主页结果: " + ok);
                        }
                    }
                }, 5000);
            }
        });
        addBtn(root, "7  刷新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        TextView logTitle = new TextView(this);
        logTitle.setText("\n事件记录（最新在最下面；手机浏览器打开电视地址也能看到）");
        logTitle.setTextColor(Color.parseColor("#94A3B8"));
        logTitle.setTextSize(16);
        root.addView(logTitle);

        logView = new TextView(this);
        logView.setTextColor(Color.parseColor("#CBD5E1"));
        logView.setTextSize(14);
        logView.setTypeface(Typeface.MONOSPACE);
        root.addView(logView);

        setContentView(sv);
        first.requestFocus();
    }

    private Button addBtn(LinearLayout parent, String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 12;
        parent.addView(b, lp);
        return b;
    }

    private void tryOpen(String name, Intent... candidates) {
        for (Intent i : candidates) {
            try {
                startActivity(i);
                Store.log(this, "已打开" + name + " (" + i.getAction() + ")");
                return;
            } catch (Throwable t) {
                Store.log(this, "打不开" + name + " (" + i.getAction() + "): " + t.getClass().getSimpleName());
            }
        }
        Store.log(this, name + ": 全部方式都打不开");
    }

    private void overlayTest(int kind) {
        try {
            Intent i = new Intent(this, GuardService.class);
            i.setAction(GuardService.ACTION_OVERLAY);
            i.putExtra("kind", kind);
            i.putExtra("delay", 10);
            startForegroundService(i);
        } catch (Throwable t) {
            Store.log(this, "遮罩测试启动失败: " + t);
        }
    }

    private void refresh() {
        status.setText(Util.statusText(this));
        logView.setText(Store.getLog(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.post(ticker);
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(ticker);
        super.onPause();
    }
}
