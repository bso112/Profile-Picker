package com.example.firstapp.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstapp.ViewPage.MessageFragment
import com.example.firstapp.ViewPage.ProfileFragment
import com.example.firstapp.ViewPage.SwipeFragment


class ViewPageAdapter(fm : FragmentManager, lifecycle : Lifecycle) : FragmentStateAdapter(fm, lifecycle){


  //뷰를 이루는 프래그먼트 수
    override fun getItemCount(): Int {
        return 3
    }
    // position은 누른 탭의 인덱스고, 인덱스마다 알맞은 프래그먼트를 생성한다.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SwipeFragment()
            1 -> MessageFragment()
            2 -> ProfileFragment()
            else -> Fragment() //이 경우 어떻게 되는거지?
        }
    }

    //페이지의 제목을 설정한다.
    //이걸하면 페이저가 소속된 탭의 이름이 리턴된 문자열이 된다.
//    override fun getPageTitle(position: Int): CharSequence? {
//        // Return tab text label for position
//    }


}