package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Activity.ViewPage.PostInfo
import com.example.firstapp.R
import kotlinx.android.synthetic.main.mypost_item.view.*
import java.util.zip.Inflater

class MyPostAdapter(context: Context, resource: Int, postInfo: ArrayList<PostInfo>) :
    ArrayAdapter<PostInfo>(context, resource, postInfo) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View
        val postInfo = getItem(position)

        if (convertView != null)
            view = convertView
        else
            view = LayoutInflater.from(context).inflate(R.layout.mypost_item, parent, false)

        view.tv_date.text = postInfo?.date
        view.tv_post_title.text = postInfo?.title

        return view
    }


}