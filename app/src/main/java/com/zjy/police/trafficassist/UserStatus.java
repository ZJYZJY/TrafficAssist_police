package com.zjy.police.trafficassist;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

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
}
