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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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
import org.ohosdev.hapviewerandroid.app.URL_OPEN_SOURCE_LICENSES
import org.ohosdev.hapviewerandroid.app.URL_PRIVACY_POLICY
import org.ohosdev.hapviewerandroid.app.URL_REPOSITORY
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
import org.ohosdev.hapviewerandroid.extensions.overrideAnimationDurationIfNotMd3
import org.ohosdev.hapviewerandroid.extensions.setContentAutoLinkMask
import org.ohosdev.hapviewerandroid.extensions.thisApp
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.util.HarmonyOSUtil
import org.ohosdev.hapviewerandroid.util.ShizukuUtil
import org.ohosdev.hapviewerandroid.util.dialog.RequestPermissionDialogBuilder

class MainActivity : BaseActivity(), OnDragListener {

    private val infoAdapter by lazy { InfoAdapter(this) }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val rootView: CoordinatorLayout get() = binding.root

    private val onExitCallback by lazy { OnExitCallback() }
    private val model: MainViewModel by viewModels()

    private val selectFileResultLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { handelUri(it) }

    init {
        ShizukuUtil.ShizukuLifecycleObserver().apply {
            lifecycle.addObserver(this)
            // 调用onRequestPermissionsResult，统一处理
            setRequestPermissionResultListener { requestCode, grantResult ->
                onRequestPermissionsResult(
                    requestCode, arrayOf(ShizukuUtil.PERMISSION), intArrayOf(grantResult)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        onBackPressedDispatcher.addCallback(this, onExitCallback)

        model.hapInfo.observe(this) { onHapInfoChanged(it) }
        // isParsing 与 isInstalling 同时影响着菜单的禁用状态，所以要调用 invalidateMenu()
        model.isParsing.observe(this) {
            invalidateMenu()
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.isInstalling.observe(this) {
            invalidateMenu()
            binding.backgroundProgressIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.snackBarEvent.observe(this) { it.consume { showSnackBar(it.text) } }

        // 解析传入的 Intent
        if (savedInstanceState == null) handelUri(intent.data)
    }

    private fun initViews() = binding.apply {
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            window.navigationBarColor = Color.TRANSPARENT
        else bottomScrim.background = null

        setContentView(root)
        setSupportActionBar(toolbar)

        detailInfo.recyclerView.apply {
            adapter = infoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            applyDividerIfEnabled()
        }

        dropMask.root.setOnDragListener(this@MainActivity)

        selectHapButton.setOnClickListener {
            // 申请权限
            // 安卓10及以上不需要存储权限，可以直接使用
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && !isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, PERMISSIONS_EXTERNAL_STORAGE, REQUEST_CODE_SELECT_FILE
                )
                return@setOnClickListener
            }

            selectHapFile()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val themeItemId = when (thisApp.appPreference.themeType) {
            MATERIAL1 -> R.id.action_theme_material1
            MATERIAL2 -> R.id.action_theme_material2
            MATERIAL3 -> R.id.action_theme_material3
            HARMONY -> R.id.action_theme_harmony
        }
        menu.findItem(themeItemId).isChecked = true
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.action_install).apply {
                isVisible = HarmonyOSUtil.isHarmonyOS
                isEnabled =
                    model.run { !hapInfo.value?.init!! && !isParsing.value!! && !isInstalling.value!! }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_about -> showAboutDialog()
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
        permissions.forEachIndexed { i, permission ->
            Log.i(TAG, "申请权限：" + permission + "，申请结果：" + grantResults[i])
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


    private fun showAboutDialog() {
        // 华为设备上拖拽阴影在 Material Dialog 有bug
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage("") // 将在 apply 中设置
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.legal_more, null)
            .show().apply {
                contentSelectable = true
                setContentAutoLinkMask(Linkify.WEB_URLS)
                setMessage(
                    getString(R.string.about_message, BuildConfig.VERSION_NAME, URL_REPOSITORY)
                )
                setContentAutoLinkMask(0)
                contentMovementMethod = RTEditorMovementMethod.getInstance()
                getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                    val popupMenu = PopupMenu(this@MainActivity, this)
                    popupMenu.inflate(R.menu.menu_legal)
                    popupMenu.setOnMenuItemClickListener {
                        val link = when (it.itemId) {
                            R.id.action_privacy_policy -> URL_PRIVACY_POLICY
                            R.id.action_open_source_licenses -> URL_OPEN_SOURCE_LICENSES
                            else -> throw NoSuchElementException("Unknown itemId: $it")
                        }
                        openUrl(link)
                        true
                    }
                    setOnTouchListener(popupMenu.dragToOpenListener)
                    setOnClickListener { popupMenu.show() }
                }
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
        uri?.let { model.handelUri(uri) }
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
        val iconPadding = resources.getDimensionPixelSize(R.dimen.icon_padding)
        val iconWidth = resources.getDimensionPixelSize(R.dimen.icon_width)
        return BitmapDrawable(
            resources, src.newShadowBitmap(this, iconPadding, iconWidth, iconWidth)
        )
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return event.hasFileMime().also { if (it) v.alpha = 1f }
            }

            DragEvent.ACTION_DROP -> {
                event.clipData.let {
                    for (index in 0 until it.itemCount) {
                        val item = it.getItemAt(0)
                        if (item.uri != null) {
                            requestDragAndDropPermissions(event)
                            handelUri(item.uri)
                            break
                        }
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
            .overrideAnimationDurationIfNotMd3()
            .setAnchorView(R.id.selectHapButton)
            .apply { show() }
    }

    private fun onHapInfoChanged(hapInfo: HapInfo = HapInfo.INIT) {
        hapInfo.let {
            // 显示基础信息，暂时用不到。
            /* binding.basicInfo.apply {
                appName.text = it.appName
                version.text = String.format("%s (%s)", it.versionName, it.versionCode)
            } */
            applyHapIcon(it)
            infoAdapter.setInfo(it)
        }
    }

    /**
     * 设置显示的 HAP 图标
     *
     * 如果 `bitmap` 为空，则显示默认图标。
     * */
    private fun applyHapIcon(hapInfo: HapInfo) {
        binding.basicInfo.imageView.apply {
            val iconBitmap = if (hapInfo.icon != null) {
                setImageBitmap(hapInfo.icon)
                hapInfo.icon
            } else {
                setImageResource(R.drawable.ic_default_new)
                getBitmap(R.drawable.ic_default_new)!!
            }
            background = newIconShadowDrawable(iconBitmap)
        }
    }

    /**
     * 向用户确认安装 HAP
     * @param showRequestDialog 如果权限未给予，就显示授权对话框
     * */
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
                    .setNeutralButton(R.string.guide, null)
                    .show().apply {
                        getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                            openUrl(ShizukuUtil.URL_GUIDE)
                        }
                    }
            }
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.install_hap)
            .setMessage(R.string.install_hap_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> model.installHapWaitingShizuku(hapInfo) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private inner class OnExitCallback : OnBackPressedCallback(true) {
        // 不能共用一个 SnackBar
        var snackBar: Snackbar? = null
        override fun handleOnBackPressed() {
            isEnabled = false
            snackBar = showSnackBar(R.string.exit_toast).apply {
                addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                        // 如果关闭 SnackBar 的同时对象不一致，说明用户再次点击返回键，此时应保持
                        // OnExitCallback 不被启用。
                        if (snackBar == this@apply) isEnabled = true
                        else Log.w(TAG, "onDismissed: SnackBar 已更改但此时没有消失。")
                    }
                })
            }

        }

        fun closeSnackBar() {
            snackBar?.dismiss()
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
        private const val REQUEST_CODE_SHIZUKU_INSTALL = 2
    }
}