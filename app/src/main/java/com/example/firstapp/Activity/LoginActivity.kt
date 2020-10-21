package com.example.firstapp.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.firstapp.Helper.UtiliyHelper
import com.example.firstapp.Helper.VolleyHelper

import com.example.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*


/*
OnCreate() : mGoogleSignInClient 객체를 만든다.
sign_in_button.setOnClickListener( SingIn()) : mGoogleSignInClient을 이용해 구글 로그인 액티비티를 시작한다.
onActivityResult : 구글 로그인 액티비티의 결과를 받아 그로부터 task(로그인 결과포함)를 얻어내고, handleSignInResult에서 task를 처리한다.
*/


class LoginActivity : AppCompatActivity() {

    companion object {
        var mGoogleSignInClient: GoogleSignInClient? = null
        var mAccount: GoogleSignInAccount? = null

    }

    val RC_SIGN_IN: Int = 1234 //onActivityResult 에서 로그인 리퀘스트를 구별하기 위한 상수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(mGoogleSignInClient == null)
        {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        }

        sign_in_button.setOnClickListener { signIn() }

    }


    override fun onStart() {
        super.onStart()
        if(mAccount == null)
            mAccount = GoogleSignIn.getLastSignedInAccount(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        UtiliyHelper.getInstance().exitApp(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }


    private fun signIn() {
        startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
    }


    private fun checkIfAccountExist(email: String) {

        UtiliyHelper.getInstance().requestUserInfo(this, email,
            { startActivity(Intent(this, MainActivity::class.java)) },
            { startActivity(Intent(this, SignUpActivity::class.java)) }
        )

    }

    //유저가 이메일을 선택했을때
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {

        try {
            //로그인된 구글계정을 저장한다.
            mAccount = completedTask.getResult(ApiException::class.java)
            //해당 구글계정이 데이터베이스에 등록되어있는지 확인한다.
            if(mAccount == null)
                Toast.makeText(this, "로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()

            mAccount?.email?.let { checkIfAccountExist(it) }

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("MainActivity", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(applicationContext, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()

        }
    }


}