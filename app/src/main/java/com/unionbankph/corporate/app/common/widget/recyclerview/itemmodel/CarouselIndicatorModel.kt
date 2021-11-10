package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.CarouselModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.decoration.OffSetCirclePagerIndicatorDecoration



class CarouselIndicatorModel : CarouselModel_() {

    val indicator = OffSetCirclePagerIndicatorDecoration()

    override fun bind(carousel: Carousel) {
        super.bind(carousel)

//        carousel.addItemDecoration(indicator)
    }

    override fun unbind(carousel: Carousel) {
        super.unbind(carousel)

//        carousel.removeItemDecoration(indicator)
    }
}
