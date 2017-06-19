package cn.join.android.city;

import java.io.Serializable;

public class Cityinfo implements Serializable {

	private String cityName;

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	@Override
	public String toString() {
		return "Cityinfo [cityName=" + cityName+ "]";
	}

}
