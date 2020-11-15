package com.manta.firstapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.Helper.showAlertWithJustOkButton
import com.manta.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_logo.*
import java.util.*

/**
 * by 변성욱
 * 스플래시 스크린을 위한 로고액티비티
 */
class LogoActivity : AppCompatActivity() {

    /**
     * by 변성욱
     * 필요 최저한의 시간 동안 로고를 보여주었는가?
     */
    private var isMinimumWaitPassed = false

    /**
     * by변성욱
     * 기존에 로그인이 되어있는경우, 로고에서 바로 메인액티비로 넘어가기 때문에
     * 여기서  LoginActivity.mGoogleSignInClient를 셋팅해준다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        LoginActivity.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    /**
     * by변성욱
     * 로그인한 적이 없다면 로그인액티비티로,
     * 있다면 블랙리스트에 올라가있는지 확인한 후 메인액티비로 간다.
     */
    override fun onStart() {
        super.onStart()

        //로그인된 상태인지 확인
        LoginActivity.mAccount = GoogleSignIn.getLastSignedInAccount(this)

        var intent: Intent? = null

        //로그인한적이 없으면 로그인액티비티로
        if (null == LoginActivity.mAccount)
            intent = Intent(this, LoginActivity::class.java)
        //있으면
        else {
            LoginActivity.mAccount?.email?.let { email ->

                //블랙리스트에 있는지 확인
                UserInfoManager.getInstance().checkBlacklisted(this, email, {
                    //없으면 로그인처리
                    UserInfoManager.getInstance().requestUserInfo(
                        this, email,
                        { intent = Intent(this, MainActivity::class.java); if (isMinimumWaitPassed) startActivity(intent) },
                        { intent = Intent(this, LoginActivity::class.java); if (isMinimumWaitPassed) startActivity(intent) })
                },
                    {
                        //있으면 거부
                        showAlertWithJustOkButton(this, null, "해당 계정은 정지조치 되었습니다. 개발팀에 문의해주시기 바랍니다." +
                                "\nbso11246@gmail.com",
                        "돌아가기", {finishAffinity();})

                    })
            }
        }


        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_fadein)
        tv_logo.startAnimation(anim)

        //최소 0.5초는 로고를 보여준다.
        Timer().schedule(object : TimerTask() {
            override fun run() {
                isMinimumWaitPassed = true
                if (intent != null)
                    startActivity(intent)
            }
        }, 500)


    }
}