package pxfandroid.coolweather.util;

/**
 * Created by Administrator on 2016/10/11.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
