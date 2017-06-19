package cn.join.android.net.appupdate;

import android.os.Handler;
import android.os.Message;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.join.android.net.cache.NetworkHelper;
import okhttp3.Request;

/**
 * Created by wangfujia on 17/3/31.
 */

public class FileUtil {

    private final String TAG = "FileUtil";

    interface ReadFileCallBack {
        void readFilesuccess(AppVersionInfo info);
        void readFail();
    }

    private ReadFileCallBack callBack;

    public FileUtil(ReadFileCallBack callBack) {
        this.callBack = callBack;
    }

    public static final int GET_FILEINFO_SUCCESS = 0x01;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void getVersionInfo(final String filePath)
    {
        //在子线程中获取服务器的数据
        Thread thread = new Thread(){
            @Override
            public void run() {
                //1:确定地址
                try {
                    URL url = new URL(filePath);
                    //建立连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    conn.setRequestMethod("GET");
                    //设置请求超时时间
                    conn.setConnectTimeout(10*1000);
                    //设置读取超时时间
                    conn.setReadTimeout(10*1000);
                    //判断是否获取成功
                    if(conn.getResponseCode() == 200)
                    {
                        //获得输入流
                        InputStream is = conn.getInputStream();
                        //解析输入流中的数据
                        parseXmlInfo(is);

                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    if (callBack != null){
                        callBack.readFail();
                    }
                }
            }
        };

        //启动线程
        thread.start();

    }

    private void parseXmlInfo(InputStream is)
    {
        AppVersionInfo appInfo = null;
        /*我们用pull解析器解析xml文件*/

        //1.先拿到pull解析器
        XmlPullParser xParser = Xml.newPullParser();

        try {
            xParser.setInput(is, "utf-8");
            //获取事件的类型
            int eventType = xParser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("app".equals(xParser.getName())) {
                            //new出一个news的对象
                            appInfo = new AppVersionInfo();
                        }
                        else if ("versionName".equals(xParser.getName())) {
                            appInfo.versionName = xParser.nextText();
                        }
                        else if ("versionCode".equals(xParser.getName())) {
                            appInfo.versionCode = Integer.parseInt(xParser.nextText());
                        }
                        else if ("detail".equals(xParser.getName())) {
                            appInfo.versionInfo = xParser.nextText();
                        }
                        else if ("ifForceUpdate".equals(xParser.getName())) {
                            appInfo.ifForceUpdate = Integer.parseInt(xParser.nextText());
                        }
                        else if ("filePath".equals(xParser.getName())) {
                            appInfo.filePath = xParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("app".equals(xParser.getName()))
                        {
                            //结束
                            if (callBack != null){
                                callBack.readFilesuccess(appInfo);
                            }
                        }
                        break;
                }

                eventType = xParser.next();
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (callBack != null){
                callBack.readFail();
            }
        }

    }
}
