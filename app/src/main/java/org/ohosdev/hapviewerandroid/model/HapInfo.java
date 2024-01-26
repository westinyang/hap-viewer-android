package org.ohosdev.hapviewerandroid.model;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;

import org.ohosdev.hapviewerandroid.R;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Hap安装包信息
 *
 * @author westinyang
 */
public class HapInfo {
    public static final HapInfo INIT = new HapInfo(true);
    public boolean init = false;

    /* app. */
    public Bitmap icon;
    public String iconPath;
    public byte[] iconBytes;
    public String labelName;
    public String appName;
    public String packageName;
    public String versionName;
    public String versionCode;
    public String vendor;
    public String minAPIVersion;
    public String targetAPIVersion;
    public String apiReleaseType;

    /* module. */
    public String mainElement;

    public JSONArray requestPermissions;
    public List<String> requestPermissionNames;

    /* more */
    public Map<String, Object> moreInfo;

    /* 额外 */
    public String hapFilePath;
    public Set<String> techList;

    public HapInfo() {
    }

    public HapInfo(boolean init) {
        this.init = init;
    }

}
