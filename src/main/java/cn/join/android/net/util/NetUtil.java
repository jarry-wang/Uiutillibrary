package cn.join.android.net.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by wangfujia on 17/3/30.
 */

public class NetUtil {
    public NetUtil() {
    }

    /**
     * 判断网络是否连接
     * @param context
     * @return
     */
    public static boolean isConnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 判断是否wifi连接
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() != null){
            return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    /**
     * 判断是否手机连接
     * @param context
     * @return
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() != null){
            return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    /**
     * 获取网络类型
     * @param context
     * @return
     */
    public static int getNetType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && cm.getActiveNetworkInfo() != null){
            return cm.getActiveNetworkInfo().getType();
        }
        return -1;
    }

    /**
     * 获取手机IP
     * @return
     */
    public static String getPhoneIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 打开网络设置界面
     * @param context
     */
    public static void openSetting(Activity context){
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        context.startActivityForResult(intent, 0);
    }

}
