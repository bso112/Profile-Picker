package com.example.firstapp.Activity

import LoadingDialogFragment
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.firstapp.Helper.VolleyHelper
import com.example.firstapp.Helper.showSimpleAlert
import com.example.firstapp.Adapter.ViewPageAdapter
import com.example.firstapp.Helper.AdHelper
import com.example.firstapp.Helper.UtiliyHelper
import com.example.firstapp.R
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewPageAdapter: ViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) { }

        ready_UI()


    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_main, popup.menu)
        popup.show()
        popup.setOnMenuItemClickListener {
            when (it?.itemId) {
                R.id.it_logout -> {
                    showSimpleAlert(this@MainActivity, "로그아웃", "로그아웃 하시겠습니까?",
                        {
                            LoginActivity.mGoogleSignInClient?.signOut();
                            Intent(this@MainActivity, LoginActivity::class.java).apply { startActivity(this) };
                        })

                    true
                }
                R.id.it_withdraw -> {
                    showSimpleAlert(this@MainActivity, "회원탈퇴", "회원탈퇴를 하시겠습니까? 모든 게시글은 삭제됩니다.",
                        {
                            //Disconnect accounts ..? https://developers.google.com/identity/sign-in/android/disconnect
                            LoginActivity.mGoogleSignInClient?.revokeAccess()
                                ?.addOnCompleteListener { withdrawAccount() }
                        })
                    true
                }
                else -> false
            }
        }
    }

    //회원탈퇴 후 로그인액티비티로 간다.
    private fun withdrawAccount() {
        val loadingDialog = LoadingDialogFragment()

        val url = getString(R.string.urlToServer) + "withdrawAccount/${LoginActivity.mAccount?.email}"
        val request = StringRequest(
            Request.Method.GET, url,
            {
                it?.let { Log.d("volley", it) }

                loadingDialog.dismiss()

                Toast.makeText(this, "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT)
                Intent(this, LoginActivity::class.java).apply { startActivity(this) };


            },
            { it.message?.let { it1 -> Log.d("volley", it1) } })

        VolleyHelper.getInstance(this).addRequestQueue(request)

        loadingDialog.show(supportFragmentManager, "다이어로그")

    }


    private fun ready_UI() {
        viewPageAdapter = ViewPageAdapter(supportFragmentManager, lifecycle)
        mainPager.adapter = viewPageAdapter

        //TabLayoutMediator를 만들고, 그 임시객체를 이용해 tab의 제목가 아이콘을 동적으로 설정한뒤
        //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        TabLayoutMediator(tabLayout, mainPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "둘러보기"
                }
                1 -> {
                    tab.text = "내정보"
                }

            }
            //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        }.attach()


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        UtiliyHelper.getInstance().exitApp(this)
    }



}


