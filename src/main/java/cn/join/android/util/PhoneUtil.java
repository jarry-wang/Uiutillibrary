package cn.join.android.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PhoneUtil {

	/**
     * 调用打电话
     * @param phoneNum
     */
    public static void goToPhone(Context context,String phoneNum){
		Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phoneNum)); 
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    context.startActivity(intent);
	}
    /**
     * 调用发短信
     * @param phoneNo
     * @param content
     */
    public static void sendSms(Context context,String phoneNo,String content) {
		// TODO Auto-generated method stub
		Uri uri = Uri.parse("smsto:"+phoneNo);            
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);            
		it.putExtra("sms_body", content);            
		context.startActivity(it);  
	}
}
