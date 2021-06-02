package com.unionbankph.corporate.settings.presentation.learn_more

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import javax.inject.Inject

/**
 * Created by herald on 1/27/21
 */
class LearnMoreViewModel
@Inject
constructor(
    private val context: Context
) : BaseViewModel() {

    private val _learnMoreData = MutableLiveData<MutableList<LearnMoreData>>()

    val learnMoreData: LiveData<MutableList<LearnMoreData>> get() = _learnMoreData

    init {
        val learnMoreTitles =
            context.resources.getStringArray(R.array.array_learn_more_title).toMutableList()
        val learnMoreDescriptions =
            context.resources.getStringArray(R.array.array_learn_more_desc).toMutableList()
        val learnMoreData = mutableListOf<LearnMoreData>()
        learnMoreTitles
            .forEachIndexed { index, data ->
                if (App.isSME() && data == context.formatString(R.string.title_learn_more_enroll)) {
                    learnMoreTitles[index] =
                        context.formatString(R.string.title_learn_more_enroll_sme)
                }
                learnMoreData.add(
                    LearnMoreData(
                        id = index.plus(1),
                        title = learnMoreTitles[index],
                        desc = learnMoreDescriptions[index]
                    )
                )
            }
        _learnMoreData.value = learnMoreData
    }

}