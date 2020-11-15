package com.manta.firstapp.Helper

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.manta.firstapp.Default.TO_FILTER
import com.manta.firstapp.R

object UtilityHelper {

    private var backBtnTimeInMillis: Long = 0
    private var backBtnTimeDelay: Long = 2000

    fun checkIfNicknameFilled(nickname : String, onTextBanned: ()->Unit, onTextShort : ()->Unit): Boolean {

        //금지어 확인
        val mathResultForBanned = Regex(TO_FILTER).matchEntire(nickname)

        if(mathResultForBanned != null && mathResultForBanned.value.isNotEmpty())
        {
            onTextBanned()
            return false
        }


        //글자수 확인.
        val mathResultForLegnth = Regex("""[(가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9)]+""").matchEntire(nickname)

        if (mathResultForLegnth == null || mathResultForLegnth.value.length < 3) {
            onTextShort()
            return false
        }

        return true
    }


    fun checkIfNicknameDuplicated(context : Context, nickName : String, onPassed : () -> Unit, onDuplicated : () -> Unit) {

        val url = context.getString(R.string.urlToServer) + "checkNicknameDuplicated/" + nickName
        val req = StringRequest(
            Request.Method.GET, url, {
            it?.let {
                if (it == "0") {
                    onPassed()
                } else {
                    onDuplicated()
                }
            }
        },
            {
                throw it
            })

        VolleyHelper.getInstance(context).addRequestQueue(req)


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