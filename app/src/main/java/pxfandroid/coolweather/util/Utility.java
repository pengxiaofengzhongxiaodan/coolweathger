package pxfandroid.coolweather.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import pxfandroid.coolweather.db.CoolWeatherDB;
import pxfandroid.coolweather.model.City;
import pxfandroid.coolweather.model.County;
import pxfandroid.coolweather.model.Province;

/**
 * Created by Administrator on 2016/10/11.
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            List<Province> provinces = new ArrayList<Province>();
            try {
                provinces = readXML(response);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (provinces.isEmpty() == false) {
                for (Province p : provinces) {
                    coolWeatherDB.saveProvince(p);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            List<City> citys = new ArrayList<City>();
            try {
                citys = readCityXML(response);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (citys.isEmpty() == false) {
                for (City p : citys) {
                    coolWeatherDB.saveCity(p);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            List<County> counties = new ArrayList<County>();
            try {
                counties = readCountryXML(response);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (counties.isEmpty() == false) {
                for (County p : counties) {
                    coolWeatherDB.saveCounty(p);
                }
                return true;
            }
        }
        return false;
    }

    public static List<Province> readXML(String response) throws XmlPullParserException, IOException {
        ByteArrayInputStream inputstream = new ByteArrayInputStream(response.getBytes());
        List<Province> provinces = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputstream, "UTF-8");
        int eventCode = parser.getEventType();
        Province province = null;
        while (eventCode != XmlPullParser.END_DOCUMENT) {
            switch (eventCode) {
                case XmlPullParser.START_DOCUMENT:
                    provinces = new ArrayList<Province>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("city".equals(parser.getName())) {
                        province = new Province();
                        province.setProvinceName(parser.getAttributeValue(0));
                        province.setProvinceCode(parser.getAttributeValue(1));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("city".equals(parser.getName()) && province != null) {
                        provinces.add(province);
                        province = null;
                    }
                    break;
                default:
                    break;
            }
            eventCode = parser.next();
        }

        return provinces;
    }

    public static List<City> readCityXML(String response) throws XmlPullParserException, IOException {
        ByteArrayInputStream inputstream = new ByteArrayInputStream(response.getBytes());
        List<City> cities = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputstream, "UTF-8");
        int eventCode = parser.getEventType();
        City city = null;
        String provinceName = "";
        while (eventCode != XmlPullParser.END_DOCUMENT) {
            switch (eventCode) {
                case XmlPullParser.START_DOCUMENT:
                    cities = new ArrayList<City>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("city".equals(parser.getName())) {
                        city = new City();
                        city.setId(Integer.parseInt(parser.getAttributeValue(17)));
                        city.setCityName(parser.getAttributeValue(2));
                        city.setCityCode(parser.getAttributeValue(5));
                        city.setProvinceName(provinceName);
                    } else {
                        provinceName = parser.getName();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("city".equals(parser.getName()) && city != null) {
                        cities.add(city);
                        city = null;
                    }
                    break;
                default:
                    break;
            }
            eventCode = parser.next();
        }

        return cities;
    }

    public static List<County> readCountryXML(String response) throws XmlPullParserException, IOException {
        ByteArrayInputStream inputstream = new ByteArrayInputStream(response.getBytes());
        List<County> counties = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputstream, "UTF-8");
        int eventCode = parser.getEventType();
        County county = null;
        String cityName = "";
        while (eventCode != XmlPullParser.END_DOCUMENT) {
            switch (eventCode) {
                case XmlPullParser.START_DOCUMENT:
                    counties = new ArrayList<County>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("city".equals(parser.getName())) {
                        county = new County();
                        county.setCountyCode(parser.getAttributeValue(5));
                        county.setCityId(Integer.parseInt(parser.getAttributeValue(17)));
                        county.setCountyName(parser.getAttributeValue(2));
                        county.setCityName(cityName);
                    } else {
                        cityName = parser.getName();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("city".equals(parser.getName()) && county != null) {
                        counties.add(county);
                        county = null;
                    }
                    break;
                default:
                    break;
            }
            eventCode = parser.next();
        }

        return counties;

    }

    public static void handleWeatherResponse(Context context, String response, String county_code,String cityCode) throws XmlPullParserException, IOException {
        ByteArrayInputStream inputstream = new ByteArrayInputStream(response.getBytes());
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputstream, "UTF-8");
        int eventCode = parser.getEventType();
        boolean isMatch = false;
        String cityName = "";
        String minTemp = "";
        String maxTemp = "";
        String weatherDesp = "";
        String publishTime = "";
        while (eventCode != XmlPullParser.END_DOCUMENT) {
            switch (eventCode) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if ("city".equals(parser.getName())) {
                        if (parser.getAttributeValue(2).equals(county_code)) {
                            isMatch = true;
                            cityName = parser.getAttributeValue(2);
                            minTemp = parser.getAttributeValue(9);
                            maxTemp = parser.getAttributeValue(10);
                            publishTime = parser.getAttributeValue(16);
                            weatherDesp = parser.getAttributeValue(8) + "--" + parser.getAttributeValue(12);
                            saveWeatherInfo(context,cityCode,cityName,minTemp,maxTemp,weatherDesp,publishTime);
                        }
                    } else {

                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
            }
            if (isMatch) {
                break;
            }
            eventCode = parser.next();
        }
    }

    public static void saveWeatherInfo(Context context,String countyName, String cityName, String minTemp, String maxTemp, String weatherDesp, String publishTime) {
//      SimpleDateFormat sdf=new SimpleDateFormat("")
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("county_name", countyName);
        editor.putString("temp1", minTemp);
        editor.putString("temp2", maxTemp);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", publishTime);
        editor.commit();
    }

}
