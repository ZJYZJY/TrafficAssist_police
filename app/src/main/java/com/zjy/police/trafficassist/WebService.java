package com.zjy.police.trafficassist;

import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 73958 on 2016/10/23.
 */

public class WebService {

    /**
     * 对应servlet的URL
     */
    private static String path;
    private static String server_IP = "192.168.31.100";

    public static Map<String, String> getAccidentInfo(double x, double y) {
        path = "http://" + server_IP + "/trafficassist/getInfoForPolice.php";
        path = path + "?longitude=" + x + "&latitude=" + y;
        Map<String, String> map = new HashMap<>();
        String result = Connect();
        try {
            if(result != null) {
                JSONObject accidentInfo = new JSONObject(result);
                map.put("username", (String) accidentInfo.get("username"));
                map.put("longitude", (String) accidentInfo.get("longitude"));
                map.put("latitude", (String) accidentInfo.get("latitude"));
                return map;
            }else
                return null;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("json", "json_not_ok");
            return null;
        }
    }

    private static String Connect() {

        HttpURLConnection conn = null;
        InputStream is = null;
        InputStreamReader isr;
        BufferedReader br;

        try {
            conn = (HttpURLConnection) new URL(path).openConnection();

            conn.setConnectTimeout(60000); // 设置超时时间
            conn.setReadTimeout(60000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST"); // 设置获取信息方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line, info = "";
                while ((line = br.readLine()) != null) {
                    info = info + line;
                }
                br.close();
                isr.close();
                is.close();
                Log.d("返回数据：", info);
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 意外退出时进行连接关闭保护
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
