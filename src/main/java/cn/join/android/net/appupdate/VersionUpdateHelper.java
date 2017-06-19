package cn.join.android.net.appupdate;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;

import cn.join.android.net.util.NetUtil;
import cn.join.android.util.ToastUtil;

/**
 * Created by wangfujia on 17/3/30.
 */

public class VersionUpdateHelper {

    private final String TAG = "VersionUpdateHelper";
    private Context context;

    private String appVersionUrl;

    private boolean isCanceled;

    private boolean ifToastInfo = false;

    private boolean showDialogOnstart;

    private VersionUpdateService mService;

    private AlertDialog waitForUpdateDialog;
    private ProgressDialog progressDialog;

    private AppVersionInfo versionInfo;


    public VersionUpdateHelper(Context context,String url) {
        this.context = context;
        this.appVersionUrl = url;
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            VersionUpdateService.LocalBinder localBinder = (VersionUpdateService.LocalBinder) service;
            mService = localBinder.getService();
            mService.setCheckVersionCallBack(new CheckVersionCallBack() {
                @Override
                public void onSuccess(final AppVersionInfo info) {
                    versionInfo = info;
                    if (!info.isNeedUpdate()){
                        showToast("暂无新版本");
                        cancel();
                        return;
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(info.versionName+"版本升级");
                    builder.setMessage(info.versionInfo);
                    builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if (NetUtil.isWifi(context)){
                                mService.doDownloadTask(info.filePath);
                            }else {
                                showNoWifiDownloadDialog(info.isForceUpdate(),info.filePath);
                            }
                        }
                    });
                    if (!info.isForceUpdate()){
                        builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                cancel();
                            }
                        });
                    }
                    builder.setCancelable(false);
                    waitForUpdateDialog = builder.create();
                    waitForUpdateDialog.show();
                }

                @Override
                public void onFail() {
                    showToast("检查失败，请检查网络设置");
                    unBindService();
                }
            });

            mService.setDownLoadListener(new DownLoadListener() {
                @Override
                public void begin() {
                    if (versionInfo != null){
                        if (versionInfo.isForceUpdate()) {
                            progressDialog = new ProgressDialog(context);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setMessage("正在下载更新");
                            progressDialog.show();
                        }
                    }
                }

                @Override
                public void inProgress(int progress) {
                    if (progressDialog != null) {
                        progressDialog.setMax(100);
                        progressDialog.setProgress(progress);
                    }
                }

                @Override
                public void downLoadSuccess(File file) {
                    if (progressDialog != null){
                        progressDialog.cancel();
                    }
                    unBindService();
                    showToast("下载成功");
                }

                @Override
                public void downLoadFailed() {
                    if (progressDialog != null){
                        progressDialog.cancel();
                    }
                    unBindService();
                    showToast("下载失败");
                }
            });
            Log.d(TAG,"--onServiceConnected--");
            mService.doCheckUpdateTask(appVersionUrl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"--onServiceDisconnected--");
            if (isWaitForDownload())
                progressDialog.cancel();
            if (isWaitForUpdate())
                waitForUpdateDialog.cancel();
            if (mService != null) {
                mService.setDownLoadListener(null);
                mService.setCheckVersionCallBack(null);
            }
            mService = null;
            context = null;
        }
    };

    private void showNoWifiDownloadDialog(final boolean ifForceUpdate,final String filePath) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("下载新版本");
        builder.setMessage("检查到您的网络处于非wifi状态，下载新版本将消耗一定的流量，是否继续下载？");
        builder.setPositiveButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                unBindService();
                if (ifForceUpdate){
                    //强制退出
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }
        });
        builder.setNegativeButton("继续下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mService.doDownloadTask(filePath);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void startUpdate(){
        Log.d(TAG,"startUpdate");
        if (isCanceled)
            return;
        if (isWaitForDownload() || isWaitForDownload()){
            return;
        }
        if (mService == null && context != null){
            Intent intent = new Intent(context,VersionUpdateService.class);
            context.bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
            Log.d(TAG,"startUpdate--bindService");
        }
    }

    public void stopUpdate(){
        Log.d(TAG,"stopUpdate");
        unBindService();
    }

    private void cancel() {
        isCanceled = true;
        unBindService();
    }

    public void resetCancelFlag() {
        isCanceled = false;
    }

    private void unBindService(){
        if (isWaitForDownload() || isWaitForDownload()){
            return;
        }
        if (mService != null && !mService.isDownloading()){
            Log.d(TAG,"unbindService");
            context.unbindService(serviceConnection);
            mService = null;
        }
    }

    private boolean isWaitForUpdate() {
        return waitForUpdateDialog != null && waitForUpdateDialog.isShowing();
    }

    private boolean isWaitForDownload() {
        return progressDialog != null && progressDialog.isShowing();
    }


    public void setIfToastInfo(boolean ifToastInfo) {
        this.ifToastInfo = ifToastInfo;
    }


    public void setShowDialogOnstart(boolean showDialogOnstart) {
        this.showDialogOnstart = showDialogOnstart;
    }

    private void showToast(String info) {
        ToastUtil toastUtil = new ToastUtil(context);
        if (ifToastInfo)
            toastUtil.showToast(info);
    }
}
