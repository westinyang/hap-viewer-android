package org.ohosdev.hapviewerandroid.util;

import android.app.Dialog;
import android.widget.TextView;

/**
 * 对话框工具类
 *
 * @author Jesse205
 */
public class DialogUtil {
    /**
     * 设置对话框内容是否可选
     * @param dialog 对话框
     */
    public static void setDialogContentSelectable(Dialog dialog,boolean selectable) {
        TextView textView = ((TextView) dialog.findViewById(android.R.id.message));
        textView.setTextIsSelectable(selectable);
    }
}
