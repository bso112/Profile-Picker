package com.example.firstapp.Helper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.firstapp.Default.UserInfo
import com.example.firstapp.R
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File

//http 요청을 관리
class NetworkManager {

    var mUserInfo: UserInfo? = null
        private set;
    private var backBtnTimeInMillis: Long = 0
    private var backBtnTimeDelay: Long = 2000


    companion object {

        private var INSTANCE: NetworkManager? = null

        fun getInstance(): NetworkManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE = NetworkManager()
                return INSTANCE as NetworkManager
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
        val file = File(context.cacheDir, "account_" + userInfo.email)
        file.writeText(fileContents)



        mUserInfo = userInfo
    }

    fun checkBlacklisted(context: Context, email : String, onPass : ()->Unit, onRejected : ()->Unit )
    {
        val url = context.getString(R.string.urlToServer) + "checkBlacklisted/${email}"
        var request = JsonObjectRequest(Request.Method.GET, url, null,
            {
                it?.let {
                    val isBlacklisted = it.getInt("isBlacklisted")
                    if(isBlacklisted == 0)
                        onPass()
                    else
                        onRejected()
                }

            },
            {
                it?.let { Log.d("volley", it.message.toString()) }
            })

        context?.let { VolleyHelper.getInstance(it).addRequestQueue(request) }
    }

    fun requestUserInfo(context: Context, email: String, onResponse: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {

        //만약 로컬에 저장한 데이터로만 로그인 판단해버리면 데베에 유저정보가 없어도 로컬에 캐싱된 데이터가 있으면 접속되버림.
        // 이메일만 조작하면 다른 계정으로도 접속됨
        //그니까 카테고리만 쓰자.


        //없으면 서버에 요청
        val url = context.getString(R.string.urlToServer) + "getUserInfo/"
        var request = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(mapOf(Pair("email", email))),
            {

                if (it == null || it.length() <= 0) {
                    if (onFailed != null) onFailed()
                    return@JsonObjectRequest
                }

                it?.let { obj ->
                    val email = obj.getString("email")
                    val nickname = obj.getString("nickname")
                    val sex = obj.getInt("sex")
                    val age = obj.getInt("age")
                    val category = hashSetOf<Int>()

                    //카테고리를 내장메모리에서  얻는다.
                    val userInfoFromFile = getUserInfoFromFile(context, email)
                    if (userInfoFromFile != null) {
                        mUserInfo?.categorys?.clear();
                        category.addAll(userInfoFromFile.categorys)
                    }
                    //만약 없으면 모든 카테고리를 추가한다.
                    else
                        GlobalHelper.getInstance(context).mCategory.forEachIndexed { index, s -> category.add(index) }


                    mUserInfo = UserInfo(email, nickname, sex, age, category)

                    onResponse?.let { it() }
                }

            },
            {
                it.message?.let { it1 -> Log.d("volley", it1) }
                if (onFailed != null) onFailed()
            })

        VolleyHelper.getInstance(context).addRequestQueue(request)


    }

    private fun getUserInfoFromFile(context: Context, email: String): UserInfo? {
        val file = File(context.cacheDir, "account_" + email)
        if (file.exists())
            return Gson().fromJson(file.readText(), UserInfo::class.java)

        return null

    }

    fun exitApp(activity: Activity) {
        if (System.currentTimeMillis() < backBtnTimeInMillis + backBtnTimeDelay) {
            activity.finishAffinity();
            return;
        }
        backBtnTimeInMillis = System.currentTimeMillis()
        Toast.makeText(activity, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
    }


}