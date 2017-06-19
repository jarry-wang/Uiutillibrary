package cn.join.android.net.cache;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.join.android.Logger;
import cn.join.android.net.appupdate.DownLoadListener;
import cn.join.android.net.http.RequestParams;
import cn.join.android.util.FileUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wangfujia on 17/4/5.
 * 利用okhttp请求网络数据，对okhttp的二次封装
 */

public class NetworkHelper {

    private final int DOWNLOAD_WAIT = 0X01;
    private final int DOWNLOAD_START = 0X01;
    private final int DOWNLOAD_IN = 0X01;
    private final int DOWNLOAD_SUCCESS = 0X01;
    private final int DOWNLOAD_ERROR = 0X01;

    private static NetworkHelper mNetworkHelper;
    private OkHttpClient mOkHttpClient;
    String result;
    final Handler uiHandler = new Handler();

    private NetworkHelper() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.readTimeout(30,TimeUnit.SECONDS);
        clientBuilder.connectTimeout(15,TimeUnit.SECONDS);
        clientBuilder.writeTimeout(40,TimeUnit.SECONDS);
        mOkHttpClient = clientBuilder.build();
    }

    public static NetworkHelper getInstance() {
        if (mNetworkHelper == null) {
            synchronized (NetworkHelper.class) {
                if (mNetworkHelper == null) {
                    mNetworkHelper = new NetworkHelper();
                }
            }
        }
        return mNetworkHelper;
    }

    public enum CallbackType {
        /**
         * 有缓存时,先回调缓存数据, 如果数据有更新会再次回调
         */
        CacheFirst,
        /**
         * 网络调用成功时,返回新数据, 如果没有网络或网络请求失败, 返回缓存数据
         */
        ForceUpdate,
        /**
         * 只有网络返回的数据和缓存里的不同时才会回调
         */
        CallbackOnChange,
        /**
         * 网络调用成功时,返回新数据
         */
        NoCache
    }

    public enum HttpType {
        Get,
        Post,
        Delete
    }

    /**
     * API 数据回调
     *
     * @author Zexu
     */
    public interface ResponseListener {
        void onResponse(String content);

        void onResponseNetError(String content,int code);
    }

    /**
     * 下载回调
     */
    public interface NetworkDownloadListener {
        void onWaiting();

        void onStarted();

        void onDownloadSuccess(File file);

        void onDownloading(int progress);

        void onError();
    }

    /**
     * 网络请求
     * @param context
     * @param type
     * @param callbackType
     * @param url
     * @param headerMap
     * @param params
     * @param listener
     */
    public void callJSONAPI(final Context context, final HttpType type, final CallbackType callbackType,
                                   final String url, final Map<String, String> headerMap, final RequestParams params, final ResponseListener listener) {
        result = null;
        /**
         * 如果是读取assets文件
         */
        if (url != null && url.startsWith("asset://")) {
            String path = Uri.parse(url).getEncodedPath();
            if (path.startsWith("/"))
                path = path.substring(1);
            postResultSuccess(getAssetContent(context, path), listener);
            return;
        }

        final JSONCache cache = new JSONCache(context);
        final String key = getUrlWithParams(url, params);
        Logger.d("getData-httpUrl:" + key);
        final String cacheData = cache.getCacheData(key);
        if (callbackType == CallbackType.CacheFirst) {
            if (listener != null) {
                postResultSuccess(cacheData, listener);
            }
        }

        Request.Builder requestBuilder = new Request.Builder();
        if (type == HttpType.Get) {
            requestBuilder.url(getUrlWithParams(url,params));
        } else if (type == HttpType.Post) {
            requestBuilder.url(url).post(getPostBodyWithParams(params));
        } else if (type == HttpType.Delete) {

        }
        requestBuilder.headers(getHeadersWithMap(headerMap));
        Call mCall = mOkHttpClient.newCall(requestBuilder.build());
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e(e.getMessage(), e);
                postResultFail(callbackType == CallbackType.ForceUpdate ? cacheData : null, 404, listener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //code >= 200 && code < 300
                    result = response.body().string();
                    if (callbackType == CallbackType.CallbackOnChange) {
                        if (cacheData != null && cacheData.equals(result))
                            return;
                    }
                    postResultSuccess(result, listener);
                    cache.addCacheData(key, result);
                }else {
                    result = "ERROR:" + response.code();
                    postResultFail(callbackType == CallbackType.ForceUpdate ? cacheData : null, response.code(), listener);
                }
//                Logger.d("NetworkHelper--result="+result);
            }
        });
    }

    public void readFile(final String filePath) {
        Request request = new Request.Builder().url(filePath).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public void download(final String url, final String saveDir, final NetworkDownloadListener downloadListener) {
        final UIDownloadListener uiDownloadListener = new UIDownloadListener(downloadListener);
        uiDownloadListener.onWaiting();
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                uiDownloadListener.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("NetworkHelper","---onResponse--"+ Process.myTid());
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String savePath = FileUtil.isExistDir(saveDir);

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, FileUtil.getNameFromUrl(url));
                    Logger.d("文件下载地址:"+file.getAbsolutePath());
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    uiDownloadListener.onStarted();
                    int seeProgress = 0;
                    while ((len = is.read(buf)) != -1){
                        fos.write(buf, 0 ,len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        if (progress > seeProgress + 1){
                            uiDownloadListener.onDownloading(progress);
                            seeProgress = progress;
                        }
                    }
                    fos.flush();
                    uiDownloadListener.onDownloadSuccess(file);
                } catch (IOException e) {
                    uiDownloadListener.onError();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos !=null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }

            }
        });
    }


    void postResultSuccess(final String result,final ResponseListener listener) {
        if (listener == null)
            return;
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
            try {
                listener.onResponse(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }

        });
    }

    void postResultFail(final String result, final int errorCode, final ResponseListener listener) {
        Logger.e("Network postResultFail :" + errorCode);
        if (listener == null)
            return;
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    listener.onResponseNetError(result,errorCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 获取请求头Headers
     * @param headerMap
     * @return
     */
    private static Headers getHeadersWithMap(Map<String, String> headerMap) {
        Headers headers = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headerMap != null) {
            Iterator<String> iterator = headerMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                headersBuilder.add(key,headerMap.get(key));
            }
        }
        headers = headersBuilder.build();
        return headers;
    }


    /**
     * Get请求拼接url与参数
     * @param url
     * @param params
     * @return
     */
    private static String getUrlWithParams(String url, RequestParams params) {
        if (params != null) {
            String paramString = params.toString();
            paramString = paramString.replaceAll("\\+", "%20");
            if (url != null && url.indexOf('?') == -1)
                url += "?" + paramString;
            else
                url += "&" + paramString;
        }
        Logger.d("NetworkHelper--getUrlWithParams--url="+url);
        return url;
    }

    /**
     * 获取Post请求body
     * @param params
     * @return
     */
    private static RequestBody getPostBodyWithParams(RequestParams params){
        RequestBody requestBody = null;
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.getParamsList().size() > 0){
            for (BasicNameValuePair item:params.getParamsList()
                 ) {
                builder.add(item.getName(),item.getValue());
            }
        }
        requestBody = builder.build();
        return requestBody;
    }

    /**
     * 读取assets文件信息
     * @param ctx
     * @param filename
     * @return
     */
    private static String getAssetContent(Context ctx, String filename) {
        try {
            InputStream is = ctx.getAssets().open(filename);
            String content = new String(IS2ByteArray(is));
            is.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] IS2ByteArray(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buf = new byte[8192];
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        int len;
        while ((len = bis.read(buf)) != -1) {
            bao.write(buf, 0, len);
        }
        return bao.toByteArray();
    }

    private class UIDownloadListener implements NetworkDownloadListener {

        private NetworkDownloadListener mDownloadListener;

        public UIDownloadListener(NetworkDownloadListener downLoadListener) {
            mDownloadListener = downLoadListener;
        }

        @Override
        public void onWaiting() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onWaiting();
                }
            });
        }

        @Override
        public void onStarted() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onStarted();
                }
            });
        }

        @Override
        public void onDownloadSuccess(final File file) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onDownloadSuccess(file);
                }
            });
        }

        @Override
        public void onDownloading(final int progress) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onDownloading(progress);
                }
            });
        }

        @Override
        public void onError() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onError();
                }
            });
        }
    }

}
