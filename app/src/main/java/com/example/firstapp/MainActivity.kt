package com.example.firstapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.firstapp.ViewPage.ViewPageAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewPageAdapter : ViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewPageAdapter = ViewPageAdapter(supportFragmentManager, lifecycle)
        mainPager.adapter = viewPageAdapter

        //TabLayoutMediator를 만들고, 그 임시객체를 이용해 tab의 제목가 아이콘을 동적으로 설정한뒤
        //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        TabLayoutMediator(tabLayout, mainPager){ tab, position->
            when(position){
                0-> {
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.fire, null)
                }
                1->{
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.chat, null)
                }
                2->{
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.profile, null)
                }

            }
        //탭 레이아웃에 메인페이저를 붙인다.(연동한다)
        }.attach()




    }

}


