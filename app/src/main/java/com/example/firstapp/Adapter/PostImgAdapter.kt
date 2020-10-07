package com.example.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.R
import kotlinx.android.synthetic.main.post_img_item.view.*

//스와이프 프래그먼트에서 게시물 클릭했을때 이미지 보여주는 어댑터
class PostImgAdapter(context: Context, resourceID: Int, bitmaps: ArrayList<MyPicture>, val mSelected : ArrayList<Boolean>) :
    ArrayAdapter<MyPicture>(context, resourceID, bitmaps) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        lateinit var view :View
        var picture = getItem(position)


        if(convertView != null)
            view = convertView
        else
            view =
            LayoutInflater.from(context).inflate(R.layout.post_img_item, parent, false)

        //mSelected를 참고해서 선택 이미지를 끄고킨다.
        if(mSelected.size > position)
            view.iv_vote.visibility = if(mSelected[position]) View.VISIBLE else View.INVISIBLE

        picture?.let { view.iv_post_image.setImageBitmap(it.bitmap) }

        return view
    }
}