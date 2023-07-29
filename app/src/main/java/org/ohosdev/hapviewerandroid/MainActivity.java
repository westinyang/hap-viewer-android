package org.ohosdev.hapviewerandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.onegravity.rteditor.RTEditorMovementMethod;

import org.ohosdev.hapviewerandroid.adapter.InfoAdapter;
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding;
import org.ohosdev.hapviewerandroid.helper.DialogHelper;
import org.ohosdev.hapviewerandroid.manager.ThemeManager;
import org.ohosdev.hapviewerandroid.model.HapInfo;
import org.ohosdev.hapviewerandroid.util.BitmapUtil;
import org.ohosdev.hapviewerandroid.util.HapUtil;
import org.ohosdev.hapviewerandroid.util.MyFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.insets.WindowInsetsHelper;
import rikka.layoutinflater.view.LayoutInflaterFactory;

public class MainActivity extends AppCompatActivity implements View.OnDragListener {

    private static final String TAG = "MainActivity";
    // 文件读写权限
    private static final String[] PERMISSIONS_EXTERNAL_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // private static final String KEY_NOW_URI = "now_uri";
    // 文件读写权限 请求码
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    // public static HapInfo currentHapInfo = null;
    private ThemeManager themeManager;
    // private long exitTime = 0;
    private InfoAdapter infoAdapter;
    private ActivityMainBinding binding;
    private OnExitCallback onExitCallback;
    private MainViewModel model;
    /* @Nullable
    private Uri nowUri = null; */
    private final ActivityResultLauncher<String> selectFileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), this::parse);

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLayoutInflater().setFactory2(new LayoutInflaterFactory(getDelegate())
                .addOnViewCreatedListener(WindowInsetsHelper.getLISTENER()));
        super.onCreate(savedInstanceState);

        themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        onExitCallback = new OnExitCallback();
        getOnBackPressedDispatcher().addCallback(this, onExitCallback);


        infoAdapter = new InfoAdapter(this);
        RecyclerView recyclerView = binding.detailInfo.recyclerView;
        recyclerView.setAdapter(infoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 启用拖放
        binding.dropMask.getRoot().setOnDragListener(this);

        binding.floatingActionButton.setOnClickListener(this::onFabClick);

        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 禁用横屏
        // 禁用横屏会导致平板与折叠屏用户体验不佳。应用目前的布局对横屏已经非常友好，取消禁用并无大碍

        // 解析传入的 Intent
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            parse(intent.getData());
        }

        model = new ViewModelProvider(this).get(MainViewModel.class);
        model.getHapInfo().observe(this, this::onHapInfoChanged);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        switch (themeManager.getThemeStyle()) {
            case Material1:
                menu.findItem(R.id.action_theme_material1).setChecked(true);
                break;
            case Material2:
                menu.findItem(R.id.action_theme_material2).setChecked(true);
                break;
            case Material3:
                menu.findItem(R.id.action_theme_material3).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            handelAboutClick(item);
        } else if (itemId == R.id.action_theme_material1) {
            ThemeManager.setAppThemeStyle(this, ThemeManager.ThemeStyle.Material1);
            checkTheme();
        } else if (itemId == R.id.action_theme_material2) {
            ThemeManager.setAppThemeStyle(this, ThemeManager.ThemeStyle.Material2);
            checkTheme();
        } else if (itemId == R.id.action_theme_material3) {
            ThemeManager.setAppThemeStyle(this, ThemeManager.ThemeStyle.Material3);
            checkTheme();
        }
        return true;
    }

    // 由于引入了ViewModel和LiveData，就不需要以这种方式保存数据了
    /* @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nowUri != null)
            outState.putString(KEY_NOW_URI, nowUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String nowUriString = savedInstanceState.getString(KEY_NOW_URI);
        if (nowUriString != null) {
            parse(Uri.parse(nowUriString));
        }
    } */

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
            Snackbar.make(binding.getRoot(), R.string.permission_grant_fail, Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.floatingActionButton)
                    .show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Snackbar可能不会显示，也就不会重新启用，这时候就需要在重新进入应用时启用一下二次返回。
        onExitCallback.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 退出时可能会显示Snackbar，以至于下一次弹出多个snackbar
        if (onExitCallback.snackbar != null) {
            onExitCallback.snackbar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 因为用了LiveData，所以不应该在这里销毁
        /* if (currentHapInfo != null) {
            HapUtil.destroyHapInfo(this, currentHapInfo);
        } */
    }

    public void handelAboutClick(MenuItem item) {
        // 使用 Material Dialog
        // 但是华为设备上拖拽阴影在 Material Dialog 有bug
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        AlertDialog alertDialog = builder.setTitle(R.string.about)
                .setMessage("")
                .setPositiveButton(android.R.string.ok, null)
                .show();
        DialogHelper.setContentSelectable(alertDialog, true);
        DialogHelper.setContentAutoLinkMask(alertDialog, Linkify.WEB_URLS);
        DialogHelper.setContentMovementMethod(alertDialog, RTEditorMovementMethod.getInstance());
        alertDialog.setMessage(String.format(getString(R.string.about_message), BuildConfig.VERSION_NAME));
    }

    public void onFabClick(View view) {
        // 申请权限
        // 安卓10及以上不需要存储权限，可以直接使用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_EXTERNAL_STORAGE, REQUEST_CODE_EXTERNAL_STORAGE);
        } else {
            selectFile();
        }
    }

    public void selectFile() {
        selectFileResultLauncher.launch("*/*");
    }

    private void parse(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            synchronized (this) {
                runOnUiThread(() -> binding.progressBar.setVisibility(View.VISIBLE));
                File file = MyFileUtil.getOrCopyFile(MainActivity.this, uri);
                if (file == null) {
                    Snackbar.make(binding.getRoot(), R.string.parse_error_fail_obtain, Snackbar.LENGTH_SHORT)
                            .setAnchorView(R.id.floatingActionButton)
                            .show();
                    runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
                    return;
                }
                // 解析hap
                String path = file.getAbsolutePath();
                String extName = path.substring(path.lastIndexOf(".") + 1);
                if (path.length() > 0 && "hap".equals(extName)) {
                    parseHapAndShowInfo(path, uri);
                } else {
                    AtomicBoolean continueFlag = new AtomicBoolean(false);
                    Snackbar.make(binding.getRoot(), R.string.parse_error_type, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.parse_continue_ignoreError, v -> {
                                continueFlag.set(true);
                                parseHapAndShowInfo(path, uri);
                            })
                            .setAnchorView(R.id.floatingActionButton)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    // 不继续解析，说明此文件没用了
                                    if (!continueFlag.get()) {
                                        MyFileUtil.deleteExternalCacheFile(MainActivity.this, path);
                                    }
                                }
                            })
                            .show();
                }
                runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
            }
        }).start();
    }

    private void checkTheme() {
        if (themeManager.checkThemeChanged()) {
            recreate();
        }
    }

    /**
     * 解析hap并显示信息
     *
     * @param hapFilePath
     * @param uri
     */
    private void parseHapAndShowInfo(@NonNull String hapFilePath, @NonNull Uri uri) {
        // 解析hap
        HapInfo hapInfo;
        runOnUiThread(() -> binding.progressBar.setVisibility(View.VISIBLE));
        try {
            hapInfo = HapUtil.parse(hapFilePath);

            runOnUiThread(() -> model.getHapInfo().setValue(hapInfo));
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Snackbar.make(binding.getRoot(), R.string.parse_error_fail, Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.floatingActionButton)
                    .show();
        }
        // 到此为止，这个临时文件没用了，可以删掉了
        MyFileUtil.deleteExternalCacheFile(this, hapFilePath);
        runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
    }


    /**
     * 创建阴影 Bitmap
     *
     * @param src 原始 Bitmap
     * @return 阴影Bitmap
     */
    private Bitmap newShadowBitmap(Bitmap src) {
        return BitmapUtil.newShadowBitmap(this, src,
                this.getResources().getDimensionPixelSize(R.dimen.icon_padding),
                this.getResources().getDimensionPixelSize(R.dimen.icon_width),
                this.getResources().getDimensionPixelSize(R.dimen.icon_width));
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                for (int i = 0; i < event.getClipDescription().getMimeTypeCount(); i++) {
                    if (!event.getClipDescription().getMimeType(i).equals(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        v.setAlpha(1);
                        return true;
                    }
                }
                return false;
            }
            case DragEvent.ACTION_DROP: {
                for (int i = 0; i < event.getClipData().getItemCount(); i++) {
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    if (item.getUri() != null) {
                        requestDragAndDropPermissions(event);
                        parse(item.getUri());
                        break;
                    }
                }
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                v.setAlpha(0);
                break;
            }
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parse(intent.getData());
    }

    private void onHapInfoChanged(HapInfo hapInfo) {
        if (hapInfo != null) {
            // 显示基础信息
            binding.basicInfo.appName.setText(hapInfo.appName);
            binding.basicInfo.version.setText(String.format("%s (%s)", hapInfo.versionName, hapInfo.versionCode));
            // 显示应用图标
            if (hapInfo.icon != null) {
                binding.basicInfo.imageView.setImageBitmap(hapInfo.icon);
                binding.basicInfo.imageView.setBackground(new BitmapDrawable(getResources(), newShadowBitmap(hapInfo.icon)));
            } else {
                BitmapDrawable defaultIconDrawable = (BitmapDrawable) Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_default_new));
                binding.basicInfo.imageView.setBackground(new BitmapDrawable(getResources(),
                        newShadowBitmap(defaultIconDrawable.getBitmap())));
            }

            // 显示应用信息
            infoAdapter.setInfo(hapInfo);
        } else {
            infoAdapter.setInfo(new HapInfo(true));
        }
    }

    // https://developer.android.google.cn/topic/libraries/architecture/viewmodel?hl=zh-cn
    public static class MainViewModel extends ViewModel {
        private MutableLiveData<HapInfo> hapInfo;

        public MutableLiveData<HapInfo> getHapInfo() {
            if (hapInfo == null) {
                hapInfo = new MutableLiveData<>(new HapInfo(true));
            }
            return hapInfo;
        }

        @Override
        protected void onCleared() {
            super.onCleared();
            HapInfo hapInfoValue = hapInfo.getValue();
            if (hapInfoValue != null && hapInfoValue.icon != null) {
                hapInfoValue.icon.recycle();
            }
        }
    }

    private class OnExitCallback extends OnBackPressedCallback {
        @Nullable
        private Snackbar snackbar;

        public OnExitCallback() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            OnExitCallback.this.setEnabled(false);
            snackbar = Snackbar.make(binding.getRoot(), R.string.exit_toast, Snackbar.LENGTH_SHORT);
            snackbar.setAnchorView(R.id.floatingActionButton);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    OnExitCallback.this.setEnabled(true);
                }
            });
            snackbar.show();
        }
    }
}