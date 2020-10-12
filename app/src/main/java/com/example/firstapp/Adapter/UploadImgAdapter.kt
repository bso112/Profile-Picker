package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Activity.MyBitmap
import com.example.firstapp.R
import kotlinx.android.synthetic.main.upload_img_item.view.*
import java.util.*

class UploadImgAdapter(context : Context, resourceID : Int, bitmaps: LinkedList<MyBitmap>) :
    ArrayAdapter<MyBitmap>(context, resourceID,bitmaps ) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var bitmap = getItem(position)

        val cardView: View = LayoutInflater.from(context).inflate(R.layout.upload_img_item, parent, false)

        cardView.iv_upload_picture.setImageBitmap(bitmap?.bitmap)
        cardView.btn_upload_cancel.setOnClickListener {
            remove(bitmap)
            notifyDataSetChanged()
        }
        return cardView
    }
}