package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Default.Post
import com.example.firstapp.R
import kotlinx.android.synthetic.main.mypost_item.view.*
import kotlin.math.floor

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

        var likeSum = post?.getLikeSum() ?: 0
        var stringLikes = if(likeSum >= 1000) "${floor(likeSum / 100.0F) / 10.0F}K" else likeSum.toString()


        view.tv_mypost_date.text = post?.postInfo?.date?.substring(IntRange(0, 9))
        view.tv_mypost_title.text = post?.postInfo?.title
        view.tv_mypost_like.text = stringLikes
        view.iv_mypost_tumbnail.setImageBitmap(post?.tumbnail)
        post?.onTumbnailSet = { view.pb_mypost_loading.visibility = View.INVISIBLE }


        return view
    }


}