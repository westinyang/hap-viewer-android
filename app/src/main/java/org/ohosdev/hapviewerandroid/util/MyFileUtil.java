package org.ohosdev.hapviewerandroid.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jesse205.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MyFileUtil {

    /**
     * Android 10+ Uri to File
     * <br>
     * <a href="https://blog.csdn.net/jingzz1/article/details/106188462">android10以上 uri转file uri转真实路径</a>
     *
     * @param ctx
     * @param uri
     * @return File
     */
    // @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    public static File uriToFileApiQ(@NonNull Context ctx, @NonNull Uri uri) throws RuntimeException {
        File file = null;
        // android10以上转换
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_FILE)) {
            file = new File(Objects.requireNonNull(uri.getPath()));
        } else if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            // 把文件复制到沙盒目录
            ContentResolver contentResolver = ctx.getContentResolver();
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                assert cursor != null : new RuntimeException("Cursor is null.");
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    assert index >= 0 : new RuntimeException("index < 0.");
                    String displayName = cursor.getString(index);
                    File externalCacheDir = ctx.getExternalCacheDir();
                    assert externalCacheDir != null : new RuntimeException("cannot find externalCacheDir");
                    File cache = new File(externalCacheDir.getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    try (InputStream is = contentResolver.openInputStream(uri);
                         FileOutputStream fos = new FileOutputStream(cache)
                    ) {
                        assert is != null : new IOException("Cannot open contentResolver.openInputStream.");
                        FileUtil.copyFile(is, fos);
                        file = cache;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return file;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * <br>
     * <a href="https://blog.csdn.net/weixin_40255793/article/details/79496076">Android使用系统文件管理器选择文件，并将Uri转换为File</a>
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @Nullable
    public static String getPath(@NonNull final Context context, @NonNull final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    @Nullable
    public static String getDataColumn(@NonNull Context context, @NonNull Uri uri, @Nullable String selection,
                                       @Nullable String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 仅当有存储权限，且可以获取文件路径时返回原文件，否则返回临时文件
     *
     * @param context 上下文
     * @param uri     Uri
     * @return 获取到的文件路径，或者是复制到的新文件路径
     */
    @Nullable
    public static File getOrCopyFile(@NonNull Context context, @NonNull Uri uri) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                String path = MyFileUtil.getPath(context, uri);
                if (path != null) {
                    File file = new File(path);
                    if (file.exists() && file.canRead())
                        return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return MyFileUtil.uriToFileApiQ(context, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
