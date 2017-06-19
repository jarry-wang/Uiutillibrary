package cn.join.android.city;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.join.android.Logger;
import cn.join.android.R;
import cn.join.android.city.ScrollerNumberPicker.OnSelectListener;


/**
 * 城市Picker
 *
 * @author zd
 */
public class CityPicker extends LinearLayout {
    /**
     * 滑动控件
     */
    private ScrollerNumberPicker provincePicker;
    private ScrollerNumberPicker cityPicker;
    private ScrollerNumberPicker counyPicker;
    /**
     * 选择监听
     */
    private OnSelectingListener onSelectingListener;
    /**
     * 刷新界面
     */
    private static final int REFRESH_VIEW = 0x001;
    /**
     * 临时日期
     */
    private int tempProvinceIndex = -1;
    private int temCityIndex = -1;
    private int tempCounyIndex = -1;
    private Context context;

    public void setProvinceMap(HashMap<String, ProviceInfo> provinceMap) {
        this.provinceMap = provinceMap;
    }

    public void setCityMap(HashMap<String, HashMap<String, Cityinfo>> cityMap) {
        this.cityMap = cityMap;
    }

    public void setCounyMap(HashMap<String, HashMap<String, Counyinfo>> counyMap) {
        this.counyMap = counyMap;
    }

    private HashMap<String, ProviceInfo> provinceMap = new HashMap<String, ProviceInfo>();
    private HashMap<String, HashMap<String, Cityinfo>> cityMap = new HashMap<String, HashMap<String, Cityinfo>>();
    private HashMap<String, HashMap<String, Counyinfo>> counyMap = new HashMap<String, HashMap<String, Counyinfo>>();

    private CitycodeUtil citycodeUtil;
    private String city_string;

