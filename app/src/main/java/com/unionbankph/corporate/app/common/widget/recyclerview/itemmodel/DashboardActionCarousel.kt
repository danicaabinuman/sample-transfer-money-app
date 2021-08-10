package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DashboardActionCarousel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Carousel(context, attrs, defStyleAttr) {

    init {
        setDefaultGlobalSnapHelperFactory(null)
        setDefaultItemSpacingDp(0)
        setPaddingDp(0)
    }

    override fun createLayoutManager(): LayoutManager {
        return GridLayoutManager(
            context,
            4,
            RecyclerView.VERTICAL,
            false)
    }
}
