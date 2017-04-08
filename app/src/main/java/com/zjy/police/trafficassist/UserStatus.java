package com.zjy.police.trafficassist;

import android.content.Context;
import android.content.SharedPreferences;

import com.zjy.police.trafficassist.model.User;
import com.zjy.police.trafficassist.utils.LogUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created 2016/5/1.
 *
 * @author 郑家烨.
 */
public class UserStatus {

    public static boolean LOGIN_STATUS = false;

    public static User USER;

    public static boolean first_show = true;

    public static SharedPreferences SP;

    public static SharedPreferences.Editor EDITOR;

    public static void ClearUserLoginStatus(Context context){
        LOGIN_STATUS = false;
        USER = null;
        SP = context.getSharedPreferences("USER_INFO", MODE_PRIVATE);
        SP.edit().clear().apply();
        LogUtil.e("unlogin success");
    }
}
