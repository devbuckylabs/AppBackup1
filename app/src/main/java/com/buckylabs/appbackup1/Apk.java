package com.buckylabs.appbackup1;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class Apk {

    private String AppName;
    private Drawable AppIcon;

    public Apk(String appName, Drawable appIcon) {
        AppName = appName;
        AppIcon = appIcon;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public Drawable getAppIcon() {
        return AppIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        AppIcon = appIcon;
    }



}
