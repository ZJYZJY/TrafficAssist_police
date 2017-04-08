package com.zjy.police.trafficassist.helper;

import android.content.Context;
import android.widget.Toast;

import com.zjy.police.trafficassist.App;
import com.zjy.police.trafficassist.utils.HttpUtil;
import com.zjy.police.trafficassist.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zjy.police.trafficassist.UserStatus.USER;
import static com.zjy.police.trafficassist.utils.HttpUtil.SUCCESS;

/**
 * com.zjy.trafficassist.utils
 * Created by 73958 on 2017/3/21.
 */

public class ConnectIMServerHelper {

    /**
     * 单例模式
     * @return sInstance
     */
    public static ConnectIMServerHelper getInstance(){
        return ConnectIMServerHolder.sInstance;
    }

    private static class ConnectIMServerHolder{
        private static final ConnectIMServerHelper sInstance = new ConnectIMServerHelper();
    }

    public void connectIMServer(final Context context, String token) {

        if (context.getApplicationInfo().packageName.equals(App.getCurProcessName(context.getApplicationContext()))) {

            RongIM.connect(token, new RongIMClient.ConnectCallback() {

                /**
                 * Token 错误。可以从下面两点检查
                 * 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                @Override
                public void onTokenIncorrect() {
                    LogUtil.d("IMServer", "--onTokenIncorrect--");

                    Map<String, String> user = new HashMap<>();
                    user.put("username", USER.getUsername());
                    user.put("tname", USER.getNickname());
                    HttpUtil.create().getToken(user).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String res = response.body().string();
                                if(HttpUtil.stateCode(res) == SUCCESS){
                                    JSONObject json = new JSONObject(res);
                                    String token = json.getString("token");
                                    ConnectIMServerHelper.getInstance().connectIMServer(context, token);
                                    LogUtil.d("token from server:" + token);
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                LogUtil.e("Exception"+e.toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                /**
                 * 连接融云成功
                 * @param userid 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userid) {
                    LogUtil.d("IMServer", "--onSuccess-- " + userid);
                    Toast.makeText(context, userid + " 登陆成功", Toast.LENGTH_SHORT).show();
                }

                /**
                 * 连接融云失败
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    LogUtil.d("IMServer", "--onError-- " + errorCode);
                }
            });
        }
    }
}
