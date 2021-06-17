package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.cardview.widget.CardView
import com.unionbankph.corporate.R

class UploadPhotosCustomAdapter : BaseAdapter() {

    val uploadPhotoList = arrayListOf<UploadPhotos>()

    override fun getCount(): Int {
        return uploadPhotoList.size
    }

    override fun getItem(position: Int): Any {
        return uploadPhotoList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.activity_onboarding_upload_photos, null)

//        val card = view.findViewById<CardView>(R.id.card_view)
        return view
    }

}