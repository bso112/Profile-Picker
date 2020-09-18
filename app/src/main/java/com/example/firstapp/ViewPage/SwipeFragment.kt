package com.example.firstapp.ViewPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.firstapp.Card.Card
import com.example.firstapp.Card.CardAdapter
import com.example.firstapp.R
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.frag_swipe.*


class SwipeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_swipe, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_btn)
        //dislike 버튼 눌렀을때
        btn_dislike.setOnClickListener {
            swipeView.topCardListener.selectLeft()
            it.startAnimation(btnAnim)
        }
        //like 버튼 눌렀을때
        btn_like.setOnClickListener {
            swipeView.topCardListener.selectRight();
            it.startAnimation(btnAnim)
        }


        //make cards
        val cards = arrayListOf<Card>(Card("Lena", R.drawable.face), Card("manta", R.drawable.fire))

        if (null == context) {
            Log.d("SwipeFragment", "Context is null")
            return
        }


        //craete cardAdapter
        var arrayAdapter = CardAdapter(context!!, R.layout.swipe_item, cards)

        //set the listener and the adapter
        swipeView.adapter = arrayAdapter
        swipeView.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!")
                cards.removeAt(0)
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
            }

            override fun onRightCardExit(dataObject: Any) {
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                // Ask for more data here

            }

            override fun onScroll(p0: Float) {
                // not implemented
            }
        })


    }
}