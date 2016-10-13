package pxfandroid.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import pxfandroid.coolweather.receiver.AutoUpdateReceiver;
import pxfandroid.coolweather.util.HttpCallbackListener;
import pxfandroid.coolweather.util.HttpUtil;
import pxfandroid.coolweather.util.Utility;

/**
 * Created by Administrator on 2016/10/13.
 */
public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Service", "执行了一次");
                UpdateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int aHour = 3000;
        //   long triggerAttime = SystemClock.elapsedRealtime() + aHour;
        long triggerAttime = SystemClock.elapsedRealtime() + 10 * 1000;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAttime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void UpdateWeather() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String cityName = preferences.getString("city_name", "");
        final String countyName = preferences.getString("county_name", "");
        String address = "http://flash.weather.com.cn/wmaps/xml/" + countyName + ".xml";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    Utility.handleWeatherResponse(AutoUpdateService.this, response, cityName, countyName);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
