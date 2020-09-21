package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.internal.Objects
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*


/*
OnCreate() : mGoogleSignInClient 객체를 만든다.
sign_in_button.setOnClickListener( SingIn()) : mGoogleSignInClient을 이용해 구글 로그인 액티비티를 시작한다.
onActivityResult : 구글 로그인 액티비티의 결과를 받아 그로부터 task(로그인 결과포함)를 얻어내고, handleSignInResult에서 task를 처리한다.
*/


class LoginActivity : AppCompatActivity() {

    lateinit var  mGoogleSignInClient : GoogleSignInClient;
    val RC_SIGN_IN : Int = 1234 //onActivityResult 에서 로그인 리퀘스트를 구별하기 위한 상수

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

    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)

        sign_in_button.setOnClickListener{ signIn()}

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

    private fun updateUI(account: GoogleSignInAccount?) {
        
        //로그인에 성공했다면
        if (null != account) {
            //main activity로 가기
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(FIRSTAPP_USERNAME,account.displayName)
            }
            startActivity(intent)
            Log.d("MainActivity", "login success!")
        }

    }

    private fun signIn()
    {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("MainActivity", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
            Toast.makeText(applicationContext, "로그인에 실패하였습니다!", Toast.LENGTH_SHORT).show()

        }
    }


}