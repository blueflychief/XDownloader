package com.infinite.downloaderapp.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/28 - 12:20
 * Description: Class description
 */
public class CityEntity implements JsonableEnity<CityEntity> {
    private static final String KEY_CITYNAME = "cityName";
    private static final String KEY_CITYCODE = "cityCode";
    private static final String KEY_CITYID = "cityId";
    private String cityName;
    private String cityCode;
    private int cityId;


    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public static CityEntity toObject(String jsonString) {
        JSONObject object = null;
        try {
            object = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object != null) {
            CityEntity entity = new CityEntity();
            entity.toObject(object);
            return entity;
        }
        return null;
    }

    @Override
    public CityEntity toObject(JSONObject object) {
        if (object != null) {
            cityName = object.optString(KEY_CITYNAME);
            cityCode = object.optString(KEY_CITYCODE);
            cityId = object.optInt(KEY_CITYID);
            return this;
        }
        return null;
    }

    @Override
    public String toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(KEY_CITYNAME, cityName);
            object.put(KEY_CITYCODE, cityCode);
            object.put(KEY_CITYID, cityId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }


    @Override
    public String toString() {
        return "CityEntity{" +
                "cityName='" + cityName + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", cityId=" + cityId +
                '}';
    }
}
