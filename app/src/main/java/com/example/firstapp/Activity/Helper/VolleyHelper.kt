package com.example.firstapp.Activity.Helper

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class VolleyHelper (val context : Context){

    var mRequestQueue : RequestQueue = Volley.newRequestQueue(context.applicationContext)


    companion object {

        private var INSTANCE : VolleyHelper? = null

        fun getInstance(context: Context) : VolleyHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = VolleyHelper(context)
                return INSTANCE as VolleyHelper
            }
    }

    fun <T> addRequestQueue(req : Request<T>)
    {
        mRequestQueue.add(req)
    }

}