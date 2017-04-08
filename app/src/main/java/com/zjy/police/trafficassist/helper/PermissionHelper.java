package com.zjy.police.trafficassist.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * com.zjy.trafficassist.helper
 * Created by 73958 on 2017/4/6.
 */

public abstract class PermissionHelper {

    public static final int REQUEST_LOCATION = 0;
    public static final int REQUEST_CALL = 1;

    public static void requestPermission(Context context, final Activity activity, final int requestCode) {
        if (ContextCompat.checkSelfPermission(context, getPermissionString(requestCode))
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("请求权限")
                        .setMessage("应用需要定位权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{getPermissionString(requestCode)},
                                        PermissionHelper.REQUEST_LOCATION);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                Toast.makeText(context, "rationally", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PermissionHelper.REQUEST_LOCATION);
                Toast.makeText(context, "directly", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "granted", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPermissionString(int requestCode) {
        String permission = "";
        switch (requestCode) {
            case REQUEST_LOCATION:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            case REQUEST_CALL:
                permission = Manifest.permission.CALL_PHONE;
                break;
        }
        return permission;
    }

    /**
     * 校验权限请求结果
     *
     * @param grantResults
     * @return boolean
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
