package jp.gr.java_conf.shygoo.shortcut_maker.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SharedPreferencesUtil {

    private static final String SHARED_PREFERENCES_NAME = "saveData";

    private static final String KEY_PREFIX_COMPONENT_PACKAGE = "componentPackage.";

    private static final String KEY_PREFIX_COMPONENT_CLASS = "componentClass.";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static ComponentName loadDefaultComponent(Context context, String mimeType) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String pkg = sharedPreferences.getString(KEY_PREFIX_COMPONENT_PACKAGE + mimeType, "");
        String cls = sharedPreferences.getString(KEY_PREFIX_COMPONENT_CLASS + mimeType, "");
        if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(cls)) {
            ComponentName component = new ComponentName(pkg, cls);
            if (ComponentUtil.isEnabled(context, component)) {
                return component;
            }
        }
        return null;
    }

    public static void saveDefaultComponent(Context context, String mimeType, ComponentName component) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        if (component == null) {
            editor
                    .remove(KEY_PREFIX_COMPONENT_PACKAGE + mimeType)
                    .remove(KEY_PREFIX_COMPONENT_CLASS + mimeType);
        } else {
            editor
                    .putString(KEY_PREFIX_COMPONENT_PACKAGE + mimeType, component.getPackageName())
                    .putString(KEY_PREFIX_COMPONENT_CLASS + mimeType, component.getClassName());

        }
        editor.apply();
    }
}
