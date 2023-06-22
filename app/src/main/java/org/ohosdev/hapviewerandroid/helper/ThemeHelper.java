package org.ohosdev.hapviewerandroid.helper;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.content.res.TypedArray;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.ohosdev.hapviewerandroid.R;

/**
 * 主题辅助类
 *
 * @author Jesse205
 */
public class ThemeHelper {

    public static void fixSystemBarsAppearance(Activity activity) {

        TypedArray array = activity.getTheme().obtainStyledAttributes(new int[]{R.attr.windowLightStatusBar, R.attr.windowLightNavigationBar});
        boolean windowLightStatusBar = array.getBoolean(0, false);
        boolean windowLightNavigationBar = array.getBoolean(1, false);
        array.recycle();

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());

        windowInsetsController.setAppearanceLightStatusBars(windowLightStatusBar);
        if (SDK_INT >= 26 && windowLightNavigationBar) {
            windowInsetsController.setAppearanceLightNavigationBars(true);
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.system_window_scrim,activity.getTheme()));
        }
    }
}
