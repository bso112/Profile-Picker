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

        var likes = post?.getTumbnailPicture()?.likes ?: 0
        var stringLikes = if(likes >= 1000) "${floor(likes / 100.0F) / 10.0F}K" else likes.toString()


        view.tv_post_date.text = post?.postInfo?.date?.substring(IntRange(0, 9))
        view.tv_post_title.text = post?.postInfo?.title
        view.tv_post_like.text = stringLikes
        view.iv_post_tumbnail.setImageBitmap(post?.tumbnail)


        return view
    }


}