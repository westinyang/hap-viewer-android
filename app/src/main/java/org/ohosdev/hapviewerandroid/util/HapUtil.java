package org.ohosdev.hapviewerandroid.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.ohosdev.hapviewerandroid.extensions.FileExtensionsKt;
import org.ohosdev.hapviewerandroid.model.HapInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ReUtil;

/**
 * HapUtil
 *
 * @author westinyang
 */
public class HapUtil {

    /**
     * 解析hap
     *
     * @param hapFilePath hap文件路径
     * @return HapInfo
     */
    public static HapInfo parse(String hapFilePath) throws IOException {
        HapInfo hapInfo = new HapInfo();
        ZipFile zipFile = null;
        try {
            hapInfo.hapFilePath = hapFilePath;
            zipFile = new ZipFile(hapFilePath);
            // 读取module.json
            JSONObject module = getEntryToJsonObject(zipFile, "module.json");
            // 读取pack.info
            // JSONObject pack = getEntryJsonObject(zipFile, "pack.info");

            // app.
            JSONObject appObj = module.getJSONObject("app");
            hapInfo.packageName = appObj.getString("bundleName");
            hapInfo.versionName = appObj.getString("versionName");
            hapInfo.versionCode = appObj.getString("versionCode");
            hapInfo.vendor = appObj.getString("vendor");
            hapInfo.minAPIVersion = appObj.getString("minAPIVersion");
            hapInfo.targetAPIVersion = appObj.getString("targetAPIVersion");
            hapInfo.apiReleaseType = appObj.getString("apiReleaseType");

            // module.
            JSONObject moduleObj = module.getJSONObject("module");
            hapInfo.mainElement = moduleObj.getString("mainElement");

            // 解析图标
            JSONArray moduleAbilities = module.getJSONObject("module").getJSONArray("abilities");
            JSONObject targetAbility = null;
            try {
                targetAbility = (JSONObject) moduleAbilities.get(0);
            } catch (Exception ignore) {
            }
            for (Object item : moduleAbilities) {
                JSONObject ability = (JSONObject) item;
                if (hapInfo.mainElement.equals(ability.getString("name"))) {
                    targetAbility = ability;
                    break;
                }
            }
            if (targetAbility != null) {
                String iconName = targetAbility.getString("icon").split(":")[1];
                String iconPath = String.format("resources/base/media/%s.png", iconName);
                hapInfo.iconPath = iconPath;
                hapInfo.iconBytes = getEntryToBytes(zipFile, iconPath);
                hapInfo.icon = getEntryToImage(zipFile, iconPath);
            }

            // 解析名称
            byte[] resourcesIndexBytes = getEntryToBytes(zipFile, "resources.index");
            String resourcesIndexHex = HexUtil.encodeHexStr(resourcesIndexBytes).toUpperCase();
            String appNameHex = ReUtil.get("(00.{2}0000000900000003000001.{2}00)(.*?)(00.{2}00)", resourcesIndexHex, 2);
            hapInfo.appName = HexUtil.decodeHexStr(appNameHex);

            // 技术探测，暂时先简单判断时间，后续抽离到配置文件
            Set<String> techList = new HashSet<>();
            try {
                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry zipEntry = entries.nextElement();
                    // System.out.println(zipEntry.getName());
                    if (ReUtil.contains("libs\\/arm.*\\/libcocos.so", zipEntry.getName())
                            || ReUtil.contains("ets\\/workers\\/CocosWorker.abc", zipEntry.getName())
                    ) {
                        techList.add("Cocos");
                    } else if (ReUtil.contains("libs\\/arm.*\\/libflutter.so", zipEntry.getName())) {
                        techList.add("Flutter");
                    } else if (ReUtil.contains("libs\\/arm.*\\/libQt5Core.so", zipEntry.getName())) {
                        techList.add("Qt");
                    }
                }
            } finally {
                zipFile.close();
            }
            hapInfo.techList = techList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("HAP file parse failed");
        } finally {
            IoUtil.close(zipFile);
        }

        return hapInfo;
    }

    /**
     * 读取文件为JsonObject
     *
     * @param zipFile   zip文件
     * @param entryName 条目名称
     * @return
     */
    public static JSONObject getEntryToJsonObject(ZipFile zipFile, String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            String content = IoUtil.readUtf8(is);
            return JSONObject.parseObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(is);
        }
    }

    /**
     * 读取文件为图像
     *
     * @param zipFile   zip文件
     * @param entryName 条目名称
     * @return
     */
    public static Bitmap getEntryToImage(ZipFile zipFile, String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(is);
        }
    }

    /**
     * 读取文件为字节数组
     *
     * @param zipFile   zip文件
     * @param entryName 条目名称
     * @return
     */
    public static byte[] getEntryToBytes(ZipFile zipFile, String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            return IoUtil.readBytes(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(is);
        }
    }

    public static void destroyHapInfo(@NonNull Context context, @NonNull HapInfo hapInfo) {
        // 删除临时文件
        if (hapInfo.hapFilePath != null) {
            File hapFile = new File(hapInfo.hapFilePath);
            if (FileExtensionsKt.isExternalCache(hapFile, context)) {
                if (hapFile.isFile() && !hapFile.delete()) {
                    hapFile.deleteOnExit();
                }
            }
        }
        if (hapInfo.icon != null) {
            hapInfo.icon.recycle();
        }
    }

}
