package cn.join.android.net.appupdate;

/**
 * Created by wangfujia on 17/3/30.
 */

public interface CheckVersionCallBack {
    void onSuccess(AppVersionInfo info);
    void onFail();
}
