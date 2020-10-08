package com.example.firstapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.example.firstapp.R
import com.example.firstapp.Adapter.ViewPageAdapter
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
                    tab.text = "둘러보기"
                }
                1->{
                    tab.text = "내정보"
                }

            }
            //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        }.attach()


    }

}


