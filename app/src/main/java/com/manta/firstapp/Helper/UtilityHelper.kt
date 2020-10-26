package com.manta.firstapp.Helper

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.manta.firstapp.Default.TO_FILTER
import com.manta.firstapp.R

object UtilityHelper {

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


}