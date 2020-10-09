package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.R
import kotlinx.android.synthetic.main.statistic_picture_item.view.*
import kotlin.math.floor

class StatisticPictureAdapter(context: Context, resource: Int, myPictures: ArrayList<MyPicture>) :
    ArrayAdapter<MyPicture>(context, resource, myPictures) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View;
        val picture = getItem(position)

        if (convertView != null)
            view = convertView
        else
            view =
                LayoutInflater.from(context).inflate(R.layout.statistic_picture_item, parent, false)


        view.iv_statistic_picture.setImageBitmap(picture?.bitmap)

        if (picture != null) {
            var likeSum = 0.0f
            for (i in 0 until count) {
                val pic = getItem(i)
                likeSum += pic?.likes ?: 0
            }
            val persent: Int = (picture.likes / likeSum.coerceAtLeast(1F) * 100).toInt()
            view.tv_statistic_likes_persent.text = "$persent%"

            var stringLikes = if(picture.likes >= 1000) "${floor(picture.likes / 100.0F) / 10.0F}K" else picture.likes.toString()
            view.tv_statistic_likes_absolute.text = "(${stringLikes}명)"
        }


        return view

    }

}