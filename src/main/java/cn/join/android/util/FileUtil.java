package cn.join.android.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangfujia on 17/3/31.
 */

public class FileUtil {
    public static File getDownloadCacheFolder() {
        return Environment.getExternalStorageDirectory();
    }

    public static String getDownloadCacheFolderPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String isExistDir(String saveDir) throws IOException {
        File downLoadForder = new File(getDownloadCacheFolder(),saveDir);
        if (!downLoadForder.mkdirs()) {
            downLoadForder.createNewFile();
        }
        String savePath = downLoadForder.getAbsolutePath();
        return savePath;
    }

    public static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static <T> T checkNotNull(T reference,String info) {
        if (reference == null) {
            throw new NullPointerException(info);
        }
        return reference;
    }
}
