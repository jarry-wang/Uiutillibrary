package cn.join.android.city;

import java.io.Serializable;

public class Counyinfo implements Serializable {

	private String districtName;

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	@Override
	public String toString() {
		return "Counyinfo [districtName=" + districtName+ "]";
	}

}
