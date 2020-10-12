package com.example.firstapp.Activity.Helper

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity

//그냥 잡일하는 헬퍼
class UtiliyHelper
{
    private var backBtnTimeInMillis : Long = 0
    private var backBtnTimeDelay : Long = 2000

    companion object {

        private var INSTANCE : UtiliyHelper? = null

        fun getInstance() : UtiliyHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = UtiliyHelper()
                return INSTANCE as UtiliyHelper
            }
    }

    fun exitApp(activity : Activity)
    {
        if(System.currentTimeMillis() < backBtnTimeInMillis + backBtnTimeDelay)
        {
            finishAffinity(activity);
            //System.runFinalization();
            // android.os.Process.killProcess(android.os.Process.myPid());
            return;
        }
        backBtnTimeInMillis = System.currentTimeMillis()
        Toast.makeText(activity, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
    }

}