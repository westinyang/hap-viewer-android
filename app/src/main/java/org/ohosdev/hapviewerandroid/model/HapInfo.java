package org.ohosdev.hapviewerandroid.model;

import android.content.Context;
import android.graphics.Bitmap;

import org.ohosdev.hapviewerandroid.R;

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

    /* 额外 */
    public String hapFilePath;
    public Set<String> techList;
    private String techDesc;

    public HapInfo() {
    }

    public HapInfo(boolean init) {
        this.init = init;
    }

    public String getTechDesc() {
        if (techList != null && techList.size() > 0) {
            techDesc = String.join("、", techList);
        } else {
            techDesc = "原生开发或未知开发技术";
        }
        return techDesc;
    }

    public void setTechDesc(String techDesc) {
        this.techDesc = techDesc;
    }

    public String getTechDesc(Context context) {
        if (techList != null && techList.size() > 0) {
            techDesc = String.join(context.getString(R.string.separator_words), techList);
        } else {
            techDesc = context.getString(R.string.info_tech_unknown);
        }
        return techDesc;
    }
}
