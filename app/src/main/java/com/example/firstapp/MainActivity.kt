package com.example.firstapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firstapp.ViewPage.ViewPageAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewPageAdapter : ViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPageAdapter = ViewPageAdapter(supportFragmentManager)
        mainPager.adapter = viewPageAdapter

        tabLayout.setupWithViewPager(mainPager)



    }

}


