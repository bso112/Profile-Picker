package com.manta.firstapp.Activity

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
import com.manta.firstapp.Helper.VolleyHelper
import com.manta.firstapp.Helper.showSimpleAlert
import com.manta.firstapp.Adapter.ViewPageAdapter
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.R
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayoutMediator
import com.manta.firstapp.Helper.UtilityHelper
import kotlinx.android.synthetic.main.activity_main.*

/**
 * by 변성욱
 * 게시물과 내 투표 뷰페이지를 포함하는 메인 액티티비.
 * 메뉴 아이콘을 누르면 로그아웃, 회원탈퇴가 가능하고 SettingActivity로 갈 수 있다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mViewPageAdapter: ViewPageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) { }

        ready_UI()


    }

    /**
     * by 변성욱
     * 메뉴 아이콘을 누르면 로그아웃, 회원탈퇴, 설정의 옵션을 띄운다.
     */
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
                            val task = LoginActivity.mGoogleSignInClient?.signOut()

                            task?.addOnCompleteListener {
                                Intent(this@MainActivity, LoginActivity::class.java).apply { startActivity(this) }; }
                            task?.addOnFailureListener {
                                Toast.makeText(this, "실패했습니다. 다시한번 시도해주세요.", Toast.LENGTH_SHORT).show() }



                        })

                    true
                }
                R.id.it_withdraw -> {
                    showSimpleAlert(this@MainActivity, "회원탈퇴", "회원탈퇴를 하시겠습니까? 모든 게시글은 삭제됩니다.",
                        {
                            //delete the information that your app obtained from the Google APIs.
                            val task = LoginActivity.mGoogleSignInClient?.revokeAccess()

                            task?.addOnCompleteListener {  withdrawAccount() }
                            task?.addOnFailureListener {

                                Toast.makeText(this, "실패했습니다. 다시한번 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }


                        })
                    true
                }
                R.id.it_setting -> {
                    Intent(this, SettingActivity::class.java).apply{
                        startActivity(this)
                    }

                    true
                }
                else -> false
            }
        }
    }

    /**
     * by 변성욱
     * 회원탈퇴 후 로그인 액티비티로 간다.
     */
    private fun withdrawAccount() {
        val loadingDialog = LoadingDialogFragment()

        val url = getString(R.string.urlToServer) + "withdrawAccount/${LoginActivity.mAccount?.email}"
        val request = StringRequest(
            Request.Method.GET, url,
            {
                it?.let { Log.d("volley", it) }

                loadingDialog.dismiss()

                Toast.makeText(this, "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                Intent(this, LoginActivity::class.java).apply { startActivity(this) };


            },
            { it.message?.let { it1 -> Log.d("volley", it1) } })

        VolleyHelper.getInstance(this).addRequestQueue(request)

        loadingDialog.show(supportFragmentManager, "다이어로그")

    }


    /**
     * by 변성욱
     * 뷰페이지를 셋팅한다.
     */
    private fun ready_UI() {
        mViewPageAdapter = ViewPageAdapter(supportFragmentManager, lifecycle)
        vp_mainPager.adapter = mViewPageAdapter
        //스와이프 비활성화
        vp_mainPager.isUserInputEnabled = false


        //TabLayoutMediator를 만들고, 그 임시객체를 이용해 tab의 제목가 아이콘을 동적으로 설정한뒤
        //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        TabLayoutMediator(tabLayout, vp_mainPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "둘러보기"
                }
                1 -> {
                    tab.text = "내 투표"
                }

            }
            //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        }.attach()


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        UtilityHelper.exitApp(this)
    }


}


