package com.example.firstapp.Helper

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat.finishAffinity
import com.example.firstapp.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView

//그냥 잡일하는 헬퍼
class UtiliyHelper {


    private var backBtnTimeInMillis: Long = 0
    private var backBtnTimeDelay: Long = 2000


    companion object {

        private var INSTANCE: UtiliyHelper? = null

        fun getInstance(): UtiliyHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = UtiliyHelper()
                return INSTANCE as UtiliyHelper
            }
    }





    fun exitApp(activity: Activity) {
        if (System.currentTimeMillis() < backBtnTimeInMillis + backBtnTimeDelay) {
            finishAffinity(activity);
            //System.runFinalization();
            // android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }
        backBtnTimeInMillis = System.currentTimeMillis()
        Toast.makeText(activity, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
    }




}