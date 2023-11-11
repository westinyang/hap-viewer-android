package org.ohosdev.hapviewerandroid.util

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("UNCHECKED_CAST")
open class DialogBuilder<T : MaterialAlertDialogBuilder> : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun setTitle(titleId: Int): T {
        return super.setTitle(titleId) as T
    }

    override fun setTitle(title: CharSequence?): T {
        return super.setTitle(title) as T
    }

    override fun setCustomTitle(customTitleView: View?): T {
        return super.setCustomTitle(customTitleView) as T
    }

    override fun setMessage(messageId: Int): T {
        return super.setMessage(messageId) as T
    }

    override fun setMessage(message: CharSequence?): T {
        return super.setMessage(message) as T
    }

    override fun setIcon(iconId: Int): T {
        return super.setIcon(iconId) as T
    }

    override fun setIcon(icon: Drawable?): T {
        return super.setIcon(icon) as T
    }

    override fun setIconAttribute(attrId: Int): T {
        return super.setIconAttribute(attrId) as T
    }

    override fun setPositiveButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setPositiveButton(textId, listener) as T
    }

    override fun setPositiveButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setPositiveButton(text, listener) as T
    }

    override fun setPositiveButtonIcon(icon: Drawable?): T {
        return super.setPositiveButtonIcon(icon) as T
    }

    override fun setNegativeButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setNegativeButton(textId, listener) as T
    }

    override fun setNegativeButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setNegativeButton(text, listener) as T
    }

    override fun setNegativeButtonIcon(icon: Drawable?): T {
        return super.setNegativeButtonIcon(icon) as T
    }

    override fun setNeutralButton(
        textId: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setNeutralButton(textId, listener) as T
    }

    override fun setNeutralButton(
        text: CharSequence?, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setNeutralButton(text, listener) as T
    }

    override fun setNeutralButtonIcon(icon: Drawable?): T {
        return super.setNeutralButtonIcon(icon) as T
    }

    override fun setCancelable(cancelable: Boolean): T {
        return super.setCancelable(cancelable) as T
    }

    override fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?): T {
        return super.setOnCancelListener(onCancelListener) as T
    }

    override fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?): T {
        return super.setOnDismissListener(onDismissListener) as T
    }

    override fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): T {
        return super.setOnKeyListener(onKeyListener) as T
    }

    override fun setItems(
        itemsId: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setItems(itemsId, listener) as T
    }

    override fun setItems(
        items: Array<out CharSequence>?, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setItems(items, listener) as T
    }

    override fun setAdapter(
        adapter: ListAdapter?, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setAdapter(adapter, listener) as T
    }

    override fun setCursor(
        cursor: Cursor?, listener: DialogInterface.OnClickListener?, labelColumn: String
    ): T {
        return super.setCursor(cursor, listener, labelColumn) as T
    }

    override fun setMultiChoiceItems(
        itemsId: Int,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): T {
        return super.setMultiChoiceItems(itemsId, checkedItems, listener) as T
    }

    override fun setMultiChoiceItems(
        items: Array<out CharSequence>?,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): T {
        return super.setMultiChoiceItems(items, checkedItems, listener) as T
    }

    override fun setMultiChoiceItems(
        cursor: Cursor?,
        isCheckedColumn: String,
        labelColumn: String,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): T {
        return super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener) as T
    }

    override fun setSingleChoiceItems(
        itemsId: Int, checkedItem: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setSingleChoiceItems(itemsId, checkedItem, listener) as T
    }

    override fun setSingleChoiceItems(
        cursor: Cursor?,
        checkedItem: Int,
        labelColumn: String,
        listener: DialogInterface.OnClickListener?
    ): T {
        return super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener) as T
    }

    override fun setSingleChoiceItems(
        items: Array<out CharSequence>?,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ): T {
        return super.setSingleChoiceItems(items, checkedItem, listener) as T
    }

    override fun setSingleChoiceItems(
        adapter: ListAdapter?, checkedItem: Int, listener: DialogInterface.OnClickListener?
    ): T {
        return super.setSingleChoiceItems(adapter, checkedItem, listener) as T
    }

    override fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener?): T {
        return super.setOnItemSelectedListener(listener) as T
    }

    override fun setView(layoutResId: Int): T {
        return super.setView(layoutResId) as T
    }

    override fun setView(view: View?): T {
        return super.setView(view) as T
    }

    override fun setBackground(background: Drawable?): T {
        return super.setBackground(background) as T
    }

    override fun setBackgroundInsetStart(backgroundInsetStart: Int): T {
        return super.setBackgroundInsetStart(backgroundInsetStart) as T
    }

    override fun setBackgroundInsetTop(backgroundInsetTop: Int): T {
        return super.setBackgroundInsetTop(backgroundInsetTop) as T
    }

    override fun setBackgroundInsetEnd(backgroundInsetEnd: Int): T {
        return super.setBackgroundInsetEnd(backgroundInsetEnd) as T
    }

    override fun setBackgroundInsetBottom(backgroundInsetBottom: Int): T {
        return super.setBackgroundInsetBottom(backgroundInsetBottom) as T
    }
}