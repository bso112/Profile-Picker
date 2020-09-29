package com.example.firstapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.firstapp.EXTRA_USERNAME
import com.example.firstapp.R
import com.example.firstapp.ViewPage.ViewPageAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewPageAdapter : ViewPageAdapter
    private var backBtnTimeInMillis : Long = 0
    private var backBtnTimeDelay : Long = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        ready_UI()

    }

    private fun ready_UI()
    {
        viewPageAdapter = ViewPageAdapter(supportFragmentManager, lifecycle)
        mainPager.adapter = viewPageAdapter

        //TabLayoutMediator를 만들고, 그 임시객체를 이용해 tab의 제목가 아이콘을 동적으로 설정한뒤
        //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        TabLayoutMediator(tabLayout, mainPager){ tab, position->
            when(position){
                0-> {
                    tab.icon = ResourcesCompat.getDrawable(resources,
                        R.drawable.fire, null)
                }
                1->{
                    tab.icon = ResourcesCompat.getDrawable(resources,
                        R.drawable.chat, null)
                }
                2->{
                    tab.icon = ResourcesCompat.getDrawable(resources,
                        R.drawable.profile, null)
                }

            }
            //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        }.attach()

        //환영인사
        val message = intent.getStringExtra(EXTRA_USERNAME)
        Toast.makeText(applicationContext, message + "님, 안녕하세요!", Toast.LENGTH_SHORT).show()

    }

}


