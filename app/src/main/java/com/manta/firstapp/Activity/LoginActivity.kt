package com.manta.firstapp.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.Helper.showAlertWithJustOkButton

import com.manta.firstapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.manta.firstapp.Helper.UtilityHelper
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern


/**
 * by 변성욱
 * 로그인을 담당하는 액티비티.
 * 구글 로그인을 통해 로그인을 제공한다.
 * 만약 전에 로그인했던 기록이 있다면, 자동로그인을 한다.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * by 변성욱
     * 사용자 로그인정보
     */
    companion object {
        var mGoogleSignInClient: GoogleSignInClient? = null
        var mAccount: GoogleSignInAccount? = null
    }

    private val RC_SIGN_IN: Int = 1 //onActivityResult 에서 로그인 리퀘스트를 구별하기 위한 상수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //전에 로그인한 기록이 없다면, 구글로그인을 요청한다.
        if(mGoogleSignInClient == null)
        {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        }

        //로그인버튼을 누르면 로그인한다.
        sign_in_button.setOnClickListener { signIn() }

        //"여기" 텍스트를 누르면 개인정보이용약관으로 리다이렉트
        val mTransform = Linkify.TransformFilter { match, url -> ""; }
        val pattern = Pattern.compile("여기");
        Linkify.addLinks(tv_login_link, pattern, "https://blackmanta.tistory.com/1?category=818797", null, mTransform);

    }


    //전에 로그인한 적이 있는지 확인한다.
    override fun onStart() {
        super.onStart()
        if(mAccount == null)
            mAccount = GoogleSignIn.getLastSignedInAccount(this)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        UtilityHelper.exitApp(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //로그인 요청결과를 받아 처리한다.
        if (requestCode === RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    /**
     * by 변성욱
     * 구글의 로그인 액티비티를 시작한다.
     */
    private fun signIn() {
        startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
    }


    /**
     * by 변성욱
     * 로그인을 시도한 계정이 블랙리스트에 있는지 확인한다.
     */
    private fun checkIfAccountExist(email: String) {
        //블랙리스트에 있는지 확인
        UserInfoManager.getInstance().checkBlacklisted(this, email, {
            //없으면 로그인
            UserInfoManager.getInstance().requestUserInfo(this, email,
                { startActivity(Intent(this, MainActivity::class.java)) },
                { startActivity(Intent(this, SignUpActivity::class.java)) })
        }, {
            //있으면 거부
            showAlertWithJustOkButton(this, null, "해당 계정은 정지조치 되었습니다. 개발팀에 문의해주시기 바랍니다. " +
                    "\n bso11246@gmail.com",
                "돌아가기", {finishAffinity()})
        })

    }

    /**
     * by 변성욱
     * 로그인 결과를 처리한다.
     * 로그인에 성공하면 블랙리스트에 있는지, 기존에 등록된 회원인지 확인한다.
     */
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