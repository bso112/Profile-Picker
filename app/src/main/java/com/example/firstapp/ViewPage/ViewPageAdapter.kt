package com.example.firstapp.ViewPage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.firstapp.ViewPage.MessageFragment
import com.example.firstapp.ViewPage.ProfileFragment
import com.example.firstapp.ViewPage.SwipeFragment

class ViewPageAdapter(var fm : FragmentManager) : FragmentPagerAdapter(fm) {

    //뷰 페이지를 이루는 프래그먼트 수
    override fun getCount(): Int {
        return 3
    }

    // position은 누른 탭의 인덱스고, 인덱스마다 알맞은 프래그먼트를 생성한다.
    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> SwipeFragment()
            1 -> MessageFragment()
            2 -> ProfileFragment()
            else -> Fragment() //이 경우 어떻게 되는거지?
        }
    }




}