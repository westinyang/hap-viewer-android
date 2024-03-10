package org.ohosdev.hapviewerandroid.model;

import android.graphics.Bitmap;

import com.alibaba.fastjson.JSONArray;

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
    public static final int FLAG_FILE_STATE_INSTALLING = 1;
    public static final int FLAG_FILE_STATE_PREINSTALLATION = 1<<1;
    public static final int FLAG_FILE_STATE_INIT = 1 << 2;

    public int fileStateFlags = 0;

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

    private HapInfo(boolean init) {
        if (init) {
            fileStateFlags |= FLAG_FILE_STATE_INIT;
        }
    }

    public boolean isInit() {
        return (fileStateFlags & FLAG_FILE_STATE_INIT) != 0;
    }

    public boolean isInstalling() {
        return (fileStateFlags & FLAG_FILE_STATE_INSTALLING) != 0;
    }

    public boolean isPreinstallation() {
        return (fileStateFlags & FLAG_FILE_STATE_PREINSTALLATION) != 0;
    }


    public void setInstalling(boolean installing) {
        synchronized (this) {
            if (installing) {
                fileStateFlags |= FLAG_FILE_STATE_INSTALLING;
            } else {
                fileStateFlags &= ~FLAG_FILE_STATE_INSTALLING;
            }
        }
    }

    public void setPreinstallation(boolean preinstallation) {
        synchronized (this) {
            if (preinstallation) {
                fileStateFlags |= FLAG_FILE_STATE_PREINSTALLATION;
            } else {
                fileStateFlags &= ~FLAG_FILE_STATE_PREINSTALLATION;
            }
        }
    }

}
