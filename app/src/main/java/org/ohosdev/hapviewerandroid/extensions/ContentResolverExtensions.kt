package org.ohosdev.hapviewerandroid.extensions

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException

private const val TAG = "ContentResolverExtensions"

/**
 * 获取 `_data` 列的值作为字符串并返回，一般用于获取文件路径。
 * */
@Throws(IOException::class)
fun ContentResolver.getDataColumn(uri: Uri, selection: String? = null, selectionArgs: Array<String>? = null) =
    queryForString(uri, "_data", selection, selectionArgs)

/**
 * 从 ContentResolver 中搜索 `uri`，将`column` 的值作为字符串并返回。
 * @see ContentResolver.query
 * */
fun ContentResolver.queryForString(
    uri: Uri,
    column: String,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String? = runCatching {
    return query(uri, arrayOf(column), selection, selectionArgs, null)?.use {
        if (it.moveToFirst() && !it.isNull(0)) {
            it.getString(0)
        } else {
            null
        }
    }
}.onFailure { it.printStackTrace() }.getOrNull()

fun ContentResolver.getDocumentName(uri: Uri) = queryForString(uri, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
