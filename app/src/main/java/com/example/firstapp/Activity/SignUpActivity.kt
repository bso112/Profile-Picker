package com.example.firstapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.BoringLayout
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import com.example.firstapp.Default.UserInfo
import com.example.firstapp.Helper.GlobalHelper
import com.example.firstapp.Helper.UtiliyHelper
import com.example.firstapp.R
import kotlinx.android.synthetic.main.activity_sing_up.*

class SignUpActivity : AppCompatActivity() {


    val mCategorys = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)

        btn_signup_submit.setOnClickListener {


            if (checkIfAllFilled()) {
                if (LoginActivity.mAccount == null || LoginActivity.mAccount!!.email == null)
                    return@setOnClickListener

                val userInfo: UserInfo = UserInfo(
                    LoginActivity.mAccount!!.email!!, et_signup_nickname.text.toString(),
                    rg_signup_sex.indexOfChild(findViewById<RadioButton>(rg_signup_sex.checkedRadioButtonId)),
                    (sp_signup_age.selectedItemPosition + 1) * 10, mCategorys
                )

                val url = getString(R.string.urlToServer) + "writeUserInfo/"
                //데이터베이스에 유저정보 저장
                UtiliyHelper.getInstance().sendUserInfoToDB(this, userInfo, url)

                Intent(this, MainActivity::class.java).apply {
                    startActivity(this)


                }
            }


        }

        et_signup_nickname.addTextChangedListener {
            checkIfNicknameFilled()
        }
    }


    fun onCheckboxClicked(view: View) {
        if((view as CheckBox).isChecked)
        {
            mCategorys.add(GlobalHelper.getInstance (this).mCategory.indexOf((view as CheckBox).text.toString()))
            tv_signup_warn_category.visibility = View.INVISIBLE
        }
        else if(mCategorys.isEmpty())
            tv_signup_warn_category.visibility = View.VISIBLE
    }

    private fun checkIfAllFilled() : Boolean{

        var result  = true

        //경고문구를 띄워야되기때문에 바로 리턴해서 나가지 않는다.
        if(!checkIfNicknameFilled())
            result = false

        if (mCategorys.isEmpty()) {
            tv_signup_warn_category.visibility = View.VISIBLE
            result = false
        }

        return result
    }

    private fun checkIfNicknameFilled(): Boolean {

        //패턴확인
        val mathResult = Regex("""[(가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9)]+""").matchEntire(et_signup_nickname.text)

        //중복확인 하지말자 귀찮다.

        tv_signup_warn_nickname.visibility =
            if (mathResult == null || mathResult.value.length < 3) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

        if(tv_signup_warn_nickname.visibility == View.VISIBLE) return false
        
        return true
    }


}