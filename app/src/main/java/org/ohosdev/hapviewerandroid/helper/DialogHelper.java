package org.ohosdev.hapviewerandroid.helper;

import android.app.Dialog;
import android.text.method.MovementMethod;
import android.widget.TextView;

/**
 * 对话框工具类
 *
 * @author Jesse205
 */
public class DialogHelper {
    /**
     * 设置对话框内容是否可选
     *
     * @param dialog 对话框
     */
    public static void setContentSelectable(Dialog dialog, boolean selectable) {
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextIsSelectable(selectable);
    }

    public static void setContentAutoLinkMask(Dialog dialog, int mask) {
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setLinksClickable(true);
        textView.setAutoLinkMask(mask);
    }

    public static void setContentMovementMethod(Dialog dialog, MovementMethod movement) {
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setMovementMethod(movement);
    }
}
