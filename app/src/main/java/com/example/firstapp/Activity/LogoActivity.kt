package com.example.firstapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.firstapp.Helper.UtiliyHelper
import com.example.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_logo.*
import java.util.*

class LogoActivity : AppCompatActivity() {

    private var isMinimumWaitPassed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)
    }

    override fun onStart() {
        super.onStart()

        //로그인된 상태인지 확인
        LoginActivity.mAccount = GoogleSignIn.getLastSignedInAccount(this)

        var intent: Intent? = null

        //전에 로그인한 적이 있으면 바로 로그인처리
        //DB에 해당 구글계정이 있는지 확인한다. 있으면 MainActivity, 없으면 SignUpActivity로 간다.
        if(null == LoginActivity.mAccount)
            intent =Intent(this, LoginActivity::class.java)
        else
        {
            //DB에 계정정보있는지 확인
            LoginActivity.mAccount?.email?.let {
                UtiliyHelper.getInstance().requestUserInfo(
                    this, it,
                    { intent = Intent(this, MainActivity::class.java) ; if(isMinimumWaitPassed) startActivity(intent)},
                    { intent = Intent(this, LoginActivity::class.java) ; if(isMinimumWaitPassed) startActivity(intent)})
            }
        }




        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_fadein)
        tv_logo.startAnimation(anim)

        //최소 0.5초는 로고를 보여준다.
        Timer().schedule(object : TimerTask() {
            override fun run() {
                isMinimumWaitPassed = true
                if(intent != null)
                    startActivity(intent)
            }
        }, 500)


    }
}