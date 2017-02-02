package jp.gr.java_conf.shygoo.shortcut_maker.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

public class ComponentUtil {

    public static ActivityInfo getActivityInfo(Context context, ComponentName component) {
        PackageManager manager = context.getPackageManager();
        try {
            return manager.getActivityInfo(component, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isEnabled(Context context, ComponentName component) {
        return getActivityInfo(context, component) != null;
    }

    public static CharSequence getName(Context context, ComponentName component) {
        ActivityInfo activityInfo = getActivityInfo(context, component);
        if (activityInfo == null) {
            return null;
        }
        return activityInfo.loadLabel(context.getPackageManager());
    }
}