    public CityPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //getaddressinfo();
    }

    public CityPicker(Context context) {
        super(context);
        this.context = context;
        //getaddressinfo();
    }

    public void setPlace(String province, String city, String couny) {
        if (provinceMap.size() > 1) {
            List<String> provinces = citycodeUtil.getProvince(provinceMap);
            for (int i = 0; i < provinces.size(); i++) {
                if (province.equals(provinces.get(i))) {
                    provincePicker.setDefault(i);
                    if (cityMap.size() > i) {
                        List<String> citys = citycodeUtil.getCity(cityMap, citycodeUtil
                                .getProvince_list_code().get(i));
                        cityPicker.setData((ArrayList<String>) citys);
                        for (int j = 0; j < citys.size(); j++) {
                            if (city.equals(citys.get(j))) {
                                cityPicker.setDefault(j);
                                if (counyMap.size() > j) {
                                    List<String> counys = citycodeUtil.getCouny(counyMap, citycodeUtil
                                            .getCity_list_code().get(j));
                                    counyPicker.setData((ArrayList<String>) counys);
                                    if (TextUtils.isEmpty(couny)) {
                                        counyPicker.setDefault(0);
                                    } else {
                                        for (int k = 0; k < counys.size(); k++) {
                                            if (couny.equals(counys.get(k))) {
                                                counyPicker.setDefault(k);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    // 获取城市信息
    private void getaddressinfo() {
        // 读取城市信息string
        String area_str = FileUtil.readAssets(context, "pcc.json");
        if (TextUtils.isEmpty(area_str)) {
            Logger.d("file pcc.json not find");
            return;
        }
        Gson gson = new Gson();
        PccInfo pccInfo = gson.fromJson(area_str, PccInfo.class);
        provinceMap = pccInfo.province;
        cityMap = pccInfo.city;
        counyMap = pccInfo.district;
    }


    public void showPickerView() {
        this.removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.city_picker, this);
        citycodeUtil = CitycodeUtil.getSingleton();
//		// 获取控件引用
        provincePicker = (ScrollerNumberPicker) findViewById(R.id.province);

        cityPicker = (ScrollerNumberPicker) findViewById(R.id.city);
        counyPicker = (ScrollerNumberPicker) findViewById(R.id.couny);
        provincePicker.setData(citycodeUtil.getProvince(provinceMap));
        provincePicker.setDefault(0);
        cityPicker.setData(citycodeUtil.getCity(cityMap, citycodeUtil
                .getProvince_list_code().get(0)));
        cityPicker.setDefault(0);
        counyPicker.setData(citycodeUtil.getCouny(counyMap, citycodeUtil
                .getCity_list_code().get(0)));
        counyPicker.setDefault(0);
        provincePicker.setOnSelectListener(new OnSelectListener() {

            @Override
            public void endSelect(int id, String text) {
                if (text.equals("") || text == null)
                    return;
                if (tempProvinceIndex != id) {
                    String selectDay = cityPicker.getSelectedText();
                    if (selectDay == null || selectDay.equals(""))
                        return;
                    String selectMonth = counyPicker.getSelectedText();
                    if (selectMonth == null || selectMonth.equals(""))
                        return;
                    // 城市数组
                    cityPicker.setData(citycodeUtil.getCity(cityMap,
                            citycodeUtil.getProvince_list_code().get(id)));
                    cityPicker.setDefault(0);
                    counyPicker.setData(citycodeUtil.getCouny(counyMap,
                            citycodeUtil.getCity_list_code().get(0)));
                    counyPicker.setDefault(0);
                    int lastDay = Integer.valueOf(provincePicker.getListSize());
                    if (id > lastDay) {
                        provincePicker.setDefault(lastDay - 1);
                    }
                }
                tempProvinceIndex = id;
                Message message = new Message();
                message.what = REFRESH_VIEW;
                handler.sendMessage(message);
            }

            @Override
            public void selecting(int id, String text) {
            }
        });
        cityPicker.setOnSelectListener(new OnSelectListener() {

            @Override
            public void endSelect(int id, String text) {
                if (text.equals("") || text == null)
                    return;
                if (temCityIndex != id) {
                    String selectDay = provincePicker.getSelectedText();
                    if (selectDay == null || selectDay.equals(""))
                        return;
                    String selectMonth = counyPicker.getSelectedText();
                    if (selectMonth == null || selectMonth.equals(""))
                        return;
                    counyPicker.setData(citycodeUtil.getCouny(counyMap,
                            citycodeUtil.getCity_list_code().get(id)));
                    counyPicker.setDefault(0);
                    int lastDay = Integer.valueOf(cityPicker.getListSize());
                    if (id > lastDay) {
                        cityPicker.setDefault(lastDay - 1);
                    }
                }
                temCityIndex = id;
                Message message = new Message();
                message.what = REFRESH_VIEW;
                handler.sendMessage(message);
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
        counyPicker.setOnSelectListener(new OnSelectListener() {

            @Override
            public void endSelect(int id, String text) {
                if (text.equals("") || text == null)
                    return;
                if (tempCounyIndex != id) {
                    String selectDay = provincePicker.getSelectedText();
                    if (selectDay == null || selectDay.equals(""))
                        return;
                    String selectMonth = cityPicker.getSelectedText();
                    if (selectMonth == null || selectMonth.equals(""))
                        return;
                    // 城市数组
//					city_code_string = citycodeUtil.getCouny_list_code()
                    int lastDay = Integer.valueOf(counyPicker.getListSize());
                    if (id > lastDay) {
                        counyPicker.setDefault(lastDay - 1);
                    }
                }
                tempCounyIndex = id;
                Message message = new Message();
                message.what = REFRESH_VIEW;
                handler.sendMessage(message);
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //showPickerView();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_VIEW:
                    if (onSelectingListener != null)
                        onSelectingListener.selected(true);
                    break;
                default:
                    break;
            }
        }

    };

    public void setOnSelectingListener(OnSelectingListener onSelectingListener) {
        this.onSelectingListener = onSelectingListener;
    }

//	public String getCity_code_string() {
//		return city_code_string;
//	}

    public String getCity_string() {
        city_string = provincePicker.getSelectedText()
                + " " + cityPicker.getSelectedText() + " " + counyPicker.getSelectedText();
        return city_string;
    }

    public String getProvince() {
        return provincePicker.getSelectedText();
    }

    public String getCity() {
        return cityPicker.getSelectedText();
    }

    public String getCouny() {
        return counyPicker.getSelectedText();
    }

    public interface OnSelectingListener {
        public void selected(boolean selected);
    }
}
