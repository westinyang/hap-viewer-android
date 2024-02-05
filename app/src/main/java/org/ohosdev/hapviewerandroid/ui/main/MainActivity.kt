package org.ohosdev.hapviewerandroid.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
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
import cn.hutool.json.JSONUtil
import com.google.android.material.snackbar.Snackbar
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.AppPreference
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding
import org.ohosdev.hapviewerandroid.extensions.copyText
import org.ohosdev.hapviewerandroid.extensions.getBitmap
import org.ohosdev.hapviewerandroid.extensions.getFirstUri
import org.ohosdev.hapviewerandroid.extensions.getTechDesc
import org.ohosdev.hapviewerandroid.extensions.hasFileMime
import org.ohosdev.hapviewerandroid.extensions.init
import org.ohosdev.hapviewerandroid.extensions.isGranted
import org.ohosdev.hapviewerandroid.extensions.isPermissionGranted
import org.ohosdev.hapviewerandroid.extensions.overrideAnimationDurationIfNeeded
import org.ohosdev.hapviewerandroid.extensions.setFragmentResultListener
import org.ohosdev.hapviewerandroid.extensions.thisApp
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.ui.about.AboutDialogFragment
import org.ohosdev.hapviewerandroid.ui.common.BaseActivity
import org.ohosdev.hapviewerandroid.ui.common.dialog.AlertDialogFragment
import org.ohosdev.hapviewerandroid.ui.common.dialog.RequestPermissionDialogFragment
import org.ohosdev.hapviewerandroid.util.HarmonyOSUtil
import org.ohosdev.hapviewerandroid.util.ShizukuUtil
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuLifecycleObserver
import org.ohosdev.hapviewerandroid.view.drawable.ShadowBitmapDrawable
import org.ohosdev.hapviewerandroid.view.list.ListItem
import org.ohosdev.hapviewerandroid.view.list.ListItemGroup

