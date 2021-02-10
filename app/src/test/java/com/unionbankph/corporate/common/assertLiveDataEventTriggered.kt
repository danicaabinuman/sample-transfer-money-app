package com.unionbankph.corporate.common

import androidx.lifecycle.LiveData
import com.unionbankph.corporate.app.common.platform.events.Event
import org.junit.Assert.assertEquals

fun assertLiveDataEventTriggered(
    liveData: LiveData<Event<String>>,
    taskId: String
) {
    val value = liveData.getOrAwaitValue()
    assertEquals(value.getContentIfNotHandled(), taskId)
}

fun assertSnackbarMessage(snackbarLiveData: LiveData<Event<Int>>, messageId: Int) {
    val value: Event<Int> = snackbarLiveData.getOrAwaitValue()
    assertEquals(value.getContentIfNotHandled(), messageId)
}


