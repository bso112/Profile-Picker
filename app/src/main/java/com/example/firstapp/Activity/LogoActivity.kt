package com.example.firstapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.example.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_logo.*
import java.util.*

class LogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)
    }

    override fun onStart() {
        super.onStart()


        val intent: Intent = Intent(this, LoginActivity::class.java)


        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_fadein)
        iv_logo.startAnimation(anim)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                startActivity(intent)
            }
        }, 500)


    }
}