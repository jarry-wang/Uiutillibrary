package cn.join.android.net.appupdate;

/**
 * Created by wangfujia on 17/3/31.
 */

public class AppVersionInfo {
    public String versionName;
    public int versionCode;
    public String versionInfo;
    public int ifForceUpdate;
    public String filePath;

    public boolean needUpgrade;

    public boolean isNeedUpdate() {
        return needUpgrade;
    }

    public boolean isForceUpdate() {
        return ifForceUpdate == 1;
    }


    public void setNeedUpgrade(boolean needUpgrade) {
        this.needUpgrade = needUpgrade;
    }
}
