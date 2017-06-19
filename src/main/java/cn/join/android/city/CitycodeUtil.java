package cn.join.android.city;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * 城市代码
 * 
 * @author zd
 * 
 */
public class CitycodeUtil {

	private ArrayList<String> province_list = new ArrayList<String>();
	private ArrayList<String> city_list = new ArrayList<String>();
	private ArrayList<String> couny_list = new ArrayList<String>();
	public ArrayList<String> province_list_code = new ArrayList<String>();
	public ArrayList<String> city_list_code = new ArrayList<String>();
	public ArrayList<String> couny_list_code = new ArrayList<String>();
	/** 单例 */
	public static CitycodeUtil model;
	private Context context;

	private CitycodeUtil() {
	}

	public ArrayList<String> getProvince_list_code() {
		return province_list_code;
	}

	public ArrayList<String> getCity_list_code() {
		return city_list_code;
	}

	public void setCity_list_code(ArrayList<String> city_list_code) {
		this.city_list_code = city_list_code;
	}

	public ArrayList<String> getCouny_list_code() {
		return couny_list_code;
	}

	public void setCouny_list_code(ArrayList<String> couny_list_code) {
		this.couny_list_code = couny_list_code;
	}

	public void setProvince_list_code(ArrayList<String> province_list_code) {

		this.province_list_code = province_list_code;
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static CitycodeUtil getSingleton() {
		if (null == model) {
			model = new CitycodeUtil();
		}
		return model;
	}


	public ArrayList<String> getProvince(HashMap<String, ProviceInfo>  provice) {
		if (province_list_code.size() > 0) {
			province_list_code.clear();
		}
		if (province_list.size() > 0) {
			province_list.clear();
		}
		Iterator iter = provice.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			province_list.add(provice.get(key).getName());
			province_list_code.add(key);
		}
		return province_list;

	}

	public ArrayList<String> getCity(
			HashMap<String, HashMap<String, Cityinfo>> cityHashMap, String provicecode) {
		if (city_list_code.size() > 0) {
			city_list_code.clear();
		}
		if (city_list.size() > 0) {
			city_list.clear();
		}
		HashMap<String, Cityinfo> city = cityHashMap.get(provicecode);
		Iterator iter = city.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			city_list.add(city.get(key).getCityName());
			city_list_code.add(key);
		}
		return city_list;

	}

	public ArrayList<String> getCouny(
			HashMap<String, HashMap<String, Counyinfo>> cityHashMap, String citycode) {
		HashMap<String, Counyinfo> couny = null;
		if (couny_list_code.size() > 0) {
			couny_list_code.clear();

		}
		if (couny_list.size() > 0) {
			couny_list.clear();
		}
		if (couny == null) {
			couny = new HashMap<String, Counyinfo>();
		} else {
			couny.clear();
		}
		couny = cityHashMap.get(citycode);
		Iterator iter = couny.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			couny_list.add(couny.get(key).getDistrictName());
			couny_list_code.add(key);
		}
		return couny_list;

	}
}
