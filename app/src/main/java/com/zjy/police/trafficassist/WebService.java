package com.zjy.police.trafficassist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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

    public static Map<String, String> getAccidentLoc(double x, double y) {
        path = "http://" + server_IP + "/trafficassist/policeApi/getAccLoc.php";
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
            Log.d("json", "info_not_ok");
            return null;
        }
    }

    public static ArrayList<String> getAccidentTags(String username) {
        path = "http://" + server_IP + "/trafficassist/policeApi/getAccInfo.php";
        path = path + "?username=" + username;
        String result = Connect();
        JSONObject json = null;
        ArrayList<String> accTags = new ArrayList<>();
        try {
            json = new JSONObject(result);
            JSONArray tags = json.getJSONArray("tags");
            for(int i = 0; i < tags.length(); i++) {
                String each_tag = (String) tags.get(i);
                accTags.add(each_tag);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("json", "tags_not_ok");
        }
        return accTags;
    }

    public static ArrayList<String> getInfo(String username) {
        path = "http://" + server_IP + "/trafficassist/policeApi/getAccInfo.php";
        path = path + "?username=" + username;
        String result = Connect();
        JSONObject json = null;
        ArrayList<String> picpath = new ArrayList<>();
        try {
            json = new JSONObject(result);
            JSONArray filenames = json.getJSONArray("filenames");
            for(int i = 0; i < filenames.length(); i++) {
                String each_tag = (String) filenames.get(i);
                picpath.add(each_tag);
            }
            return picpath;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("json", "path_not_ok");
            return null;
        }
    }

    public static ArrayList<Bitmap> getAccidentPics(String username) {
        path = "http://" + server_IP + "/trafficassist/policeApi/getAccInfo.php";
        path = path + "?username=" + username;
        String result = Connect();
        JSONObject json = null;
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        ArrayList<String> picpath = new ArrayList<>();
        try {
            json = new JSONObject(result);
            JSONArray filenames = json.getJSONArray("filenames");
            for(int i = 0; i < filenames.length(); i++) {
                String each_tag = (String) filenames.get(i);
                picpath.add(each_tag);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("json", "path_not_ok");
        }
        for(int i = 0; i < picpath.size(); i++)
            bitmaps.add(getLocalOrNetBitmap("http://192.168.31.100/TrafficAssist/AccidentImage/" + picpath.get(i)));
        return bitmaps;
    }

    private static Bitmap getLocalOrNetBitmap(String url) {
        int IO_BUFFER_SIZE = 2*1024;
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try
        {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            byte[] b = new byte[IO_BUFFER_SIZE];
            int read;
            while ((read = in.read(b)) != -1)
                out.write(b, 0, read);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
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
