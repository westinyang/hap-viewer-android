package org.ohosdev.hapviewerandroid.util.ohos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.ohosdev.hapviewerandroid.model.HapInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.MatchResult;
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

    private static final String TAG = "HapUtil";

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
            JSONObject appObj = module.getJSONObject("app");
            JSONObject moduleObj = module.getJSONObject("module");

            // app.
            hapInfo.bundleName = appObj.getString("bundleName");
            hapInfo.versionName = appObj.getString("versionName");
            hapInfo.versionCode = appObj.getString("versionCode");
            hapInfo.vendor = appObj.getString("vendor");
            hapInfo.minAPIVersion = appObj.getString("minAPIVersion");
            hapInfo.targetAPIVersion = appObj.getString("targetAPIVersion");
            hapInfo.apiReleaseType = appObj.getString("apiReleaseType");
            if (hapInfo.minAPIVersion.length() > 2) {
                hapInfo.minAPIVersion = hapInfo.minAPIVersion.substring(hapInfo.minAPIVersion.length() - 2);
            }
            if (hapInfo.targetAPIVersion.length() > 2) {
                hapInfo.targetAPIVersion = hapInfo.targetAPIVersion.substring(hapInfo.targetAPIVersion.length() - 2);
            }

            // module.
            hapInfo.mainElement = moduleObj.getString("mainElement");
            // 解析权限
            hapInfo.requestPermissions = moduleObj.getJSONArray("requestPermissions");
            if (hapInfo.requestPermissions != null && !hapInfo.requestPermissions.isEmpty()) {
                hapInfo.requestPermissionNames = new ArrayList<>();
                for (Object item : hapInfo.requestPermissions) {
                    JSONObject itemObj = (JSONObject) item;
                    hapInfo.requestPermissionNames.add(itemObj.getString("name"));
                }
            }

            // 解析图标
            JSONArray moduleAbilities = module.getJSONObject("module").getJSONArray("abilities");
            if (moduleAbilities == null) {
                moduleAbilities = module.getJSONObject("module").getJSONArray("extensionAbilities");
            }
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
                if ("layered_image".equals(iconName)) {
                    iconName = "startIcon";
                }
                String iconPath = String.format("resources/base/media/%s.png", iconName);
                hapInfo.iconPath = iconPath;
                try {
                    hapInfo.iconBytes = getEntryToBytes(zipFile, iconPath);
                    hapInfo.icon = getEntryToImage(zipFile, iconPath);
                } catch (Exception ignore) {}
                // 同时记录下label，用于下面解析名称
                hapInfo.labelName = targetAbility.getString("label").split(":")[1];
            }

            // 解析名称
            String appName = "解析失败";
            try {
                byte[] resourcesIndexBytes = getEntryToBytes(zipFile, "resources.index");
                String resourcesIndexHex = HexUtil.encodeHexStr(resourcesIndexBytes).toUpperCase();
                // label2hex
                String labelNameHex = HexUtil.encodeHexStr(hapInfo.labelName).toUpperCase();
                String reg = "00.{2}000000.{2}000000.{2}0000.{2}.{2}00(.*?)00.{2}00" + labelNameHex;
                // List<String> all =  ReUtil.findAll(reg, resourcesIndexHex, 1);
                MatchResult mr = ReUtil.lastIndexOf(reg, resourcesIndexHex);
                String appNameHex = mr.group(1);
                // 防止非贪婪模式异常导致匹配不准确，按特征正则分割取出最后一段
                String[] appNameHexArr = appNameHex.split("00.{2}000000.{2}000000.{2}0000.{2}.{2}00");
                appNameHex = appNameHexArr[appNameHexArr.length - 1];
                appName = HexUtil.decodeHexStr(appNameHex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hapInfo.appName = appName;

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
                    } else if (ReUtil.contains("libs\\/arm.*\\/libil2cpp.so", zipEntry.getName()) || ReUtil.contains("libs\\/arm.*\\/libtuanjie.so", zipEntry.getName())) {
                        techList.add("Unity(团结引擎)");
                    }
                }
            } finally {
                zipFile.close();
            }
            hapInfo.techList = techList;

            // 更多信息
            hapInfo.moreInfo = new LinkedHashMap<>();
            hapInfo.moreInfo.put("appName", hapInfo.appName);
            hapInfo.moreInfo.put("bundleName", hapInfo.bundleName);
            hapInfo.moreInfo.put("iconPath", hapInfo.iconPath);
            hapInfo.moreInfo.put("vendor", appObj.getString("vendor"));
            hapInfo.moreInfo.put("versionName", hapInfo.versionName);
            hapInfo.moreInfo.put("versionCode", hapInfo.versionCode);
            hapInfo.moreInfo.put("targetAPIVersion", hapInfo.targetAPIVersion);
            hapInfo.moreInfo.put("minAPIVersion", hapInfo.minAPIVersion);
            hapInfo.moreInfo.put("apiReleaseType", hapInfo.apiReleaseType);
            hapInfo.moreInfo.put("mainElement", hapInfo.mainElement);
            hapInfo.moreInfo.put("deviceTypes", moduleObj.getJSONArray("deviceTypes"));
            hapInfo.moreInfo.put("virtualMachine", moduleObj.getString("virtualMachine"));
            hapInfo.moreInfo.put("techList", hapInfo.techList);
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
}
