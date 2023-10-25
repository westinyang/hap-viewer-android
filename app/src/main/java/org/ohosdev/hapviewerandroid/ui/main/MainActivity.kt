package org.ohosdev.hapviewerandroid.ui.main

import android.Manifest
import android.content.ClipDescription
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.onegravity.rteditor.RTEditorMovementMethod
import org.ohosdev.hapviewerandroid.BuildConfig
import org.ohosdev.hapviewerandroid.R
import org.ohosdev.hapviewerandroid.adapter.InfoAdapter
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.HARMONY
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL1
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL2
import org.ohosdev.hapviewerandroid.app.AppPreference.ThemeType.MATERIAL3
import org.ohosdev.hapviewerandroid.databinding.ActivityMainBinding
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
import java.io.IOException
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity(), OnDragListener {
    private val themeManager: ThemeManager = ThemeManager(this)

    private var infoAdapter: InfoAdapter? = null
    private lateinit var binding: ActivityMainBinding

    private lateinit var onExitCallback: OnExitCallback
    private val model: MainViewModel by viewModels()

    private val selectFileResultLauncher =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri: Uri? ->
            parse(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.factory2 = LayoutInflaterFactory(delegate)
            .addOnViewCreatedListener(WindowInsetsHelper.LISTENER)
        super.onCreate(savedInstanceState)
        themeManager.applyTheme()

        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        onExitCallback = OnExitCallback()

        onBackPressedDispatcher.addCallback(this, onExitCallback)

        infoAdapter = InfoAdapter(this)

        val recyclerView = binding.detailInfo.recyclerView
        recyclerView.adapter = infoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dividerTypedArray = theme.obtainStyledAttributes(
            intArrayOf(R.attr.enableDivider)
        )

        if (dividerTypedArray.getBoolean(0, false)) {
            recyclerView.addItemDecoration(object :
                MaterialDividerItemDecoration(this, DividerItemDecoration.VERTICAL) {
                override fun shouldDrawDivider(
                    position: Int,
                    adapter: RecyclerView.Adapter<*>?
                ): Boolean {
                    return if (adapter != null) {
                        position != adapter.itemCount - 1
                    } else false
                }
            })
        }

        // 启用拖放
        binding.dropMask.root.setOnDragListener(this)
        binding.floatingActionButton.setOnClickListener { view: View? ->
            onFabClick(
                view
            )
        }

        // 解析传入的 Intent
        if (savedInstanceState == null) {
            val intent = intent
            parse(intent.data)
        }

        model.hapInfo.observe(this) { hapInfo: HapInfo? ->
            onHapInfoChanged(
                hapInfo
            )
        }
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
        var successCount = 0
        for (i in permissions.indices) {
            Log.i(TAG, "申请权限：" + permissions[i] + "，申请结果：" + grantResults[i])
            if (grantResults[i] == 0) {
                successCount++
            }
        }
        if (successCount == permissions.size) {
            if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE) {
                selectFile()
            }
        } else {
            Snackbar.make(binding.root, R.string.permission_grant_fail, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.floatingActionButton)
                .show()
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
        onExitCallback.snackbar?.dismiss()
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
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                PERMISSIONS_EXTERNAL_STORAGE,
                REQUEST_CODE_EXTERNAL_STORAGE
            )
        } else {
            selectFile()
        }
    }

    private fun selectFile() {
        selectFileResultLauncher.launch("*/*")
    }

    private fun parse(uri: Uri?) {
        if (uri == null) {
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        Thread(Runnable {
            synchronized(this) {
                runOnUiThread { binding.progressBar.visibility = View.VISIBLE }
                val file = MyFileUtil.getOrCopyFile(this@MainActivity, uri)
                if (file == null) {
                    Snackbar.make(
                        binding.root,
                        R.string.parse_error_fail_obtain,
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(R.id.floatingActionButton)
                        .show()
                    runOnUiThread { binding.progressBar.visibility = View.GONE }
                    return@Runnable
                }
                // 解析hap
                val path = file.absolutePath
                val extName = path.substring(path.lastIndexOf(".") + 1)
                if (path.length > 0 && "hap" == extName) {
                    parseHapAndShowInfo(path, uri)
                } else {
                    val continueFlag =
                        AtomicBoolean(false)
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
                runOnUiThread { binding.progressBar.visibility = View.GONE }
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
        runOnUiThread { binding.progressBar.visibility = View.VISIBLE }
        try {
            hapInfo = HapUtil.parse(hapFilePath)
            runOnUiThread { model.hapInfo.setValue(hapInfo) }
        } catch (e: IOException) {
            e.printStackTrace()
            Snackbar.make(binding.root, R.string.parse_error_fail, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.floatingActionButton)
                .show()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Snackbar.make(binding.root, R.string.parse_error_fail, Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.floatingActionButton)
                .show()
        }
        // 到此为止，这个临时文件没用了，可以删掉了
        MyFileUtil.deleteExternalCacheFile(this, hapFilePath)
        runOnUiThread { binding.progressBar.visibility = View.GONE }
    }

    /**
     * 创建阴影 Bitmap
     *
     * @param src 原始 Bitmap
     * @return 阴影Bitmap
     */
    private fun newShadowBitmap(src: Bitmap): Bitmap {
        return BitmapUtil.newShadowBitmap(
            this, src,
            this.resources.getDimensionPixelSize(R.dimen.icon_padding),
            this.resources.getDimensionPixelSize(R.dimen.icon_width),
            this.resources.getDimensionPixelSize(R.dimen.icon_width)
        )
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                var i = 0
                while (i < event.clipDescription.mimeTypeCount) {
                    if (event.clipDescription.getMimeType(i) != ClipDescription.MIMETYPE_TEXT_PLAIN) {
                        v.alpha = 1f
                        return true
                    }
                    i++
                }
                return false
            }

            DragEvent.ACTION_DROP -> {
                var i = 0
                while (i < event.clipData.itemCount) {
                    val item = event.clipData.getItemAt(0)
                    if (item.uri != null) {
                        requestDragAndDropPermissions(event)
                        parse(item.uri)
                        break
                    }
                    i++
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

    private fun onHapInfoChanged(hapInfo: HapInfo?) {
        if (hapInfo != null) {
            // 显示基础信息
            binding.basicInfo.appName.text = hapInfo.appName
            binding.basicInfo.version.text =
                String.format("%s (%s)", hapInfo.versionName, hapInfo.versionCode)
            // 显示应用图标
            if (hapInfo.icon != null) {
                binding.basicInfo.imageView.setImageBitmap(hapInfo.icon)
                binding.basicInfo.imageView.background =
                    BitmapDrawable(resources, newShadowBitmap(hapInfo.icon))
            } else {
                val defaultIconDrawable = Objects.requireNonNull(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.ic_default_new
                    )
                ) as BitmapDrawable
                binding.basicInfo.imageView.background = BitmapDrawable(
                    resources,
                    newShadowBitmap(defaultIconDrawable.bitmap)
                )
            }

            // 显示应用信息
            infoAdapter!!.setInfo(hapInfo)
        } else {
            infoAdapter!!.setInfo(HapInfo(true))
        }
    }

    private inner class OnExitCallback : OnBackPressedCallback(true) {
        var snackbar: Snackbar? = null
        override fun handleOnBackPressed() {
            isEnabled = false
            snackbar = Snackbar.make(binding.root, R.string.exit_toast, Snackbar.LENGTH_SHORT)
            snackbar!!.setAnchorView(R.id.floatingActionButton)
            snackbar!!.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                    isEnabled = true
                }
            })
            snackbar!!.show()
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
         * 文件读写权限 请求码
         * */
        private const val REQUEST_CODE_EXTERNAL_STORAGE = 1
    }
}