class MainActivity : BaseActivity(), OnDragListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override val rootView: CoordinatorLayout get() = binding.root
    private val model: MainViewModel by viewModels()
    private var hapInfo
        get() = model.hapInfo.value!!
        set(value) {
            model.hapInfo.value = value
        }


    // private val infoAdapter by lazy { InfoAdapter(this, this::onInfoItemClick) }
    private val selectFileResultLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { handelUri(it) }
    private val onExitCallback by lazy { OnExitCallback() }

    init {
        ShizukuLifecycleObserver().apply {
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

        model.hapInfo.observe(this, this::onHapInfoChanged)
        // isParsing 与 isInstalling 同时影响着菜单的禁用状态，所以要调用 invalidateMenu()
        model.isParsing.observe(this) {
            invalidateMenu()
            binding.foregroundProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.isInstalling.observe(this) {
            invalidateMenu()
            binding.backgroundProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        model.snackBarEvent.observe(this) { it.consume { showSnackBar(it.text) } }

        // 解析传入的 Intent
        if (savedInstanceState == null) handelUri(intent.data)

        setFragmentResultListener(REQUEST_KEY_INSTALL_HAP) {
            model.installHapWaitingShizuku(hapInfo)
        }
        setFragmentResultListener(REQUEST_KEY_REQUEST_SHIZUKU) {
            ShizukuUtil.requestPermission(this, REQUEST_CODE_SHIZUKU_INSTALL)
        }
        setFragmentResultListener(REQUEST_KEY_REQUEST_STORAGE) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_EXTERNAL_STORAGE,
                REQUEST_CODE_SELECT_FILE
            )
        }
    }

    private fun initViews() = binding.apply {
        // 在适当的安卓版本设置状态栏、导航栏透明
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            bottomScrim.background = null
        }

        setContentView(root)
        setSupportActionBar(toolbar)

        dropMask.root.setOnDragListener(this@MainActivity)

        selectHapButton.setOnClickListener {
            // 申请权限
            // 安卓10及以上不需要存储权限，可以直接使用
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && !isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                RequestPermissionDialogFragment()
                    .setPermissionNames(intArrayOf(R.string.permission_storage))
                    .setFunctionNames(intArrayOf(R.string.read_file_directly))
                    .setAdditional(R.string.permission_storage_additional)
                    .setOnAgreeKey(REQUEST_KEY_REQUEST_STORAGE)
                    .show(supportFragmentManager, TAG_DIALOG_REQUEST_STORAGE)
                return@setOnClickListener
            }

            selectHapFile()
        }
        registerForContextMenu(detailsInfo.detailsGroup)

        detailsInfo.moreInfoItem.setOnClickListener { showMoreInfoDialog() }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        when (menuInfo) {
            is ListItemGroup.ListItemGroupContextMenuInfo -> {
                menu.setHeaderTitle(menuInfo.title)
                if (!hapInfo.init && !menuInfo.valueText.isNullOrEmpty()) {
                    menuInflater.inflate(R.menu.menu_main_info, menu)
                }
            }

            else -> super.onCreateContextMenu(menu, v, menuInfo)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // 将各自的事件传出去
        when (val menuInfo = item.menuInfo) {
            is ListItemGroup.ListItemGroupContextMenuInfo -> {
                return onInfoContextItemSelected(item, menuInfo)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun onInfoContextItemSelected(
        item: MenuItem,
        menuInfo: ListItemGroup.ListItemGroupContextMenuInfo
    ): Boolean {
        when (item.itemId) {
            R.id.action_copy -> {
                menuInfo.valueText.also {
                    if (it.isNullOrEmpty()) return false
                    copyText(it)
                }
                showSnackBar(getString(R.string.copied_withName, menuInfo.title))
            }

            else -> return false
        }
        return true
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
                isVisible = SystemUtil.isOhosSupported
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
            R.id.action_install -> installHap(hapInfo)
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
        if (BuildConfig.DEBUG) {
            permissions.forEachIndexed { i, permission ->
                Log.i(TAG, "申请权限：$permission，申请结果：${grantResults[i]}")
            }
        }
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            when (requestCode) {
                REQUEST_CODE_SELECT_FILE -> selectHapFile()
                REQUEST_CODE_SHIZUKU_INSTALL -> installHap(hapInfo)
            }
        } else {
            when (requestCode) {
                // 可以在不获得权限的情况下选择文件
                REQUEST_CODE_SELECT_FILE -> selectHapFile()
                else -> showSnackBar(R.string.permission_grant_fail)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Snack bar 可能不会显示，也就不会重新启用，这时候就需要在重新进入应用时启用一下二次返回。
        onExitCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        // 退出时可能会显示Snack bar，以至于下一次弹出多个Snack bar
        onExitCallback.closeSnackBar()
    }


    private fun showAboutDialog() {
        AboutDialogFragment().show(supportFragmentManager, AboutDialogFragment.TAG)
    }

    private fun showMoreInfoDialog() {
        MoreInfoDialogFragment()
            .setInfoJson(JSONUtil.toJsonPrettyStr(hapInfo.moreInfo))
            .show(supportFragmentManager, MoreInfoDialogFragment.TAG)
    }


    private fun selectHapFile() {
        // Hap 文件 mime 类型未知，使用 */* 更保险
        selectFileResultLauncher.launch("*/*")
    }

    /**
     * 解析 Uri，如果为空就什么都不做
     * */
    private fun handelUri(uri: Uri?) {
        if (uri != null) {
            model.handelUri(uri)
        }
    }

    private fun changeTheme(themeType: AppPreference.ThemeType) {
        thisApp.appPreference.themeType = themeType
        if (themeManager.isThemeChanged()) {
            recreate()
        }
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return event.hasFileMime().also { if (it) v.alpha = 1f }
            }

            DragEvent.ACTION_DROP -> event.clipData.getFirstUri().let {
                if (it != null) {
                    requestDragAndDropPermissions(event)
                    handelUri(it)
                } else {
                    return false
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

    // 重写该方法，将 SnackBar 放置到悬浮按钮之上
    @SuppressLint("ShowToast")
    override fun showSnackBar(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
            .overrideAnimationDurationIfNeeded()
            .setAnchorView(R.id.select_hap_button)
            .apply { show() }


    private fun onHapInfoChanged(hapInfo: HapInfo = HapInfo.INIT) {
        val unknownString = getString(android.R.string.unknownName)
        val unknownTechString = getString(R.string.info_tech_unknown)
        hapInfo.let {
            // 显示基础信息，暂时用不到。
            /* binding.basicInfo.apply {
                appName.text = it.appName
                version.text = String.format("%s (%s)", it.versionName, it.versionCode)
            } */
            applyHapIcon(it)
            fun ListItem.setHapInfoValue(value: String?) {
                val enabled = !it.init && value != null
                valueText = if (enabled) value else unknownString
            }
            binding.detailsInfo.apply {
                appNameItem.setHapInfoValue(it.appName)
                packageNameItem.setHapInfoValue(it.packageName)
                versionNameItem.setHapInfoValue(it.versionName)
                versionCodeItem.setHapInfoValue(it.versionCode)
                targetItem.setHapInfoValue("API ${it.targetAPIVersion} (${it.apiReleaseType})")
                techItem.setHapInfoValue(it.getTechDesc(this@MainActivity) ?: unknownTechString)
                moreInfoItem.isEnabled = !it.init && it.moreInfo != null
            }
        }

    }

    /**
     * 设置显示的 HAP 图标
     *
     * 如果 `bitmap` 为空，则显示默认图标。
     * */
    private fun applyHapIcon(hapInfo: HapInfo) {
        binding.basicInfo.hapIconImage.apply {
            val iconBitmap = if (hapInfo.icon != null) {
                setImageBitmap(hapInfo.icon)
                hapInfo.icon
            } else {
                setImageResource(R.drawable.ic_default_new)
                getBitmap(R.drawable.ic_default_new)!!
            }
            background.apply {
                if (this is ShadowBitmapDrawable) {
                    setShadowBitmap(iconBitmap, resources.getDimension(R.dimen.icon_shadow_radius))
                }
            }
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
                RequestPermissionDialogFragment()
                    .setPermissionNames(intArrayOf(R.string.permission_shizuku))
                    .setFunctionNames(intArrayOf(R.string.install_hap))
                    .setGuideUrl(ShizukuUtil.URL_GUIDE)
                    .setOnAgreeKey(REQUEST_KEY_REQUEST_SHIZUKU)
                    .show(supportFragmentManager, TAG_DIALOG_REQUEST_SHIZUKU)
            }
            return
        }
        AlertDialogFragment()
            .setTitle(R.string.install_hap)
            .setMessage(R.string.install_hap_message)
            .setPositiveButton(android.R.string.ok, "install_hap")
            .setNegativeButton(android.R.string.cancel, null)
            .show(supportFragmentManager, TAG_DIALOG_INSTALL_HAP)
    }

    /**
     * 按两次返回键退出
     * */
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

        fun closeSnackBar() = snackBar?.dismiss()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val TAG_DIALOG_INSTALL_HAP = "install_hap"
        private const val TAG_DIALOG_REQUEST_SHIZUKU = "request_shizuku"
        private const val TAG_DIALOG_REQUEST_STORAGE = "request_storage"

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

        private const val REQUEST_KEY_INSTALL_HAP = "install_hap"
        private const val REQUEST_KEY_REQUEST_SHIZUKU = "request_shizuku"
        private const val REQUEST_KEY_REQUEST_STORAGE = "request_storage"
    }

}