package org.ohosdev.hapviewerandroid.util.dialog

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 快速复写对话框，避免重写非常多方法。
 */
@Suppress("UNCHECKED_CAST")
open class DialogBuilder<T : DialogBuilder<T>> : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun setTitle(titleId: Int) = super.setTitle(titleId) as T

    override fun setTitle(title: CharSequence?) = super.setTitle(title) as T

    override fun setCustomTitle(customTitleView: View?) = super.setCustomTitle(customTitleView) as T

    override fun setMessage(messageId: Int) = super.setMessage(messageId) as T

    override fun setMessage(message: CharSequence?) = super.setMessage(message) as T

    override fun setIcon(iconId: Int) = super.setIcon(iconId) as T

    override fun setIcon(icon: Drawable?) = super.setIcon(icon) as T

    override fun setIconAttribute(attrId: Int) = super.setIconAttribute(attrId) as T

    override fun setPositiveButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ) = super.setPositiveButton(textId, listener) as T

    override fun setPositiveButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ) = super.setPositiveButton(text, listener) as T

    override fun setPositiveButtonIcon(icon: Drawable?) = super.setPositiveButtonIcon(icon) as T

    override fun setNegativeButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ) = super.setNegativeButton(textId, listener) as T

    override fun setNegativeButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ) = super.setNegativeButton(text, listener) as T

    override fun setNegativeButtonIcon(icon: Drawable?) = super.setNegativeButtonIcon(icon) as T

    override fun setNeutralButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ) = super.setNeutralButton(textId, listener) as T

    override fun setNeutralButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ) = super.setNeutralButton(text, listener) as T

    override fun setNeutralButtonIcon(icon: Drawable?) = super.setNeutralButtonIcon(icon) as T

    override fun setCancelable(cancelable: Boolean) = super.setCancelable(cancelable) as T

    override fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?) =
        super.setOnCancelListener(onCancelListener) as T

    override fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?) =
        super.setOnDismissListener(onDismissListener) as T

    override fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?) =
        super.setOnKeyListener(onKeyListener) as T

    override fun setItems(itemsId: Int, listener: DialogInterface.OnClickListener?) =
        super.setItems(itemsId, listener) as T

    override fun setItems(
        items: Array<out CharSequence>?, listener: DialogInterface.OnClickListener?
    ) = super.setItems(items, listener) as T

    override fun setAdapter(adapter: ListAdapter?, listener: DialogInterface.OnClickListener?) =
        super.setAdapter(adapter, listener) as T

    override fun setCursor(
        cursor: Cursor?, listener: DialogInterface.OnClickListener?, labelColumn: String
    ) = super.setCursor(cursor, listener, labelColumn) as T

    override fun setMultiChoiceItems(
        itemsId: Int,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ) = super.setMultiChoiceItems(itemsId, checkedItems, listener) as T

    override fun setMultiChoiceItems(
        items: Array<out CharSequence>?,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ) = super.setMultiChoiceItems(items, checkedItems, listener) as T

    override fun setMultiChoiceItems(
        cursor: Cursor?,
        isCheckedColumn: String,
        labelColumn: String,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ) = super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener) as T

    override fun setSingleChoiceItems(
        itemsId: Int, checkedItem: Int, listener: DialogInterface.OnClickListener?
    ) = super.setSingleChoiceItems(itemsId, checkedItem, listener) as T

    override fun setSingleChoiceItems(
        cursor: Cursor?,
        checkedItem: Int,
        labelColumn: String,
        listener: DialogInterface.OnClickListener?
    ) = super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener) as T

    override fun setSingleChoiceItems(
        items: Array<out CharSequence>?,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ) = super.setSingleChoiceItems(items, checkedItem, listener) as T

    override fun setSingleChoiceItems(
        adapter: ListAdapter?, checkedItem: Int, listener: DialogInterface.OnClickListener?
    ) = super.setSingleChoiceItems(adapter, checkedItem, listener) as T

    override fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener?) =
        super.setOnItemSelectedListener(listener) as T

    override fun setView(layoutResId: Int) = super.setView(layoutResId) as T

    override fun setView(view: View?) = super.setView(view) as T

    override fun setBackground(background: Drawable?) = super.setBackground(background) as T

    override fun setBackgroundInsetStart(backgroundInsetStart: Int) =
        super.setBackgroundInsetStart(backgroundInsetStart) as T

    override fun setBackgroundInsetTop(backgroundInsetTop: Int) =
        super.setBackgroundInsetTop(backgroundInsetTop) as T

    override fun setBackgroundInsetEnd(backgroundInsetEnd: Int) =
        super.setBackgroundInsetEnd(backgroundInsetEnd) as T

    override fun setBackgroundInsetBottom(backgroundInsetBottom: Int) =
        super.setBackgroundInsetBottom(backgroundInsetBottom) as T
}