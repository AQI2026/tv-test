package com.familyguard.tvtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GuardService extends Service {
    static final String ACTION_OVERLAY = "com.familyguard.tvprobe.OVERLAY";
    static volatile boolean running = false;

    private Handler handler;
    private volatile ServerSocket server;
    private BroadcastReceiver screenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        handler = new Handler(Looper.getMainLooper());
        Store.incr(this, "svc_created");
        Store.log(this, "守护服务已创建");
        ensureForeground();
        startHttp();

        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Store.log(context, "系统广播: " + intent.getAction());
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_USER_PRESENT);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, f, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(screenReceiver, f);
        }
    }

    private void ensureForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel("guard", "守护服务", NotificationManager.IMPORTANCE_LOW);
        nm.createNotificationChannel(ch);
        Notification n = new Notification.Builder(this, "guard")
                .setContentTitle("TV能力检测运行中")
                .setContentText("端口 " + Util.PORT)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }
    }

    private void startHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(Util.PORT);
                    Store.log(GuardService.this, "局域网服务已监听 " + Util.PORT);
                    while (!server.isClosed()) {
                        Socket s = server.accept();
                        try {
                            s.setSoTimeout(2000);
                            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            String line;
                            while ((line = br.readLine()) != null && !line.isEmpty()) {
                                // 只读取请求头，不做处理
                            }
                            String body = "TV能力检测服务运行中\n\n" + Util.statusText(GuardService.this)
                                    + "\n\n---- 事件记录 ----\n" + Store.getLog(GuardService.this) + "\n";
                            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                            OutputStream os = s.getOutputStream();
                            String head = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: "
                                    + bytes.length + "\r\nConnection: close\r\n\r\n";
                            os.write(head.getBytes(StandardCharsets.UTF_8));
                            os.write(bytes);
                            os.flush();
                        } catch (Throwable ignore) {
                        } finally {
                            try {
                                s.close();
                            } catch (Throwable ignore2) {
                            }
                        }
                    }
                } catch (Throwable t) {
                    Store.log(GuardService.this, "局域网服务异常: " + t);
                }
            }
        }, "probe-http").start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensureForeground();
        if (intent != null && ACTION_OVERLAY.equals(intent.getAction())) {
            final int kind = intent.getIntExtra("kind", 0);
            final int delay = intent.getIntExtra("delay", 10);
            Store.log(this, delay + " 秒后显示遮罩(" + (kind == 0 ? "悬浮窗" : "无障碍") + ")");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (kind == 0) {
                        if (Settings.canDrawOverlays(GuardService.this)) {
                            Overlay.show(GuardService.this, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, 10, "悬浮窗");
                        } else {
                            Store.log(GuardService.this, "遮罩失败: 没有悬浮窗权限");
                        }
                    } else {
                        GuardAccessibilityService s = GuardAccessibilityService.instance;
                        if (s != null) {
                            Overlay.show(s, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, 10, "无障碍");
                        } else {
                            Store.log(GuardService.this, "遮罩失败: 无障碍服务未连接");
                        }
                    }
                }
            }, delay * 1000L);
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Store.log(this, "界面被划掉(onTaskRemoved)");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        running = false;
        Store.log(this, "守护服务被销毁");
        try {
            if (server != null) server.close();
        } catch (Throwable ignore) {
        }
        try {
            unregisterReceiver(screenReceiver);
        } catch (Throwable ignore) {
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
