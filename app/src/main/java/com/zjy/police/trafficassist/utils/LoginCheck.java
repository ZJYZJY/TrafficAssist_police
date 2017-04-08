package com.zjy.police.trafficassist.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.zjy.police.trafficassist.UserStatus;
import com.zjy.police.trafficassist.helper.ConnectIMServerHelper;
import com.zjy.police.trafficassist.model.User;

import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.zjy.police.trafficassist.utils.HttpUtil.SUCCESS;

public class LoginCheck {

    private User user;
    private Context context;
    private ProgressDialog mPDialog;

    public LoginCheck(Context context, User user){
        this.context = context;
        this.user = user;
        mPDialog = null;
    }

    public LoginCheck(Context context, User user, ProgressDialog mPDialog){
        this.context = context;
        this.user = user;
        this.mPDialog = mPDialog;
    }

    public void login(){
        HttpUtil.create().login(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(mPDialog != null)
                    mPDialog.dismiss();
                try {
                    String res = response.body().string();
                    LogUtil.e(res);
                    if(HttpUtil.stateCode(res) == SUCCESS){
                        UserStatus.LOGIN_STATUS = true;
                        UserStatus.USER = user;
                        // 保存登录信息
                        AutoLogin.getInstance().saveUserInfo(context);

                        // IM服务器登录
                        SharedPreferences preferences = context.getSharedPreferences("RongKitConfig", MODE_PRIVATE);
                        String token = preferences.getString("token", "");
                        ConnectIMServerHelper.getInstance().connectIMServer(context, token);

                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();

                        // 如果是LoginActivity登录的话，则关闭activity
                        String contextString = context.toString();
                        String AtyName = contextString.substring(contextString.lastIndexOf(".") + 1, contextString.indexOf("@"));
                        LogUtil.e(AtyName);
                        if(Objects.equals(AtyName, "LoginActivity")){
                            LogUtil.e("in");
                            ((Activity)context).finish();
                        }
                    }else{
                        Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
