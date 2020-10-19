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
import org.json.JSONObject
import java.io.File

//그냥 잡일하는 헬퍼
class UtiliyHelper {


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

        //유저가 선호하는 카테고리를 내부저장소에 저장
        val filename = "category"
        val fileContents = userInfo.categorys
        val file = File(context.filesDir, filename)
        val writer = file.printWriter()
        for (category in fileContents) {
            //내부적으로 write(String.valueOf(i)); 처럼 int를 String으로 만들고 씀.
            if(category >= 0)
            {
                //인자로 들어온 string을 byteArray로 바꿔서 쓰고 파일을 열고 닫는다.(매번)
                //file.writeText(category.toString())

                //printWriter.write(int) 는 int를 그냥 char로 강제형변환해서 저장함. int가 아스키코드로서 저장되는듯.
                //따라서 printWritter.write(string을 쓰자.)
                //한줄씩 쓰려면 printWritter.println(string)
                writer.println(category.toString())

            }

        }

        writer.close()
        VolleyHelper.getInstance(context).addRequestQueue(req)

        mUserInfo = userInfo
    }

    fun requestUserInfo(context: Context) {
        //이게 sendUserInfoToDB보다 먼저 결과가 올수있을듯

        val url = context.getString(R.string.urlToServer) + "getUserInfo/"
        var request = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(mapOf(Pair("email", LoginActivity.mAccount?.email.toString()))),
            {
                it?.let { obj ->
                    val email = obj.getString("email")
                    val nickname = obj.getString("nickname")
                    val sex = obj.getInt("sex")
                    val age = obj.getInt("age")
                    mUserInfo = UserInfo(email, nickname, sex, age, getCategoryFromFile(context))
                }

            },
            {
                throw  it
            })

        VolleyHelper.getInstance(context).addRequestQueue(request)



    }

    private fun getCategoryFromFile(context: Context) : HashSet<Int>
    {
        //만약 정보가이미 있으면 그냥 리턴
       mUserInfo?.let{
           return it.categorys
       }

        var result: String = ""
        //내부저장소에서 카테고리 파일 찾음
        val filename = "category"

        //중복제거
        val category = hashSetOf<Int>()
        val file = File(context.filesDir, filename)
        if(file.exists())
        {
            file.forEachLine {
                    line -> category.add(line.toInt())
            }

        }
        else
            //모든 카테고리 추가
            GlobalHelper.getInstance(context).mCategory.forEachIndexed { index, s -> category.add(index) }

        return category

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