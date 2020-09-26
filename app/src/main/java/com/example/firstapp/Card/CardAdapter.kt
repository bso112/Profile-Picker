package com.example.firstapp.Card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.firstapp.R

class CardAdapter (context : Context, resourceID : Int):
    ArrayAdapter<Card>(context, resourceID) {


    //getView는 view가 필요할때 즉, 화면에 view가 보여야할때 불린다.
    //view마다 불리기 때문에 여러번 불린다.
    //position 은 어떤 포지션이지?
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val card : Card? = getItem(position)

        /*
        Inflater는 XML을 실제로 메모리에 올리는 역할을 한다. inflate을 통해 메모리에 올리고,
        그렇게 생성한 객체를 반환한다.
         */
        val cardView : View = LayoutInflater.from(context).inflate(R.layout.swipe_item, parent, false)

        //생성된 카드뷰에서 객체를 이미지뷰를 받아온다.
        val swipeImg : ImageView = cardView.findViewById(R.id.swipImg)
        // 이미지뷰에 이미지를 셋팅한다.
        card?.let {swipeImg.setImageBitmap(card.bitmap) }

        //만든 카드뷰를 리턴한다.
        return cardView
    }


}