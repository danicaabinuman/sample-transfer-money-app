package com.unionbankph.corporate.link_details.presentation

import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.presentation.login.*
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class LinkDetailsViewModel
@Inject constructor(
        private val linkDetailsGateway: LinkDetailsGateway,
        private val schedulerProvider: SchedulerProvider
) : BaseViewModel(){

    private val _linkDetailsState = MutableLiveData<LinkDetailsState>()

    fun generateLinkDetails(linkDetailsForm: LinkDetailsForm){
        linkDetailsGateway.linkGateway(linkDetailsForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _linkDetailsState.value = ShowLinkDetailsLoading }
            .doFinally { _linkDetailsState.value = ShowLinkDetailsDismissLoading }
            .subscribe(
                {
                    it?.let {
                        if (it.referenceId != null) {
                            _linkDetailsState.value = ShowLinkDetailsDismissLoading
                            if (it.link != null) {
                                _linkDetailsState.value = ShowLinkDetailsSuccess(it)
                            } else {
                                _linkDetailsState.value = ShowLinkDetailsError(Throwable("Link Unavailable"))
                            }
                        } else {
                            _linkDetailsState.value = ShowLinkDetailsError(Throwable("Reference Id unavailable"))
                        }
                    }

                }, {
                    Timber.e(it, "Link details error")
                    _linkDetailsState.value = ShowLinkDetailsError(it)
                })
            .addTo(disposables)


    }

}

sealed class LinkDetailsState

object ShowLinkDetailsLoading : LinkDetailsState()

object ShowLinkDetailsDismissLoading : LinkDetailsState()

data class ShowLinkDetailsSuccess(val linkResponse: LinkDetailsResponse) : LinkDetailsState()

data class ShowLinkDetailsError(val throwable: Throwable) : LinkDetailsState()