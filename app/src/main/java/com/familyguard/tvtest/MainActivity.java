package com.familyguard.tvtest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0F172A"));

        TextView title = new TextView(this);
        title.setText("安装测试成功");
        title.setTextColor(Color.WHITE);
        title.setTextSize(48);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("品牌: " + Build.MANUFACTURER
                + "\n型号: " + Build.MODEL
                + "\nAndroid 版本: " + Build.VERSION.RELEASE
                + "\nAPI 级别: " + Build.VERSION.SDK_INT
                + "\nCPU: " + Build.SUPPORTED_ABIS[0]);
        info.setTextColor(Color.parseColor("#94A3B8"));
        info.setTextSize(24);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 48, 0, 0);
        root.addView(info);

        setContentView(root);
    }
}
