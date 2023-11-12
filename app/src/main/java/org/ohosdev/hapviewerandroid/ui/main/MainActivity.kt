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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.onegravity.rteditor.RTEditorMovementMethod
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.adapter.InfoAdapter
import org.ohosdev.hapviewerandroid.app.AppPreference
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.app.BaseActivity
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding
import org.ohosdev.hapviewerandroid.extensions.applyDividerIfEnabled
import org.ohosdev.hapviewerandroid.extensions.contentMovementMethod
import org.ohosdev.hapviewerandroid.extensions.contentSelectable
import org.ohosdev.hapviewerandroid.extensions.getBitmap
import org.ohosdev.hapviewerandroid.extensions.hasFileMime
import org.ohosdev.hapviewerandroid.extensions.isGranted
import org.ohosdev.hapviewerandroid.extensions.isPermissionGranted
import org.ohosdev.hapviewerandroid.extensions.newShadowBitmap
import org.ohosdev.hapviewerandroid.extensions.openUrl
import org.ohosdev.hapviewerandroid.extensions.setContentAutoLinkMask
import org.ohosdev.hapviewerandroid.extensions.thisApp
import org.ohosdev.hapviewerandroid.manager.ThemeManager
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HarmonyOSUtil
import org.ohosdev.hapviewerandroid.util.RequestPermissionDialogBuilder
import org.ohosdev.hapviewerandroid.util.ShizukuUtil
import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory

class MainActivity : BaseActivity(), OnDragListener {
    private val themeManager: ThemeManager = ThemeManager(this)

