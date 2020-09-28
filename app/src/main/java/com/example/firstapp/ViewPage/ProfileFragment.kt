package com.example.firstapp.ViewPage


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firstapp.FIRSTAPP_USERNAME
import com.example.firstapp.MainActivity
import com.example.firstapp.R
import com.example.firstapp.UploadImgActivity
import kotlinx.android.synthetic.main.frag_profile.*

class ProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.frag_profile, container, false)
    }

    override fun onStart() {
        super.onStart()
        ib_profile_create.setOnClickListener{
            val intent = Intent(context, UploadImgActivity::class.java)
            startActivity(intent)
        }
    }


}