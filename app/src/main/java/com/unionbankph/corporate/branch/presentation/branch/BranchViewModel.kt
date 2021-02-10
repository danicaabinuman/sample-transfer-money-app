package com.unionbankph.corporate.branch.presentation.branch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class BranchViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val branchVisitGateway: BranchVisitGateway
) : BaseViewModel() {

    private val _branchVisitFormState = MutableLiveData<BranchState>()

    private val branchesMutableLiveData = MutableLiveData<MutableList<Branch>>()

    val state: LiveData<BranchState> get() = _branchVisitFormState

    val branchesState: LiveData<MutableList<Branch>> get() = branchesMutableLiveData

    fun getBranches() {
        branchVisitGateway.getBranches()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _branchVisitFormState.value = ShowBranchLoading
            }
            .doFinally {
                _branchVisitFormState.value = ShowBranchDismissLoading
            }
            .subscribe(
                {
                    branchesMutableLiveData.value = it
                },
                {
                    _branchVisitFormState.value = ShowBranchError(it)
                    Timber.e(it, "getBranches Failed")
                }
            ).addTo(disposables)
    }

    fun filterList(filter: String): MutableList<Branch> {
        return if (filter != "") {
            branchesState.value.notNullable().filter {
                    it.name?.contains(filter, true) == true ||
                            it.address?.contains(filter, true) == true
                }
                .toMutableList()
        } else {
            branchesState.value.notNullable()
        }
    }
}

sealed class BranchState

object ShowBranchLoading : BranchState()

object ShowBranchEndlessLoading : BranchState()

object ShowBranchDismissLoading : BranchState()

object ShowBranchDismissEndlessLoading : BranchState()

data class ShowBranchError(val throwable: Throwable) : BranchState()
