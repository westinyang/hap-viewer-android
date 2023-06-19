package org.ohosdev.hapviewerandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.ohosdev.hapviewerandroid.model.HapInfo;
import org.ohosdev.hapviewerandroid.util.HapUtil;
import org.ohosdev.hapviewerandroid.util.MyFileUtil;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long exitTime = 0;
    public static HapInfo currentHapInfo = null;

    // 文件读写权限
    private static final String[] PERMISSIONS_EXTERNAL_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 文件读写权限 请求码
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 禁用横屏
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int successCount = 0;
        for (int i = 0; i < permissions.length; i++) {
            Log.i(TAG, "申请权限：" + permissions[i] + "，申请结果：" + grantResults[i]);
            if (grantResults[i] == 0) {
                successCount++;
            }
        }
        if (successCount == permissions.length) {
            if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE) {
                selectFile();
            }
        } else {
            // Snackbar.make(getWindow().getDecorView(), "权限申请失败", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void aboutClick(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于")
                .setMessage("HAP查看器 for Android\n\n" +
                        "软件版本：" + BuildConfig.VERSION_NAME + "\n" +
                        "软件作者：westinyang\n" +
                        "开源仓库：https://gitee.com/ohos-dev/hap-viewer-android\n" +
                        "企鹅群组：752399947")
                .setPositiveButton("关闭", (dialog, id) -> {
                    dialog.cancel();
                })
                .setCancelable(false)
                .show();
    }

    public void fabClick(View view) {
        // 申请权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_EXTERNAL_STORAGE, REQUEST_CODE_EXTERNAL_STORAGE);
        } else {
            selectFile();
        }
    }

    @SuppressWarnings("deprecation")
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "未找到文件管理应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == 1) {
            Uri uri = data.getData();
            File file = null;
            // Android 10+ 把文件复制到沙箱内
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                file = MyFileUtil.uriToFileApiQ(this, uri);
            }
            // Android 10 以下获取文件真实路径，创建File
            else {
                String path = MyFileUtil.getPath(this, uri);
                if (path != null) {
                    file = new File(path);
                }
            }
            if (file == null) {
                Toast.makeText(this, "文件获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            // 解析hap
            String path = file.getAbsolutePath();
            String extName = path.substring(path.lastIndexOf(".") + 1);
            if (path.length() > 0 && "hap".equals(extName)) {
                parseHapAndShowInfo(path);
            } else {
                Toast.makeText(this, "请选择一个hap安装包", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 解析hap并显示信息
     * @param hapFilePath
     */
    private void parseHapAndShowInfo(String hapFilePath) {
        // 解析hap
        HapInfo hapInfo;
        try {
            hapInfo = HapUtil.parse(hapFilePath);
            currentHapInfo = hapInfo;

            ImageView imageView = findViewById(R.id.imageView);
            TextView textView1 = findViewById(R.id.textView1);
            TextView textView2 = findViewById(R.id.textView2);
            TextView textView3 = findViewById(R.id.textView3);
            TextView textView4 = findViewById(R.id.textView4);
            TextView textView5 = findViewById(R.id.textView5);
            TextView textView6 = findViewById(R.id.textView6);

            // 显示信息
            imageView.setImageBitmap(hapInfo.icon);
            textView1.setText(hapInfo.appName);
            textView2.setText(hapInfo.packageName);
            textView3.setText(hapInfo.versionName);
            textView4.setText(hapInfo.versionCode);
            textView5.setText(String.format("API%s (%s)", hapInfo.targetAPIVersion, hapInfo.apiReleaseType));
            textView6.setText(hapInfo.getTechDesc());
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            //Toast.makeText(this, "hap文件解析失败（目前仅支持API9+(Stage模型)编译的安装包）", Toast.LENGTH_LONG).show();
            Snackbar.make(getWindow().getDecorView(), "hap文件解析失败，目前仅支持解析 API9+ (Stage模型) 的应用安装包", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void itemClick(View view) {
        TextView key = view.findViewWithTag("key");
        TextView val = view.findViewWithTag("val");
        String k = String.valueOf(key.getText());
        k = k.replace("：", "");
        String v = String.valueOf(val.getText());
        //获取剪切板管理器
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        //设置内容到剪切板
        cm.setPrimaryClip(ClipData.newPlainText(null, v));
        Toast.makeText(this, "已复制 " + k, Toast.LENGTH_SHORT).show();
    }
}