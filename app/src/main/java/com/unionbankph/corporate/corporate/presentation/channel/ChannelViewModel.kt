package com.unionbankph.corporate.corporate.presentation.channel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class ChannelViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val corporateGateway: CorporateGateway,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val context: Context
) : BaseViewModel() {

    private val _billers = MutableLiveData<MutableList<Biller>>()

    val billers: LiveData<MutableList<Biller>> get() = _billers

    private val _sectionedChannel = MutableLiveData<MutableList<SectionedData<Channel>>>()

    val sectionedChannels: LiveData<MutableList<SectionedData<Channel>>> get() = _sectionedChannel

    val page = BehaviorSubject.create<String>()

    fun setupPage(page: String?) {
        this.page.onNext(page.notNullable())
    }

    fun getAccountChannels() {
        getChannels(page.value.notNullable(), "gov")
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _billers.value = it.second
                    _sectionedChannel.value = it.first
                }, {
                    Timber.e(it, "getChannels failed")
                    _uiState.value = Event(UiState.Error(it))
                }).addTo(disposables)
    }

    private fun getChannels(
        page: String,
        type: String
    ): Observable<Pair<MutableList<SectionedData<Channel>>, MutableList<Biller>>> {
        val productId = if (page.equals(ChannelActivity.PAGE_BILLS_PAYMENT, true)) {
            "3"
        } else {
            "2"
        }
        val channelObservable = corporateGateway.getChannels(productId)
            .flatMap { channels ->
                return@flatMap mapChannels(page, channels)
            }
            .toObservable()

        val billersObservable = if (page.equals(ChannelActivity.PAGE_BILLS_PAYMENT, true)) {
            billsPaymentGateway.getBillers(type)
        } else {
            Single.just(mutableListOf())
        }.toObservable()

        return channelObservable.zipWith(billersObservable) { t1, t2 -> Pair(t1, t2) }
    }

    private fun mapChannels(
        page: String,
        channels: MutableList<Channel>
    ): Single<MutableList<SectionedData<Channel>>> {
        val sectionedChannels = mutableListOf<SectionedData<Channel>>()
        if (page.equals(ChannelActivity.PAGE_FUND_TRANSFER, true) ||
            page.equals(ChannelActivity.PAGE_BENEFICIARY, true)
        ) {
            val ownChannel =
                channels.find {
                    it.contextChannel?.group.equals(Constant.UNIONBANK_ONLY, true) &&
                            it.name.equals(ChannelBankEnum.TO_OWN_ACCOUNT.name, true)
                }
            val filteredUBPChannels =
                channels.filter {
                    it.contextChannel?.group.equals(Constant.UNIONBANK_ONLY, true) &&
                            it.name.equals(ChannelBankEnum.UBP_TO_UBP.name, true)
                }.toMutableList()
            filteredUBPChannels.forEach { channel ->
                ownChannel?.let {
                    channel.hasRulesAllowTransaction =
                        channel.hasRulesAllowTransaction || ownChannel.hasRulesAllowTransaction
                    channel.hasSourceAccount =
                        channel.hasSourceAccount || ownChannel.hasSourceAccount
                    channel.hasApprovalRule = channel.hasApprovalRule || ownChannel.hasApprovalRule
                    channel.hasPermission = channel.hasPermission || ownChannel.hasPermission
                }
            }
            val sectionedUBPChannels = SectionedData<Channel>()
            sectionedUBPChannels.type = Constant.UNIONBANK_ONLY
            sectionedUBPChannels.header =
                context.formatString(R.string.title_send_funds_ubp)
            sectionedUBPChannels.data = filteredUBPChannels
            sectionedChannels.add(sectionedUBPChannels)

            val filteredOtherChannels = channels
                .filter { it.contextChannel?.group.equals(Constant.OTHER_BANKS, true) }
                .asSequence().sortedWith(
                    compareBy(
                        { channel -> channel.name.equals(ChannelBankEnum.SWIFT.name, true) },
                        { channel -> channel.name.equals(ChannelBankEnum.PDDTS.name, true) },
                        { channel -> channel.name.equals(ChannelBankEnum.INSTAPAY.name, true) },
                        { channel -> channel.name.equals(ChannelBankEnum.PESONET.name, true) }
                    )
                )
                .toMutableList()

            val sectionedOtherChannels = SectionedData<Channel>()
            sectionedOtherChannels.type = Constant.OTHER_BANKS
            sectionedOtherChannels.header =
                context.formatString(R.string.title_send_funds_other_banks)
            sectionedOtherChannels.data = filteredOtherChannels
            sectionedChannels.add(sectionedOtherChannels)
        } else {
            val filteredBPChannels = channels
                .filter {
                    it.name.equals(ChannelBankEnum.BILLS_PAYMENT.name, true) &&
                            it.product?.name.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)
                }
                .toMutableList()
            val sectionedBPChannels = SectionedData<Channel>()
            sectionedBPChannels.type = Constant.BILLS_PAYMENT
            sectionedBPChannels.header =
                context.formatString(R.string.title_bills_payment)
            sectionedBPChannels.data = filteredBPChannels
            sectionedChannels.add(sectionedBPChannels)

            if (!App.isSupportedInProduction) {
                val filteredEGovChannels = channels
                    .filter {
                        !it.name.equals(ChannelBankEnum.BILLS_PAYMENT.name, true) &&
                                it.product?.name.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) &&
                                !it.name.equals("PhilHealth", true)
                    }
                    .toMutableList()
                val sectionedEGovChannels = SectionedData<Channel>()
                sectionedEGovChannels.type = Constant.EGOV_PAYMENT
                sectionedEGovChannels.header =
                    context.formatString(R.string.title_government_payment)
                sectionedEGovChannels.data = filteredEGovChannels
                sectionedChannels.add(sectionedEGovChannels)
            }
        }
        sectionedChannels.forEach {
            it.data.forEach lit@{ channel ->
                channel.reminders?.let {
                    val reminders = it.notNullable()
                    val splittedReminders = reminders.split("|")
                    val formattedReminders = mutableListOf<String>()
                    splittedReminders.forEachIndexed { index, reminder ->
                        formattedReminders.add("${index.plus(1)}. $reminder")
                    }
                    channel.formattedReminders = formattedReminders
                }
            }
        }
        return Single.just(sectionedChannels)
    }

}
