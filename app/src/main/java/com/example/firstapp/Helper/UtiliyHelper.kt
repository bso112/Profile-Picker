package com.example.firstapp.Helper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat.finishAffinity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Default.UserInfo
import com.example.firstapp.R
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

//그냥 잡일하는 헬퍼
class UtiliyHelper {

    val mUserInfoFileName = "userInfo"
    var mUserInfo: UserInfo? = null
        private set;
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

    fun sendUserInfoToDB(context: Context, userInfo: UserInfo, url: String) {

        val req = object : StringRequest(
            Request.Method.POST, url,
            {
                Log.d("volley", it)
            },
            {
                Log.d("volleyError", it.message.toString())
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = mutableMapOf(Pair("email", userInfo.email))
                params["nickname"] = userInfo.nickname
                params["sex"] = userInfo.sex.toString()
                params["age"] = userInfo.age.toString()

                return params

            }
        }

        VolleyHelper.getInstance(context).addRequestQueue(req)

        //캐싱

        val fileContents = Gson().toJson(userInfo)
        val file = File(context.cacheDir, mUserInfoFileName)
        file.writeText(fileContents)

        mUserInfo = userInfo
    }

    fun requestUserInfo(context: Context, onResponse: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {

        //정보가 이미 있는지 확인
        mUserInfo?.let {
           if(onResponse != null) onResponse()
            return;
        }

        //기기내에 캐싱된 정보가 있는지 확인
        mUserInfo = getUserInfoFromFile(context)
        mUserInfo?.let {
            if(onResponse != null) onResponse()
            return;
        }


        //서버에 요청
        val url = context.getString(R.string.urlToServer) + "getUserInfo/"
        var request = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(mapOf(Pair("email", LoginActivity.mAccount?.email.toString()))),
            {
                if (it == null || it.isNull("email"))
                    if(onFailed != null) onFailed()

                it?.let { obj ->
                    val email = obj.getString("email")
                    val nickname = obj.getString("nickname")
                    val sex = obj.getInt("sex")
                    val age = obj.getInt("age")
                    val category = hashSetOf<Int>()
                    GlobalHelper.getInstance(context).mCategory.forEachIndexed { index, s -> category.add(index) }
                    mUserInfo = UserInfo(email, nickname, sex, age, category)

                    onResponse?.let { it() }
                }

            },
            {
                throw  it
            })

        VolleyHelper.getInstance(context).addRequestQueue(request)


    }

    private fun getUserInfoFromFile(context: Context): UserInfo? {
        val file = File(context.cacheDir, mUserInfoFileName)
        if (file.exists())
            return Gson().fromJson(file.readText(), UserInfo::class.java)

        return null

    }

    fun exitApp(activity: Activity) {
        if (System.currentTimeMillis() < backBtnTimeInMillis + backBtnTimeDelay) {
            finishAffinity(activity);
            return;
        }
        backBtnTimeInMillis = System.currentTimeMillis()
        Toast.makeText(activity, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
    }


}