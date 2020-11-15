package com.manta.firstapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.manta.firstapp.Default.UserInfo
import com.manta.firstapp.Dialog.ChangeNicknameFragment
import com.manta.firstapp.Helper.*
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.frag_change_nickname_dialog.*

/**
 * by 변성욱
 * 닉네임, 성별, 나이, 관심사 등을 셋팅하는 액티비티
 * 닉네임 변경의 경우, 별개의 다이어로그에서 실행하기 때문에 그 다이어로그의 결과를 받을 수 있는
 * ChangeNicknameFragment.NoticeDialogListener를 구현한다.
 */
class SettingActivity : AppCompatActivity(), ChangeNicknameFragment.NoticeDialogListener {

    //유저정보
    lateinit var mUserInfo: UserInfo
    //관심사
    val mCategorys = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //닉네임란을 누르면 닉네임을 변경하는 다이어로그를 띄운다.
        ll_setting_change_nickname.setOnClickListener {

            //ChangeNicknameFragment.NoticeDialogListener 를 구현했으므로
            //supportFragmentManager를 넘기면 ChangeNicknameFragment에서 onAttach로 넘어간
            //SettingActivity의 context를 NoticeDialogListener로 변환할 수 있다.
            //NoticeDialogListener는  SettingActivity에서 구현한 onDialogPositiveClick을 갖고있다.
            val dialog = ChangeNicknameFragment()
            dialog.show(supportFragmentManager, null)
        }


        //유저정보 셋팅

        //닉네임, 성별셋팅
        mUserInfo = UserInfoManager.getInstance().mUserInfo ?: return
        tv_setting_nickname.setText(mUserInfo.nickname)
        rg_setting_sex.check(if (mUserInfo.sex == 0) R.id.rb_setting_male else R.id.rb_setting_female)

        //나이셋팅
        val selectionForAge = (mUserInfo.age / 10) - 2
        if (selectionForAge > 0)
            sp_setting_age.setSelection(selectionForAge)

        //싱글톤클래스(GlobalHelper)의 멤버변수로 선언된 카테고리(관심사)들을 참고해 동적으로 체크박스들을 만든다.
        for (category in GlobalHelper.getInstance(this).mCategory) {
            //그리드레이아웃에 속한 레이아웃파라미터
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)

            val checkBox = CheckBox(this)
            checkBox.text = category
            //width나 height, weigth 같은건 그리드레이아웃 파라미터이기 때문에 GridLayout.LayoutParams에서 받는다.
            checkBox.layoutParams = layoutParams
            checkBox.setOnClickListener { onCheckboxClicked(checkBox) }
            gl_setting_category.addView(checkBox)
        }

        //유저가 선택했던 카테고리에 따라서 체크박스를 체크해둔다.
        for (index in mUserInfo.categorys) {
            if (index >= 0 && index < GlobalHelper.getInstance(this).mCategory.size) {
                (gl_setting_category.getChildAt(index) as? CheckBox)?.isChecked = true
                mCategorys.add(index)
            }
        }

        //유저가 선택했던 셋팅에 따라서 내 게시물을 swipeFragement에서 카드로 표시할지 말지 체크한다.
        switch_showSelfPost.isChecked = UserInfoManager.getInstance().mUserInfo?.isShowSelfPost ?: false;

        //뒤로가기버튼 눌렀을때 변경한정보 서버에 올리기
        btn_setting_back.setOnClickListener {
            overrideSettingAndExit()

        }
        
//        //나이, 성별 못바꿈
//        rg_setting_sex.isEnabled = false;
//        rb_setting_male.isEnabled = false;
//        rb_setting_female.isEnabled = false;
//        sp_setting_age.isEnabled = false;

    }

    /**
     * by 변성욱
     * 유저가 셋팅을 변경했다면, 그것을 감지하고
     * 변경사항을 저장한다.
     */
    fun overrideSettingAndExit()
    {
        val email = LoginActivity.mAccount?.email
        if (email == null)
            finish()
        else {
            val userInfo = UserInfo(
                email, tv_setting_nickname.text.toString(),
                rg_setting_sex.indexOfChild(findViewById<RadioButton>(rg_setting_sex.checkedRadioButtonId)),
                (sp_setting_age.selectedItemPosition + 2) * 10, mCategorys,
                switch_showSelfPost.isChecked
            )

            //변경사항이 있으면 저장후 종료. 저장안해도 그냥 종료
            if (userInfo != mUserInfo) {
                showSimpleAlert(this, null, "변경사항을 저장하시겠습니까?", {
                    val url = getString(R.string.urlToServer) + "updateUserInfo/"
                    UserInfoManager.getInstance().sendUserInfoToDB(this, userInfo, url)
                    finish()
                }, { finish() })
            }
            //변경사항 없으면 바로 종료
            else
                finish()

        }
    }

    override fun onBackPressed() {
        overrideSettingAndExit()
    }

    /**
     * by 변성욱
     * 체크박스가 체크되었을때, 해당 체크박스에 설정된 text("패션", "셀카" 등등)에 해당하는
     * 인덱스번호(GlobalHelper의 mCategory로 설정된 list의 인데스번호)를 카테고리로서 멤버변수에 저장한다.
     */
    fun onCheckboxClicked(view: View) {
        val checkBox = (view as CheckBox)
        val category = GlobalHelper.getInstance(this).mCategory
        if (checkBox.isChecked)
            mCategorys.add(category.indexOf(checkBox.text.toString()))
        else
            mCategorys.remove(category.indexOf(checkBox.text.toString()))

    }

    /**
     * 닉네임변경 다이어로그에서 변경한 닉네임을
     * SettingActivity의 tv_setting_nickname에 셋팅한다.
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        dialog.dialog?.let {
            tv_setting_nickname.text = it.et_change_nickname.text.toString()
        }

    }

}