package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.unionbankph.corporate.R

internal class UploadPhotosCustomAdapter(
    private val context: Context,
    private val image: ArrayList<Uri>
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var imageView: ImageView

    override fun getCount(): Int {
        return image.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (layoutInflater == null){
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null){
            convertView = layoutInflater!!.inflate(R.layout.layout_onboarding_selected_photos, null)
        }
        imageView = convertView!!.findViewById(R.id.ivPhotos)
        imageView.setImageURI(image[position])

//        val card = view.findViewById<CardView>(R.id.card_view)
        return convertView
    }

}