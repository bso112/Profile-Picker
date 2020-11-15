package com.manta.firstapp.Dialog

import LoadingDialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.manta.firstapp.Helper.UtilityHelper
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.frag_change_nickname_dialog.*


/**
 * by 변성욱
 * 닉네임을 변경하는 다이어로그를 띄우고 관리하는 프래그먼트
 */
class ChangeNicknameFragment : DialogFragment() {

    /**
     * by 변성욱
     * 호출자에게 제공할 인터페이스
     * 호출자는 이 인터페이스를 구현함으로서 이 다이어로그를 인자로 받아볼 수 있다.
     */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    internal lateinit var listener: NoticeDialogListener

    //다이어로그 형식으로 커스텀 뷰를 띄운다.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(R.layout.frag_change_nickname_dialog)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onStart() {
        super.onStart()

        dialog?.apply {
            //다이어로그 스코프

            //제출버튼을 누르면 제출
            btn_change_submit.setOnClickListener {
                //닉네임이 조건을 충족했으면 데이터베이스에 있는 닉네임들과 중복되는지 확인하고 제출
                if(checkIfNicknameFilled())
                    sumitIfNicknameisNotDuplicated()
            }

            //닉네임이 변경될때마다 닉네임이 충족되었는가 확인
            et_change_nickname.addTextChangedListener {
                checkIfNicknameFilled()
            }

        }
    }


    /**
     * by 변성욱
     * 닉네임이 조건을 충족했는가?
     * 금칙어가 포함되지 않고, 닉네임이 충분히 길어야함.
     */
    private fun checkIfNicknameFilled() : Boolean
    {
        dialog?.apply {
            //경고는 일단 안보이게
            tv_change_nickname_warn.visibility = View.INVISIBLE

            //조건 충족여부 리턴
            return UtilityHelper.checkIfNicknameFilled(et_change_nickname.text.toString(),
                {
                    //닉네임에 금지어가 있을때
                    tv_change_nickname_warn.visibility = View.VISIBLE
                    tv_change_nickname_warn.text = getString(R.string.warn_nickname_banned)
                },
                {
                    //닉네임이 짧을때
                    tv_change_nickname_warn.visibility = View.VISIBLE
                    tv_change_nickname_warn.text = getString(R.string.warn_nicknameLegnth)
                })
        }

        return false
    }

    /**
     * by 변성욱
     * 현재 변경하려는 닉네임과 데이터베이스안에 있는 닉네임 중 중복된 것이 있는지 확인하고
     * 없다면 변경된 닉네임을 데이터베이스에 적용한다.
     */
    private fun sumitIfNicknameisNotDuplicated()
    {
        val loadingDialog = LoadingDialogFragment()
        fragmentManager?.let { loadingDialog.show(it, null) }

        dialog?.apply {
            UtilityHelper.checkIfNicknameDuplicated(context, et_change_nickname.text.toString(),
                {
                    //중복이 아니면
                    tv_change_nickname_warn.text = getString(R.string.warn_nickname_passed)
                    tv_change_nickname_warn.setTextColor(resources.getColor(R.color.Black))

                    //리스너(다이어로그 호출자)에게 스스로를 넘김
                    listener.onDialogPositiveClick(this@ChangeNicknameFragment)

                    loadingDialog.dismiss()
                    //다이어로그 닫기
                    cancel()
                },
                {
                    //중복이 됬으면
                    tv_change_nickname_warn.text = getString(R.string.warn_nickname_duplicated)
                    tv_change_nickname_warn.setTextColor(resources.getColor(R.color.Red))
                })

            //결과 메시지 보이기
            tv_change_nickname_warn.visibility = View.VISIBLE
        }
    }



    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    // context는 SettingActivty의 context를 의미한다. SettingActivity는 NoticeDialogListener를 구현했으므로
    // NoticeDialogListener로 변환가능하다.
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }


}