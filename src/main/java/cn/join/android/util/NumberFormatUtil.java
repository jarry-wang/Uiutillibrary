package cn.join.android.util;

import java.math.BigDecimal;

/**
 * Created by YHB on 2016/2/24.
 */
public class NumberFormatUtil {

    public static double parseDouble(String num) {
        double value;
        try {
            Double.parseDouble(num);
        } catch (Exception e) {
            num = "0";
        } finally {
            value = Double.parseDouble(num);
        }
        return formatNumber(value);
    }

    public static double formatNumber(double num) {
        BigDecimal bd = new BigDecimal(num);
        double v = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return v;
    }
}
