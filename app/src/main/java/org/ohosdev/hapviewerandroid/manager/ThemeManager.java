package org.ohosdev.hapviewerandroid.manager;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.ohosdev.hapviewerandroid.R;

/**
 * @author Jesse205
 */
public class ThemeManager {
    public static final String THEME_STYLE = "theme_style";
    private static final String TAG = "ThemeManager";
    @NonNull
    private static ThemeStyle defaultThemeStyle = getPlatformThemeStyle();

    private static ThemeStyle appThemeStyle;
    private final ContextThemeWrapper context;
    private final ThemeStyle themeStyle;

    public ThemeManager(ContextThemeWrapper context) {
        this.context = context;
        if (appThemeStyle == null)
            appThemeStyle = getAppThemeStyle(context);
        themeStyle = appThemeStyle;
    }

    /**
     * 设置软件全局主题风格
     *
     * @param context    上下文
     * @param themeStyle 主题配色
     */
    public static void setAppThemeStyle(Context context, ThemeStyle themeStyle) {
        appThemeStyle = themeStyle;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(THEME_STYLE, themeStyle.name());
        editor.apply();
    }

    /**
     * 获取软件全局主题风格
     *
     * @param context 上下文
     * @return 主题风格
     */
    public static ThemeStyle getAppThemeStyle(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeStyleName = sharedPreferences.getString(THEME_STYLE, null);

        if (themeStyleName == null) {
            return defaultThemeStyle;
        } else {
            try {
                return ThemeStyle.valueOf(themeStyleName);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException: " + e);
                return defaultThemeStyle;
            }
        }
    }

    public static int getThemeId(ThemeStyle themeStyle) {
        switch (themeStyle) {
            case Material1:
                return R.style.Theme_HapViewerAndroid;
            case Material2:
                return R.style.Theme_HapViewerAndroid_Material2;
            case Material3:
                return R.style.Theme_HapViewerAndroid_Material3;
            case Harmony:
                return R.style.Theme_HapViewerAndroid_Harmony;
        }
        return 0;
    }

    public static ThemeStyle getPlatformThemeStyle() {
        return ThemeStyle.Harmony;
        /* if (SDK_INT >= Build.VERSION_CODES.S) {
            return ThemeStyle.Material3;
        } else if (SDK_INT >= Build.VERSION_CODES.P) {
            return ThemeStyle.Material2;
        } else
            return ThemeStyle.Material1; */
    }

    public void applyTheme() {
        int themeId = getThemeId(themeStyle);
        context.setTheme(themeId);
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();

            TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{
                    R.attr.windowLightStatusBar,
                    R.attr.windowLightNavigationBar
            });

            boolean windowLightStatusBar = array.getBoolean(0, false);
            boolean windowLightNavigationBar = array.getBoolean(1, false);
            array.recycle();

            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(window, window.getDecorView());

            windowInsetsController.setAppearanceLightStatusBars(windowLightStatusBar);
            window.setNavigationBarColor(0);
            if (SDK_INT >= Build.VERSION_CODES.O) {
                windowInsetsController.setAppearanceLightNavigationBars(windowLightNavigationBar);
                window.setNavigationBarColor(0);
            }
        }
    }

    public boolean checkThemeChanged() {
        return appThemeStyle != themeStyle;
    }

    public ThemeStyle getThemeStyle() {
        return themeStyle;
    }

    public enum ThemeStyle {
        Material1, Material2, Material3, Harmony
    }
}
