package com.infinite.downloaderapp.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/28 - 12:09
 * Description: Class description
 */
public class ProvinceEntity implements JsonableEnity<ProvinceEntity> {
    private static final String KEY_PROVINCECODE = "provinceCode";
    private static final String KEY_PROVINCENAME = "provinceName";
    private static final String KEY_PROVINCEID = "provinceId";
    private static final String KEY_CITYLIST = "cityList";


    private String provinceCode;
    private String provinceName;
    private int provinceId;
    private List<CityEntity> cityList;


    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public List<CityEntity> getCityList() {
        return cityList;
    }

    public void setCityList(List<CityEntity> cityList) {
        this.cityList = cityList;
    }

    public static ProvinceEntity toObject(String jsonString) {
        JSONObject object = null;
        try {
            object = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object != null) {
            ProvinceEntity entity = new ProvinceEntity();
            entity.toObject(object);
            return entity;
        }
        return null;
    }

    @Override
    public ProvinceEntity toObject(JSONObject object) {
        if (object != null) {
            provinceCode = object.optString(KEY_PROVINCECODE);
            provinceName = object.optString(KEY_PROVINCENAME);
            provinceId = object.optInt(KEY_PROVINCEID);
            JSONArray jsonArray = object.optJSONArray(KEY_CITYLIST);
            if (jsonArray != null && jsonArray.length() > 0) {
                if (cityList == null) {
                    cityList = new ArrayList<>(32);
                }
                for (int i = 0, length = jsonArray.length(); i < length; i++) {
                    CityEntity cityEntity = new CityEntity();
                    cityList.add(cityEntity.toObject(jsonArray.optJSONObject(i)));
                }
            }
            return this;
        }
        return null;
    }

    @Override
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_PROVINCECODE, provinceCode);
            jsonObject.put(KEY_PROVINCENAME, provinceName);
            jsonObject.put(KEY_PROVINCEID, provinceId);
            JSONArray jsonArray = new JSONArray();
            if (cityList != null && cityList.size() > 0) {
                for (CityEntity cityEntity : cityList) {
                    jsonArray.put(cityEntity);
                }
            }
            jsonObject.put(KEY_CITYLIST, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public String toString() {
        return "ProvinceEntity{" +
                "provinceCode='" + provinceCode + '\'' +
                ", provinceName='" + provinceName + '\'' +
                ", provinceId=" + provinceId +
                ", cityList=" + cityList +
                '}';
    }
}
