package com.unionbankph.corporate.settings.presentation.country

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemCountryBinding

class CountryController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : TypedEpoxyController<MutableList<CountryCode>>() {

    private lateinit var callbacks: EpoxyAdapterCallback<CountryCode>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<CountryCode>) {
        data.forEachIndexed { index, countryCode ->
            countryItem {
                id(countryCode.id)
                countryCode(countryCode)
                position(index)
                viewUtil(this@CountryController.viewUtil)
                callbacks(this@CountryController.callbacks)
                context(this@CountryController.context)
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<CountryCode>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_country)
abstract class CountryItemModel : EpoxyModelWithHolder<CountryItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var countryCode: CountryCode

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<CountryCode>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        if (position != 0) {
            holder.binding.viewBorderTop.visibility = View.GONE
        }
        holder.binding.textViewName.text = countryCode.name
        holder.binding.textViewCallingCode.text = countryCode.callingCode
        holder.binding.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${countryCode.code?.toLowerCase()}")
        )
        holder.binding.constraintLayoutItem.setOnClickListener {
            callbacks.onClickItem(holder.binding.constraintLayoutItem, countryCode, position)
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemCountryBinding

        override fun bindView(itemView: View) {
            binding = ItemCountryBinding.bind(itemView)
        }
    }
}
