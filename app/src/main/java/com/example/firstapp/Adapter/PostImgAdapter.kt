package com.example.firstapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import com.example.firstapp.R
import kotlinx.android.synthetic.main.post_img_item.view.*

class PostImgAdapter(context: Context, resourceID: Int, bitmaps: ArrayList<Bitmap>) :
    ArrayAdapter<Bitmap>(context, resourceID, bitmaps) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        lateinit var view :View
        var bitmap = getItem(position)

        if(convertView != null)
            view = convertView
        else
        {
            view =
            LayoutInflater.from(context).inflate(R.layout.post_img_item, parent, false)

        }

        bitmap?.let { view.iv_post_image.setImageBitmap(it) }
        return view
    }
}