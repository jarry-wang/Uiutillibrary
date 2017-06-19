package cn.join.android.util;


import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class StringUtils {
    private static final String ENCODING = "UTF-8";

    /**
     * 加密为MD5
     *
     * @param content
     * @return
     */
    public static String getMD5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            return getHashString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }
        return builder.toString().toLowerCase();
    }

    /**
     * 检验手机号码正确性
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        boolean flag = false;
        try {
            //13********* ,15********,18*********
            Pattern p = Pattern
                    .compile("^((1[3,7,8][0-9])|(15[^4,\\D])|(18[0,2,5-9]))\\d{8}$");
            Matcher m = p.matcher(mobiles);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 检验是否是邮箱
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 检验字符串是否为空
     */
    public static boolean isNull(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        return false;
    }

    public static boolean isNotNull(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getKhStr(String str) {
        if (isNull(str)) {
            return "";
        } else {
            return "(" + str + ")";
        }
    }

    /**
     * 获得10位随机数
     */
    public static String getTenNum() {
        int a[] = new int[10];
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < a.length; i++) {
            a[i] = (int) (10 * (Math.random()));
            buf.append(a[i]);
        }
        return buf.toString();
    }

    /**
     * HmacSHA1签名
     *
     * @param baseString
     * @param consumerKeySecret
     * @param tokenSecret
     * @return
     * @throws IllegalStateException
     * @throws UnsupportedEncodingException
     */
    public static String generateSignature(String baseString, String consumerKeySecret, String tokenSecret) throws IllegalStateException, UnsupportedEncodingException {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec;
            String oauthSignature = consumerKeySecret + "&"
                    + ((tokenSecret != null) ? tokenSecret : "");
            spec = new SecretKeySpec(oauthSignature.getBytes(), "HmacSHA1");
            mac.init(spec);
            byteHMAC = mac.doFinal(baseString.getBytes(ENCODING));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ignore) {
            // should never happen
        }
        return Base64Util.encode(byteHMAC);
    }


    /**
     * 文本控件字符串指定索引设置指定颜色
     *
     * @param txtView
     * @param color
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static void setTextViewColorSpan(TextView txtView, int color, int startIndex, int endIndex) {
        SpannableString spannable = new SpannableString(txtView.getText().toString());
        spannable.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtView.setText(spannable);
    }

    public static String getMoneyNum(String value) {
        BigDecimal tmp = new BigDecimal(value);
        return "￥" + String.format("%.2f", tmp);
    }

    public static String getMoneyNum(Double value) {
        BigDecimal tmp = new BigDecimal(value);
        return "￥" + String.format("%.2f", tmp);
    }

    public static String getOneDecimal(String value) {
        BigDecimal tmp = new BigDecimal(value);
        return String.format("%.1f", tmp);
    }

    public static String getZeroDecimal(String value) {
        BigDecimal tmp = new BigDecimal(value);
        return String.format("%.0f", tmp);
    }

    public static String getDiscountNum(String value) {
        if (value.contains(".")) {
            if (value.split("\\.")[1].startsWith("0")) {
                return value.split("\\.")[0];
            }
        }
        return value;
    }

    public static String getEdText(EditText editText) {

        return editText.getText().toString().trim();
    }


//		public static String getWmoney(String moneyStr){
//			double money = NumberFormatUtil.parseDouble(moneyStr);
//			double wMoney = money;
//			if(money>=1000000){
//				wMoney = money/10000;
//				return getMoneyNum(wMoney) + "万元";
//			}
//			return money + "元";
//		}

//    public static String getPrice(String price) {
//        if (price.contains(".")) {
//            price = price.split("\\.")[0];
//        }
//        return "￥" + price;
//    }

    public static String getPrice2(String price) {
        return "￥" + NumberFormatUtil.parseDouble(price);
    }

    public static String getPrice3(String price) {
        return NumberFormatUtil.parseDouble(price) + "元";
    }

    /**
     * 根据优惠券类型,返回优惠券描述
     *
     * @param type      优惠券类型
     * @param variables 返回字面量
     * @return
     */
    public static String formatCouponInfo(String type, String variables) {
        if ("4".equals(type)) {
            return variables + "折";
        }
        return "￥" + variables;
    }

    public static String getStarTel(String tel) {
        String telStr = "";
        if (tel.length() >= 7) {
            telStr = tel.substring(0, 3) + "****" + tel.substring(7);
        }
        return telStr;
    }

    public static String cutToFourStr(String str) {

        String regex = "(.{4})";
        str = str.replaceAll(regex, "$1 ");
        return str;
    }

    public static String getReplaceStr(int strRes, String text, Context context) {

        String str = String.format(context.getResources().getString(strRes), text);
        return str;
    }

    public static String getReplaceStr(int strRes, String text1, String text2, Context context) {

        String str = String.format(context.getResources().getString(strRes), text1, text2);
        return str;
    }

    public static String formatPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        try {
            return phone.substring(0, 3) + " " + phone.substring(3, 7) + " " + phone.substring(7);
        } catch (Exception e) {
            return phone;
        }
    }
}
