package com.manta.firstapp.Activity

import LoadingDialogFragment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.manta.firstapp.Default.UserInfo
import com.manta.firstapp.Helper.GlobalHelper
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.Helper.UtilityHelper
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_sing_up.*

/**
 * by 변성욱
 * 회원가입 액티비티
 *
 */
class SignUpActivity : AppCompatActivity() {


    val mCategorys = HashSet<Int>()
    //닉네임 중복확인은 비동기이기 때문에 필요하다.
    var mNicknamePassed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)



        //제출버튼 클릭시
        btn_signup_submit.setOnClickListener {

            //모든 항목이 채워졌으면
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
                UserInfoManager.getInstance().sendUserInfoToDB(this, userInfo, url)

                Intent(this, MainActivity::class.java).apply {
                    startActivity(this)


                }
            }

        }

        //mCategory로부터 체크박스들을 만든다.
        for (category in GlobalHelper.getInstance(this).mCategory) {
            //그리드레이아웃에 속한 레이아웃파라미터
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            layoutParams.bottomMargin = 5
            val checkBox = CheckBox(this)
            checkBox.text = category
            //width나 height, weigth 같은건 그리드레이아웃 파라미터이기 때문에 GridLayout.LayoutParams에서 받는다.
            checkBox.layoutParams = layoutParams
            checkBox.setOnClickListener { onCheckboxClicked(checkBox) }
            gl_signup_category.addView(checkBox)
        }


        
        //닉네임 작성시
        et_signup_nickname.addTextChangedListener {
            //닉네임이 조건을 만족했는지 확인
            checkIfNicknameFilled()

            //닉네임 바뀌면 중복확인 다시해야함.
            mNicknamePassed = false
        }

        //닉네임 중복확인버튼 클릭시
        btn_signup_nickname_confirm.setOnClickListener {
            //닉네임이 조건을 만족했으면 중복확인
            if(checkIfNicknameFilled())
                checkIfNicknameDuplicated()
        }





    }

    // 닉네임 조건을 체크한다.
    private fun checkIfNicknameFilled() : Boolean
    {
        //일단 안보이게
        tv_signup_warn_nickname.visibility = View.INVISIBLE

        return UtilityHelper.checkIfNicknameFilled(et_signup_nickname.text.toString(),
            {
                //닉네임에 금지어가 있을때
                tv_signup_warn_nickname.visibility = View.VISIBLE
                tv_signup_warn_nickname.text = getString(R.string.warn_nickname_banned)
            },
            {
                //닉네임이 짧을때
                tv_signup_warn_nickname.visibility = View.VISIBLE
                tv_signup_warn_nickname.text = getString(R.string.warn_nicknameLegnth)
            })

    }

    //닉네임이 중복이 되었는지 확인
    private fun checkIfNicknameDuplicated()
    {
        val loadingDialog = LoadingDialogFragment()
        loadingDialog.show(supportFragmentManager, null)
        UtilityHelper.checkIfNicknameDuplicated(this, et_signup_nickname.text.toString(),
            {
                //중복이 아니면
                tv_signup_warn_nickname_duplicated.text = getString(R.string.warn_nickname_passed)
                tv_signup_warn_nickname_duplicated.setTextColor(resources.getColor(R.color.Black))
                mNicknamePassed = true
                loadingDialog.dismiss()
            },
            {
                //중복이 됬으면
                tv_signup_warn_nickname_duplicated.text = getString(R.string.warn_nickname_duplicated)
                tv_signup_warn_nickname_duplicated.setTextColor(resources.getColor(R.color.Red))
                mNicknamePassed = false
                loadingDialog.dismiss()
            })

        //결과 메시지
        tv_signup_warn_nickname_duplicated.visibility = View.VISIBLE
    }
    


    //체크박스가 클릭되면 선택된 카테고리를 저장한다.
    //만약 선택된 카테고리가 없으면 경고를 표시한다.
    private fun onCheckboxClicked(view: View) {
        if ((view as CheckBox).isChecked) {
            mCategorys.add(GlobalHelper.getInstance(this).mCategory.indexOf((view as CheckBox).text.toString()))
            tv_signup_warn_category.visibility = View.INVISIBLE
        } else if (mCategorys.isEmpty())
            tv_signup_warn_category.visibility = View.VISIBLE
    }

    //회원가입을 위한 모든 조건이 채워졌는지 확인한다.
    private fun checkIfAllFilled(): Boolean {

        var result = true

        //경고문구를 띄워야되기때문에 바로 리턴해서 나가지 않는다.
        if (!checkIfNicknameFilled())
            result = false

        if (mCategorys.isEmpty()) {
            tv_signup_warn_category.visibility = View.VISIBLE
            result = false
        }

        if (!mNicknamePassed)
        {
            result = false
            tv_signup_warn_nickname_duplicated.text = "닉네임 중복확인을 해주세요."
            tv_signup_warn_nickname_duplicated.setTextColor(resources.getColor(R.color.Red))
            tv_signup_warn_nickname_duplicated.visibility = View.VISIBLE
        }
        


        return result
    }






}