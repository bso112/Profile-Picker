package com.example.firstapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import com.example.firstapp.Default.UserInfo
import com.example.firstapp.Helper.*
import com.example.firstapp.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_sing_up.*

class SettingActivity : AppCompatActivity() {

    lateinit var mUserInfo: UserInfo
    val mCategorys = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //유저정보 셋팅
        mUserInfo = UtiliyHelper.getInstance().mUserInfo ?: return
        et_setting_nickname.setText(mUserInfo.nickname)
        rg_setting_sex.check(if (mUserInfo.sex == 0) R.id.rb_setting_male else R.id.rb_setting_female)

        val selectionForAge = (mUserInfo.age / 10) - 1
        if (selectionForAge > 0)
            sp_setting_age.setSelection(selectionForAge)

        for (index in mUserInfo.categorys) {
            if (index >= 0)
            {
                (gl_setting_category.getChildAt(index) as? CheckBox)?.isChecked = true
                mCategorys.add(index)
            }
        }

        //뒤로가기버튼 눌렀을때 변경한정보 서버에 올리기
        btn_setting_back.setOnClickListener {

            val email = LoginActivity.mAccount?.email
            if(email == null)
                finish()
            else
            {
                val userInfo = UserInfo(
                    email, et_setting_nickname.text.toString(),
                    rg_setting_sex.indexOfChild(findViewById<RadioButton>(rg_setting_sex.checkedRadioButtonId)),
                    (sp_setting_age.selectedItemPosition + 1) * 10, mCategorys
                )

                //변경사항이 있으면 저장후 종료
                if (userInfo != mUserInfo) {
                    showSimpleAlert(this, null, "변경사항을 저장하시겠습니까?", {
                        val url = getString(R.string.urlToServer) + "updateUserInfo/"
                        UtiliyHelper.getInstance().sendUserInfoToDB(this, userInfo, url)
                        finish()
                    })
                }
                //아니면 그냥 종료
                else
                    finish()

            }
        }

    }

    fun onCheckboxClicked(view: View) {
        val checkBox = (view as CheckBox)
        val category = GlobalHelper.getInstance(this).mCategory
        if(checkBox.isChecked)
            mCategorys.add(category.indexOf(checkBox.text.toString()))
        else
            mCategorys.remove(category.indexOf(checkBox.text.toString()))

    }


}