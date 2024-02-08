package org.ohosdev.hapviewerandroid.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnDragListener
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.alibaba.fastjson.JSON
import com.google.android.material.color.MaterialColors.getColor
import com.google.android.material.snackbar.Snackbar
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.app.AppPreference
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding
import org.ohosdev.hapviewerandroid.extensions.applyDividerIfEnabled
import org.ohosdev.hapviewerandroid.extensions.copyAndShowSnackBar
import org.ohosdev.hapviewerandroid.extensions.getBitmap
import org.ohosdev.hapviewerandroid.extensions.getFirstUri
import org.ohosdev.hapviewerandroid.extensions.getTechDesc
import org.ohosdev.hapviewerandroid.extensions.getVersionNameAndCode
import org.ohosdev.hapviewerandroid.extensions.hasFileMime
import org.ohosdev.hapviewerandroid.extensions.init
import org.ohosdev.hapviewerandroid.extensions.isGranted
import org.ohosdev.hapviewerandroid.extensions.isPermissionGranted
import org.ohosdev.hapviewerandroid.extensions.overrideAnimationDurationIfNeeded
import org.ohosdev.hapviewerandroid.extensions.requestShizukuPermission
import org.ohosdev.hapviewerandroid.extensions.resolveBoolean
import org.ohosdev.hapviewerandroid.extensions.setFragmentResultListener
import org.ohosdev.hapviewerandroid.extensions.thisApp
import org.ohosdev.hapviewerandroid.model.HapInfo
import org.ohosdev.hapviewerandroid.ui.about.AboutDialogFragment
import org.ohosdev.hapviewerandroid.ui.common.BaseActivity
import org.ohosdev.hapviewerandroid.ui.common.dialog.AlertDialogFragment
import org.ohosdev.hapviewerandroid.ui.common.dialog.RequestPermissionDialogFragment
import org.ohosdev.hapviewerandroid.util.ShizukuUtil
import org.ohosdev.hapviewerandroid.util.ShizukuUtil.ShizukuLifecycleObserver
import org.ohosdev.hapviewerandroid.util.SystemUtil
import org.ohosdev.hapviewerandroid.util.SystemUtil.isDarkNavigationBarSupported
import org.ohosdev.hapviewerandroid.util.ohos.getOhosPermSortName
import org.ohosdev.hapviewerandroid.view.AdvancedRecyclerView
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
    private lateinit var permissionsAdapter: PermissionsAdapter


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
            requestShizukuPermission(REQUEST_CODE_SHIZUKU_INSTALL)
        }
        setFragmentResultListener(REQUEST_KEY_REQUEST_STORAGE) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_EXTERNAL_STORAGE, REQUEST_CODE_SELECT_FILE)
        }
    }

    private fun initViews() = binding.apply {
        // 在适当的安卓版本设置状态栏、导航栏透明
        window.statusBarColor = Color.TRANSPARENT
        val isLightNavigationBar = resolveBoolean(R.attr.windowLightNavigationBar, false)
        if (isDarkNavigationBarSupported || !isLightNavigationBar) {
            window.navigationBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, window.decorView).also {
                it.isAppearanceLightNavigationBars = isLightNavigationBar
            }
            bottomScrim.background =
                ColorDrawable(getColor(this@MainActivity, R.attr.navigationBarColorCompatible, Color.RED))
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
        basicInfo.apply {
            root.apply {
                registerForContextMenu(this)
                setOnCreateContextMenuListener { menu, _, menuInfo ->
                    menu.setHeaderTitle(R.string.copy)
                    if (!hapInfo.init) {
                        menuInflater.inflate(R.menu.menu_main_info_basic, menu)
                    }
                }
            }
        }
        detailsInfo.apply {
            moreInfoItem.setOnClickListener { showMoreInfoDialog() }
            detailsGroup.apply {
                registerForContextMenu(this)
                setOnCreateContextMenuListener { menu, _, menuInfo ->
                    menuInfo as ListItemGroup.ContextMenuInfo
                    menu.setHeaderTitle(menuInfo.title)
                    if (!hapInfo.init && !menuInfo.valueText.isNullOrEmpty()) {
                        menuInflater.inflate(R.menu.menu_main_info, menu)
                    }
                }
            }
        }
        permissionsInfo.apply {
            permissionsList.apply {
                adapter = PermissionsAdapter(this@MainActivity).also { permissionsAdapter = it }
                applyDividerIfEnabled()
                itemAnimator = null
                registerForContextMenu(this)
                setOnCreateContextMenuListener { menu, _, menuInfo ->
                    @Suppress("UNCHECKED_CAST")
                    menuInfo as AdvancedRecyclerView.ContextMenuInfo<PermissionsAdapter.ViewHolder>
                    menu.setHeaderTitle(menuInfo.viewHolder.title?.let { it.getOhosPermSortName() ?: it })
                    menuInflater.inflate(R.menu.menu_main_info, menu)
                }

            }
        }
    }

    override fun onContextItemSelected(item: MenuItem) = when (val menuInfo = item.menuInfo) {
        // 将各自的事件传出去
        is ListItemGroup.ContextMenuInfo -> onInfoContextItemSelected(item, menuInfo)
        is AdvancedRecyclerView.ContextMenuInfo<*> ->
            when (val viewHolder = menuInfo.viewHolder) {
                is PermissionsAdapter.ViewHolder -> onPermissionItemSelected(item, viewHolder)
                else -> super.onContextItemSelected(item)
            }

        is BasicInfoCard.ContextMenuInfo -> onBasicInfoContentItemSelected(item)
        else -> super.onContextItemSelected(item)
    }


    private fun onInfoContextItemSelected(item: MenuItem, menuInfo: ListItemGroup.ContextMenuInfo): Boolean {
        when (item.itemId) {
            R.id.action_copy -> copyAndShowSnackBar(menuInfo.valueText, menuInfo.title)
            else -> return false
        }
        return true
    }

    private fun onPermissionItemSelected(item: MenuItem, viewHolder: PermissionsAdapter.ViewHolder): Boolean {
        when (item.itemId) {
            R.id.action_copy -> copyAndShowSnackBar(viewHolder.title)
            else -> return false
        }
        return true
    }

    private fun onBasicInfoContentItemSelected(item: MenuItem): Boolean {
        val content = when (item.itemId) {
            R.id.action_copy_app_name -> Pair(R.string.info_appName, hapInfo.appName)
            R.id.action_copy_version_name -> Pair(R.string.info_versionName, hapInfo.versionName)
            R.id.action_copy_version_code -> Pair(R.string.info_versionCode, hapInfo.versionCode)
            R.id.action_copy_package_name -> Pair(R.string.info_appPackageName, hapInfo.packageName)
            else -> return false
        }
        return copyAndShowSnackBar(content.second, getString(content.first))
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
            .setInfoJson(
                JSON.toJSONString(hapInfo.moreInfo, true)
                    .replace("\t", "    ")
                    .replace("([^\\\\]\".*\"):(\\S)".toRegex()) { "${it.groupValues[1]}: ${it.groupValues[2]}" }
            )
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
        val unknownString = getString(R.string.unknown)
        val unknownTechString = getString(R.string.info_tech_unknown)
        hapInfo.also {
            applyHapIcon(it)
            // 自动根据 hapInfo 的当前状态（如：初始状态）设置 valueText
            fun ListItem.setHapInfoValue(value: String?) {
                val enabled = !it.init && !value.isNullOrEmpty()
                valueText = if (enabled) value else unknownString
            }

            fun TextView.setHapInfoText(value: String?, @StringRes unknownStringId: Int?) {
                val enabled = !it.init && !value.isNullOrEmpty()
                text = when {
                    enabled -> value
                    unknownStringId != null -> getString(unknownStringId)
                    else -> unknownString
                }
            }
            binding.basicInfo.apply {
                nameText.setHapInfoText(it.appName, R.string.unknown_appName)
                versionText.setHapInfoText(it.getVersionNameAndCode(unknownString), R.string.unknown_version)
                packageText.setHapInfoText(it.packageName, R.string.unknown_packageName)
            }
            binding.detailsInfo.apply {
                // appNameItem.setHapInfoValue(it.appName)
                // packageNameItem.setHapInfoValue(it.packageName)
                // versionNameItem.setHapInfoValue(it.versionName)
                // versionCodeItem.setHapInfoValue(it.versionCode)
                targetItem.setHapInfoValue("API ${it.targetAPIVersion} (${it.apiReleaseType})")
                techItem.setHapInfoValue(it.getTechDesc(this@MainActivity) ?: unknownTechString)
                moreInfoItem.isEnabled = !it.init && it.moreInfo != null
            }
            permissionsAdapter.submitList(it.requestPermissionNames)
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
            .setPositiveButton(android.R.string.ok, REQUEST_KEY_INSTALL_HAP)
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
        private val PERMISSIONS_EXTERNAL_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

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