    private val infoAdapter by lazy { InfoAdapter(this) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val rootView: CoordinatorLayout get() = binding.root

    private val onExitCallback by lazy { OnExitCallback() }
    private val model: MainViewModel by viewModels()

    private val selectFileResultLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { handelUri(it) }

    private val shizukuLifecycleObserver = ShizukuUtil.ShizukuLifecycleObserver()

    init {
        lifecycle.addObserver(shizukuLifecycleObserver)
        // 调用onRequestPermissionsResult，统一处理
        shizukuLifecycleObserver.setRequestPermissionResultListener { requestCode, grantResult ->
            onRequestPermissionsResult(
                requestCode, arrayOf(ShizukuUtil.PERMISSION), intArrayOf(grantResult)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.factory2 = LayoutInflaterFactory(delegate)
            .addOnViewCreatedListener(WindowInsetsHelper.LISTENER)
        super.onCreate(savedInstanceState)
        themeManager.applyTheme()
        initViews()

        onBackPressedDispatcher.addCallback(this, onExitCallback)

        // 解析传入的 Intent
        if (savedInstanceState == null) {
            handelUri(intent.data)
        }

        model.hapInfo.observe(this) { onHapInfoChanged(it) }
        model.isParsing.observe(this) {
            invalidateMenu()
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.isInstalling.observe(this) {
            binding.backgroundProgressIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.snackBarEvent.observe(this) {
            it.consume {
                showSnackBar(it.text)
            }
        }
    }

    private fun initViews() {
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
            window.navigationBarColor = Color.TRANSPARENT
        else
            binding.bottomScrim.background = null
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.detailInfo.recyclerView.apply {
            adapter = infoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            applyDividerIfEnabled()
        }

        // 启用拖放
        binding.dropMask.root.setOnDragListener(this)
        binding.selectHapButton.setOnClickListener { onFabClick(it) }
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            it.findItem(R.id.action_install).apply {
                isVisible = HarmonyOSUtil.isHarmonyOS
                isEnabled = !model.hapInfo.value?.init!! && !model.isParsing.value!!
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_about -> handelAboutClick(item)
            R.id.action_theme_material1 -> changeTheme(MATERIAL1)
            R.id.action_theme_material2 -> changeTheme(MATERIAL2)
            R.id.action_theme_material3 -> changeTheme(MATERIAL3)
            R.id.action_theme_harmony -> changeTheme(HARMONY)
            R.id.action_install -> model.hapInfo.value?.let { installHap(it) }
            else -> return false
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
            when (requestCode) {
                REQUEST_CODE_SELECT_FILE -> selectHapFile()
                REQUEST_CODE_SHIZUKU_INSTALL -> installHap(model.hapInfo.value!!)
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
            && isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, PERMISSIONS_EXTERNAL_STORAGE, REQUEST_CODE_SELECT_FILE
            )
        } else {
            selectHapFile()
        }
    }

    private fun selectHapFile() {
        // Hap 文件 mime 类型未知，使用 */* 更保险
        selectFileResultLauncher.launch("*/*")
    }

    /**
     * 解析 Uri，如果为空就什么都不做
     * */
    private fun handelUri(uri: Uri?) {
        if (uri == null)
            return
        model.handelUri(uri)
    }

    private fun changeTheme(themeType: AppPreference.ThemeType) {
        thisApp.appPreference.themeType = themeType
        if (themeManager.isThemeChanged()) {
            recreate()
        }
    }

    /**
     * 创建图标的带有边距的背景阴影
     *
     * @param src 原始 Bitmap
     * @return 阴影 BitmapDrawable
     */
    private fun newIconShadowDrawable(src: Bitmap): BitmapDrawable {
        return BitmapDrawable(
            resources, src.newShadowBitmap(
                this,
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
                        handelUri(item.uri)
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
        handelUri(intent.data)
    }

    override fun showSnackBar(text: String): Snackbar {
        // 重写该方法，将 SnackBar 放置到悬浮按钮之上
        return Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.selectHapButton)
            .apply { show() }
    }

    private fun onHapInfoChanged(hapInfo: HapInfo?) {
        if (hapInfo != null) {
            hapInfo.let {
                // 显示基础信息
                binding.basicInfo.apply {
                    appName.text = it.appName
                    version.text = String.format("%s (%s)", it.versionName, it.versionCode)
                    setHapIcon(it.icon)
                }
            }
            // 显示应用信息
            infoAdapter.setInfo(hapInfo)
        } else {
            infoAdapter.setInfo(HapInfo(true))
        }
    }

    /**
     * 设置显示的 HAP 图标
     *
     * 如果bitmap为空，则显示默认图标。
     * */
    private fun setHapIcon(bitmap: Bitmap?) {
        binding.basicInfo.apply {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.background = newIconShadowDrawable(bitmap)
            } else {
                R.drawable.ic_default_new.also {
                    imageView.setImageResource(it)
                    imageView.background = newIconShadowDrawable(getBitmap(it)!!)
                }
            }
        }
    }

    private inner class OnExitCallback : OnBackPressedCallback(true) {
        // 不能共用一个 SnackBar
        var snackBar: Snackbar? = null
        override fun handleOnBackPressed() {
            isEnabled = false
            snackBar = Snackbar.make(binding.root, R.string.exit_toast, Snackbar.LENGTH_SHORT)
                .apply {
                    setAnchorView(R.id.selectHapButton)
                    addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                            // 如果关闭 SnackBar 的同时对象不一致，说明用户再次点击返回键，此时应保持
                            // OnExitCallback 不被启用。
                            if (snackBar == this@apply)
                                isEnabled = true
                            else
                                Log.w(TAG, "onDismissed: SnackBar 已更改但此时没有消失。")
                        }
                    })
                    show()
                }
        }

        fun closeSnackBar() {
            snackBar?.dismiss()
        }
    }

    private fun installHap(hapInfo: HapInfo, showRequestDialog: Boolean = true) {
        if (hapInfo.init) return
        if (!ShizukuUtil.checkPermission().isGranted) {
            if (showRequestDialog) {
                RequestPermissionDialogBuilder(this)
                    .setPermissionNames(arrayOf(R.string.permission_shizuku))
                    .setFunctionNames(arrayOf(R.string.function_install_hap))
                    .setOnRequest {
                        ShizukuUtil.requestPermission(this, REQUEST_CODE_SHIZUKU_INSTALL)
                    }
                    .setNeutralButton(R.string.guide) { _, _ ->
                        openUrl(ShizukuUtil.URL_GUIDE)
                    }
                    .show()
            }
            return
        }
        model.installHapWaitingShizuku(hapInfo)
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
        private const val REQUEST_CODE_SHIZUKU_INSTALL = 2
    }
}