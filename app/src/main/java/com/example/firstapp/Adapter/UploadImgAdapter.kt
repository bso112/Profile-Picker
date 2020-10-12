package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Activity.Helper.makeSimpleAlert
import com.example.firstapp.Activity.MyBitmap
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.R
import kotlinx.android.synthetic.main.upload_img_item.view.*
import java.util.*

class UploadImgAdapter(context : Context, resourceID : Int, bitmaps: ArrayList<MyPicture>) :
    ArrayAdapter<MyPicture>(context, resourceID,bitmaps ) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var bitmap = getItem(position)

        val cardView: View = LayoutInflater.from(context).inflate(R.layout.upload_img_item, parent, false)

        cardView.iv_upload_picture.setImageBitmap(bitmap?.bitmap)
        cardView.btn_upload_cancel.setOnClickListener {
            //경고
            makeSimpleAlert(context, "경고","선택한 사진의 모든 정보가 삭제됩니다. 지우시겠습니까?",{
                remove(bitmap)
                notifyDataSetChanged()
            })
        }
        return cardView

    }


}