package org.ohosdev.hapviewerandroid.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.DragEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnDragListener
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.onegravity.rteditor.RTEditorMovementMethod
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.adapter.InfoAdapter
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.app.BaseActivity
import org.ohosdev.hapviewerandroid.app.hasFileMime
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding
import org.ohosdev.hapviewerandroid.extensions.applyDividerIfEnabled
import org.ohosdev.hapviewerandroid.extensions.contentMovementMethod
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.setContentAutoLinkMask
import org.ohosdev.hapviewerandroid.extensions.thisApp
import org.ohosdev.hapviewerandroid.manager.ThemeManager
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.BitmapUtil
import org.ohosdev.hapviewerandroid.util.HapUtil
import org.ohosdev.hapviewerandroid.util.MyFileUtil
import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : BaseActivity(), OnDragListener {
    private val themeManager: ThemeManager = ThemeManager(this)

    private val infoAdapter = InfoAdapter(this)

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val rootView: CoordinatorLayout get() = binding.root

    private val onExitCallback by lazy { OnExitCallback() }
    private val model: MainViewModel by viewModels()

    private val selectFileResultLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { parse(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.factory2 = LayoutInflaterFactory(delegate)
            .addOnViewCreatedListener(WindowInsetsHelper.LISTENER)
        super.onCreate(savedInstanceState)
        themeManager.applyTheme()
        initViews()

        onBackPressedDispatcher.addCallback(this, onExitCallback)

        // 解析传入的 Intent
        if (savedInstanceState == null) {
            parse(intent.data)
        }

        model.hapInfo.observe(this) { onHapInfoChanged(it) }
        model.isParsing.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun initViews() {
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.detailInfo.recyclerView.apply {
            adapter = infoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            applyDividerIfEnabled()
        }

        // 启用拖放
        binding.dropMask.root.setOnDragListener(this)
        binding.floatingActionButton.setOnClickListener { onFabClick(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        when (thisApp.appPreference.themeType) {
            MATERIAL1 -> menu.findItem(R.id.action_theme_material1)
            MATERIAL2 -> menu.findItem(R.id.action_theme_material2)
            MATERIAL3 -> menu.findItem(R.id.action_theme_material3)
            HARMONY -> menu.findItem(R.id.action_theme_harmony)
        }.isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val itemId = item.itemId
        if (itemId == R.id.action_about) {
            handelAboutClick(item)
        } else if (itemId == R.id.action_theme_material1) {
            thisApp.appPreference.themeType = MATERIAL1
            checkTheme()
        } else if (itemId == R.id.action_theme_material2) {
            thisApp.appPreference.themeType = MATERIAL2
            checkTheme()
        } else if (itemId == R.id.action_theme_material3) {
            thisApp.appPreference.themeType = MATERIAL3
            checkTheme()
        } else if (itemId == R.id.action_theme_harmony) {
            thisApp.appPreference.themeType = HARMONY
            checkTheme()
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (i in permissions.indices) {
            Log.i(TAG, "申请权限：" + permissions[i] + "，申请结果：" + grantResults[i])
        }
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            if (requestCode == REQUEST_CODE_SELECT_FILE) {
                selectFile()
            }
        } else {
            showSnackBar(R.string.permission_grant_fail)
        }
    }

    override fun onResume() {
        super.onResume()
        // Snackbar 可能不会显示，也就不会重新启用，这时候就需要在重新进入应用时启用一下二次返回。
        onExitCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()

        // 退出时可能会显示Snackbar，以至于下一次弹出多个snackbar
        onExitCallback.closeSnackBar()
    }


    private fun handelAboutClick(item: MenuItem?) {
        // 使用 Material Dialog
        // 但是华为设备上拖拽阴影在 Material Dialog 有bug
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage("")
            .setPositiveButton(android.R.string.ok, null)
            .show().apply {
                contentSelectable = true
                setContentAutoLinkMask(Linkify.WEB_URLS)
                setMessage(getString(R.string.about_message, BuildConfig.VERSION_NAME))
                setContentAutoLinkMask(0)
                contentMovementMethod = RTEditorMovementMethod.getInstance()
            }
    }

    private fun onFabClick(view: View?) {
        // 申请权限
        // 安卓10及以上不需要存储权限，可以直接使用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                PERMISSIONS_EXTERNAL_STORAGE,
                REQUEST_CODE_SELECT_FILE
            )
        } else {
            selectFile()
        }
    }

    private fun selectFile() {
        // Hap 文件 mime 类型未知，使用 */* 更保险
        selectFileResultLauncher.launch("*/*")
    }

    private fun parse(uri: Uri?) {
        if (uri == null) {
            return
        }
        model.isParsing.value = true
        Thread(Runnable {
            synchronized(this) {
                model.isParsing.postValue(true)
                val file = MyFileUtil.getOrCopyFile(this@MainActivity, uri)
                if (file == null) {
                    showSnackBar(R.string.parse_error_fail_obtain)
                    model.isParsing.postValue(false)
                    return@Runnable
                }
                // 解析hap
                val path = file.absolutePath
                val extName = path.substring(path.lastIndexOf(".") + 1)
                if (path.isNotEmpty() && "hap" == extName) {
                    parseHapAndShowInfo(path, uri)
                } else {
                    val continueFlag = AtomicBoolean(false)

                    Snackbar.make(
                        binding.root,
                        R.string.parse_error_type,
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction(R.string.parse_continue_ignoreError) { v ->
                            continueFlag.set(true)
                            parseHapAndShowInfo(path, uri)
                        }
                        .setAnchorView(R.id.floatingActionButton)
                        .addCallback(object :
                            Snackbar.Callback() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar,
                                event: Int
                            ) {
                                // 不继续解析，说明此文件没用了
                                if (!continueFlag.get()) {
                                    MyFileUtil.deleteExternalCacheFile(this@MainActivity, path)
                                }
                            }
                        })
                        .show()
                }
                model.isParsing.postValue(false)
            }
        }).start()
    }

    private fun checkTheme() {
        if (themeManager.isThemeChanged()) {
            recreate()
        }
    }

    /**
     * 解析hap并显示信息
     *
     * @param hapFilePath
     * @param uri
     */
    private fun parseHapAndShowInfo(hapFilePath: String, uri: Uri) {
        // 解析hap
        val hapInfo: HapInfo
        model.isParsing.postValue(true)
        try {
            hapInfo = HapUtil.parse(hapFilePath)
            model.hapInfo.postValue(hapInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(R.string.parse_error_fail)
        }
        // 到此为止，这个临时文件没用了，可以删掉了
        MyFileUtil.deleteExternalCacheFile(this, hapFilePath)
        model.isParsing.postValue(false)
    }

    /**
     * 创建图标的带有边距的阴影 Drawable
     *
     * @param src 原始 Bitmap
     * @return 阴影 BitmapDrawable
     */
    private fun newIconShadowDrawable(src: Bitmap): BitmapDrawable {
        return BitmapDrawable(
            resources, BitmapUtil.newShadowBitmap(
                this, src,
                resources.getDimensionPixelSize(R.dimen.icon_padding),
                resources.getDimensionPixelSize(R.dimen.icon_width),
                resources.getDimensionPixelSize(R.dimen.icon_width)
            )
        )
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return event.hasFileMime().also { if (it) v.alpha = 1f }
            }

            DragEvent.ACTION_DROP -> {
                for (index in 0 until event.clipData.itemCount) {
                    val item = event.clipData.getItemAt(0)
                    if (item.uri != null) {
                        requestDragAndDropPermissions(event)
                        parse(item.uri)
                        break
                    }
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                v.alpha = 0f
            }
        }
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parse(intent.data)
    }

    override fun showSnackBar(text: String): Snackbar {
        // 重写该方法，将 SnackBar 放置到悬浮按钮之上
        return Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.floatingActionButton)
            .apply { show() }
    }

    private fun onHapInfoChanged(hapInfo: HapInfo?) {
        if (hapInfo != null) {
            // 显示基础信息
            binding.basicInfo.apply {
                appName.text = hapInfo.appName
                version.text = String.format("%s (%s)", hapInfo.versionName, hapInfo.versionCode)
                // 显示应用图标
                if (hapInfo.icon != null) {
                    imageView.setImageBitmap(hapInfo.icon)
                    imageView.background = newIconShadowDrawable(hapInfo.icon)
                } else {
                    val defaultIconDrawable = AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_default_new
                    ) as BitmapDrawable
                    imageView.background = newIconShadowDrawable(defaultIconDrawable.bitmap)
                }
            }

            // 显示应用信息
            infoAdapter.setInfo(hapInfo)
        } else {
            infoAdapter.setInfo(HapInfo(true))
        }
    }

    private inner class OnExitCallback : OnBackPressedCallback(true) {
        // 不能共用一个 SnackBar
        var snackbar: Snackbar? = null
        override fun handleOnBackPressed() {
            isEnabled = false
            snackbar = Snackbar.make(binding.root, R.string.exit_toast, Snackbar.LENGTH_SHORT)
                .apply {
                    setAnchorView(R.id.floatingActionButton)
                    addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                            // 如果关闭 SnackBar 的同时对象不一致，说明用户再次点击返回键，此时应保持
                            // OnExitCallback 不被启用。
                            if (snackbar == this@apply)
                                isEnabled = true
                            else
                                Log.w(TAG, "onDismissed: SnackBar 已更改但此时没有消失。")
                        }
                    })
                    show()
                }
        }

        fun closeSnackBar() {
            snackbar?.dismiss()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * 文件读写权限
         * */
        private val PERMISSIONS_EXTERNAL_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        /**
         * 文件读写权限，用于选择文件 请求码
         * */
        private const val REQUEST_CODE_SELECT_FILE = 1
    }
}