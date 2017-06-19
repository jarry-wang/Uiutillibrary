package cn.join.android.net.appupdate;

import java.io.File;

/**
 * Created by wangfujia on 17/3/30.
 */

public interface DownLoadListener {
    void begin();
    void inProgress(int progress);
    void downLoadSuccess(File file);
    void downLoadFailed();
}
