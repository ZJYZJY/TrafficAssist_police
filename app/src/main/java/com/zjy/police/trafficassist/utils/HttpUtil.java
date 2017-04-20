package com.zjy.police.trafficassist.utils;

import com.amap.api.maps.model.LatLng;
import com.zjy.police.trafficassist.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * com.zjy.trafficassist.utils
 * Created by 73958 on 2017/4/6.
 */

public class HttpUtil {
    private static final String SERVER_IP = "120.27.130.203";

    private static final String PORT = "8001";

    private static final String PATH = "http://" + SERVER_IP + ":" + PORT + "/";

    public static final int SUCCESS = 200;

    public static final int FAIL = 400;

    public static final int EMPTY = 201;

    private static Retrofit instance;

    public static synchronized APIService create() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(PATH)
                    .addConverterFactory(GsonConverterFactory.create())  // Gson转换器
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava转换器
                    .build();
        }
        return instance.create(APIService.class);
    }

    /**
     * 获取请求结果的状态码
     * @param str 服务器返回的数据
     */
    public static int stateCode(String str){
        try {
            JSONObject json = new JSONObject(str);
            return json.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return FAIL;
    }

    /**
     * 将File对象转换成上传时用的Part对象
     * @param filePaths 图片文件路径
     * @return parts 上传用的parts数组
     */
    public static List<MultipartBody.Part> files2Parts(String key, List<String> filePaths) {
        List<MultipartBody.Part> parts = new ArrayList<>(filePaths.size());
        int count = 1;
        for (String filePath : filePaths) {
            File file = new File(filePath);
            // 根据类型及File对象创建RequestBody（okhttp的类）
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            // 将RequestBody封装成MultipartBody.Part类型（同样是okhttp的）
            MultipartBody.Part part = MultipartBody.Part.
                    createFormData(key, file.getName(), requestBody);
            count++;
            // 添加进集合
            parts.add(part);
        }
        return parts;
    }

    /**
     * 直接添加文本类型的Part到的MultipartBody的Part集合中
     * @param parts Part集合
     * @param key 参数名（name属性）
     * @param value 文本内容
     * @param position 插入的位置
     */
    public static void addTextPart(List<MultipartBody.Part> parts,
                                   String key, String value, int position) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), value);
        MultipartBody.Part part = MultipartBody.Part.createFormData(key, null, requestBody);
        parts.add(position, part);
    }

    public interface APIService {

        /**
         * 用户登录接口
         * @param user 用户对象
         */
        @POST(APIPath.LOGIN)
        Call<ResponseBody> login(@Body User user);

        /**
         * 用户注册接口
         * @param user 用户对象
         */
        @POST(APIPath.SIGN_UP)
        Call<ResponseBody> signUp(@Body User user);

        /**
         * 获取登录IM服务器所用的token
         * @param user 输入用户名和用户昵称
         */
        @GET(APIPath.GET_TOKEN)
        Call<ResponseBody> getToken(@QueryMap Map<String, String> user);

        /**
         * 获取事故发生的位置
         * @param location 上传交警的位置信息
         */
        @GET(APIPath.GET_ACCIDENT_LOC)
        Call<ResponseBody> getAccidentLoc(@Body LatLng location);

    }

    private class APIPath{
        
        private static final String LOGIN = "trafficassist/police/login.php";

        private static final String SIGN_UP = "trafficassist/police/signup.php";

        private static final String GET_TOKEN = "trafficassist/IMServerApi/getToken.php";

        private static final String GET_ACCIDENT_LOC = "trafficassist/police/getAccLoc.php";
    }
}
