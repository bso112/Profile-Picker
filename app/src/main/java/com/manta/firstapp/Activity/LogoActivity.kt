package com.manta.firstapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.manta.firstapp.Helper.NetworkManager
import com.manta.firstapp.Helper.showAlertWithJustOkButton
import com.manta.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_logo.*
import java.util.*

class LogoActivity : AppCompatActivity() {

    private var isMinimumWaitPassed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        LoginActivity.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

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
                NetworkManager.getInstance().checkBlacklisted(this, email, {
                    //없으면 로그인처리
                    NetworkManager.getInstance().requestUserInfo(
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