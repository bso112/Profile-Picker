package com.example.firstapp.Activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Activity.Helper.UtiliyHelper

import com.example.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.system.exitProcess


/*
OnCreate() : mGoogleSignInClient 객체를 만든다.
sign_in_button.setOnClickListener( SingIn()) : mGoogleSignInClient을 이용해 구글 로그인 액티비티를 시작한다.
onActivityResult : 구글 로그인 액티비티의 결과를 받아 그로부터 task(로그인 결과포함)를 얻어내고, handleSignInResult에서 task를 처리한다.
*/


class LoginActivity : AppCompatActivity() {

    companion object {
        var mGoogleSignInClient: GoogleSignInClient? = null
        private set
        var mAccount: GoogleSignInAccount? = null
        private set


        //이미 로그인한적이 있는경우 이것만 부르면 로그인 정보를 셋팅할 수 있다.
        fun setAccountInfo(activity: Activity)
        {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            mAccount = GoogleSignIn.getLastSignedInAccount(activity)
        }

        fun clearLoginInfo()
        {
            mAccount = null
        }

    }

    val RC_SIGN_IN: Int = 1234 //onActivityResult 에서 로그인 리퀘스트를 구별하기 위한 상수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sign_in_button.setOnClickListener { signIn() }

    }


    override fun onStart() {
        super.onStart()
        //로그인이 이미 되어있는지 확인
        mAccount = GoogleSignIn.getLastSignedInAccount(this)
        mAccount?.let { onLoginSuccess(mAccount) } 
        
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

    private fun onLoginSuccess(account: GoogleSignInAccount?) {

        //로그인에 성공했다면
        if (null != account) {
            //계정 저장
            mAccount = account
            //main activity로 가기
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
        }

    }

    private fun signIn() {
        startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            //데이터베이스에 이메일 저장
            sendUserInfoToDB()
            // Signed in successfully, show authenticated UI.
            onLoginSuccess(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("MainActivity", "signInResult:failed code=" + e.statusCode)
            onLoginSuccess(null)
            Toast.makeText(applicationContext, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()

        }
    }


    private fun sendUserInfoToDB() {
        val queue = Volley.newRequestQueue(applicationContext)

        val url = getString(R.string.urlToServer) + "writeUserInfo/"
        val req = object : StringRequest(Request.Method.POST, url,
            {
                Log.d("volley", it)
            },
            {
                Log.d("volleyError", it.message.toString())
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(Pair("email", mAccount?.email.toString()))
            }
        }

        queue.add(req)
    }

}