package cn.join.android.city;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by mlfdev1 on 2015/11/25.
 */
public class PccInfo implements Serializable{
    public HashMap<String, ProviceInfo>  province;
    public HashMap<String, HashMap<String, Cityinfo>>  city;
    public HashMap<String, HashMap<String, Counyinfo>>  district;
}
