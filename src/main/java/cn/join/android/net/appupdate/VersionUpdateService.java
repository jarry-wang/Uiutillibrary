package cn.join.android.net.appupdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;

import cn.join.android.R;
import cn.join.android.net.cache.NetworkHelper;
import cn.join.android.util.AppUtil;

public class VersionUpdateService extends Service implements cn.join.android.net.appupdate.FileUtil.ReadFileCallBack{

    private final String TAG = "VersionUpdateService";
    private LocalBinder localBinder = new LocalBinder();

    private CheckVersionCallBack checkVersionCallBack;
    private DownLoadListener downLoadListener;
    private boolean downloading;

    private NotificationManager mNotificationManager;
    private Notification.Builder notificationBuilder;
    private final int NOTIFICATION_ID = 100;
    private HandlerThread notificationUpdaterThread;
    private Handler notificationHandler;
    private NetworkHelper networkHelper;

    public VersionUpdateService() {
    }


    class LocalBinder extends Binder {
        public VersionUpdateService getService() {
            return VersionUpdateService.this;
        }
    }

    Handler mainUIHanderler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"--onCreate--");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG,"--onBind--");
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"--onDestroy--");
        setDownLoadListener(null);
        setCheckVersionCallBack(null);
        stopDownLoadForground();
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        downloading = false;

    }

    public void doCheckUpdateTask(String path) {
        cn.join.android.net.appupdate.FileUtil fileUtil = new cn.join.android.net.appupdate.FileUtil(this);
        fileUtil.getVersionInfo(path);
    }

    public void doDownloadTask(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) return;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        starDownLoadForground();

        notificationUpdaterThread = new HandlerThread("notificationUpdaterThread");
        notificationUpdaterThread.start();
        notificationHandler = new Handler(notificationUpdaterThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int progress = msg.arg1;
                Log.d(TAG,"notificationHandler.handleMessage----progress="+progress);
                notificationBuilder.setContentTitle("正在下载更新" + progress + "%"); // the label of the entry
                notificationBuilder.setProgress(100, progress, false);
                mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
            }
        };

        downloading = true;
        if (downLoadListener != null) {
            downLoadListener.begin();
        }
        if (networkHelper ==null)
            networkHelper = NetworkHelper.getInstance();
        networkHelper.download(apkPath, "download", new NetworkHelper.NetworkDownloadListener() {
            @Override
            public void onWaiting() {
                Log.d(TAG,"networkHelper.download----onWaiting");
            }

            @Override
            public void onStarted() {
                Log.d(TAG,"networkHelper.download----onStarted");
            }

            @Override
            public void onDownloadSuccess(File file) {
                Log.d(TAG,"x.http().download----onSuccess");
                if (downLoadListener != null) {
                    downLoadListener.downLoadSuccess(file);
                }
                downloading = false;
                AppUtil.installApk(file,VersionUpdateService.this);
            }

            @Override
            public void onDownloading(int progress) {
                Log.d(TAG,"networkHelper.download----progress="+progress);
                if (downLoadListener != null) {
                    downLoadListener.inProgress(progress);
                }
                if (notificationHandler != null)
                    notificationHandler.sendMessage(Message.obtain(notificationHandler,0,progress,0));
                if (progress >= 100) {
                    doNotificationFinish();
                }
            }

            @Override
            public void onError() {
                Log.d(TAG,"networkHelper.download----onError");
                downloading = false;
                doNotificationFinish();
                if (downLoadListener != null) {
                    downLoadListener.downLoadFailed();
                }
            }

            private void doNotificationFinish() {
                if (mNotificationManager != null) {
                    mNotificationManager.cancelAll();
                }
                if (notificationUpdaterThread !=null){
                    notificationUpdaterThread.getLooper().quit();
                }
                notificationHandler = null;
                notificationUpdaterThread = null;
            }
        });

    }


    /**
     * 让Service保持活跃,避免出现:
     * 如果启动此服务的前台Activity意外终止时Service出现的异常(也将意外终止)
     */
    private void starDownLoadForground() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "下载中,请稍后...";
        notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);  // the status icon
        notificationBuilder.setTicker(text);  // the status text
        notificationBuilder.setWhen(System.currentTimeMillis());  // the time stamp
        notificationBuilder.setContentText(text);  // the contents of the entry
//        notificationBuilder.setContentIntent(contentIntent);  // The intent to send when the entry is clicked
        notificationBuilder.setContentTitle("正在下载更新" + 0 + "%"); // the label of the entry
        notificationBuilder.setProgress(100, 0, false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(true);
        Notification notification = notificationBuilder.getNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopDownLoadForground() {
        stopForeground(true);
    }

    @Override
    public void readFilesuccess(final AppVersionInfo info) {
        if (info.versionCode > AppUtil.getVersionCode(this)){
            info.setNeedUpgrade(true);
        }else {
            info.setNeedUpgrade(false);
        }

        mainUIHanderler.post(new Runnable() {
            @Override
            public void run() {
                if (checkVersionCallBack != null)
                    checkVersionCallBack.onSuccess(info);
            }
        });

    }

    @Override
    public void readFail() {
        Log.d(TAG,"读取版本信息失败");
        mainUIHanderler.post(new Runnable() {
            @Override
            public void run() {
                if (checkVersionCallBack != null)
                    checkVersionCallBack.onFail();
            }
        });
    }


    public void setCheckVersionCallBack(CheckVersionCallBack checkVersionCallBack) {
        this.checkVersionCallBack = checkVersionCallBack;
    }

    public void setDownLoadListener(DownLoadListener downLoadListener) {
        this.downLoadListener = downLoadListener;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

}
