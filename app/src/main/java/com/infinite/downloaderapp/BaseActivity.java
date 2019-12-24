package com.infinite.downloaderapp;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-12-24 - 17:03
 * Description: Class description
 */
public abstract class BaseActivity extends AppCompatActivity {


    public boolean permissionsCheck(String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : permissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }


    public void permissionRequest(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions, requestCode);
    }

    public boolean permissionsGranted(int[] grantResults) {
        boolean allGranted = true;
        for (int grantResult : grantResults) {
            allGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        return allGranted;
    }
}
