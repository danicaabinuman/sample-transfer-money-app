package com.unionbankph.corporate.app.receiver

import android.os.Bundle
import android.os.ResultReceiver
import timber.log.Timber

class IMMResultReceiver : ResultReceiver(null) {
    var result = -1

    public override fun onReceiveResult(r: Int, data: Bundle?) {
        result = r
    }

    // poll result value for up to 500 milliseconds
    fun getRes(): Int {
        try {
            var sleep = 0
            while (result == -1 && sleep < 500) {
                Thread.sleep(100)
                sleep += 100
            }
        } catch (e: InterruptedException) {
            Timber.e(e.message)
        }

        return result
    }
}
