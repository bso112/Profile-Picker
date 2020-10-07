package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Activity.ViewPage.Post
import com.example.firstapp.R
import kotlinx.android.synthetic.main.mypost_item.view.*

//내가 쓴 글목록 보여주는 어댑터
class MyPostAdapter(context: Context, resource: Int, val posts: ArrayList<Post>) :
    ArrayAdapter<Post>(context, resource, posts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View
        val post = getItem(position)

        if (convertView != null)
            view = convertView
        else
            view = LayoutInflater.from(context).inflate(R.layout.mypost_item, parent, false)

        view.tv_date.text = post?.postInfo?.date
        view.tv_post_title.text = post?.postInfo?.title
        view.iv_post_tumbnail.setImageBitmap(post?.tumbnail)

        return view
    }





}