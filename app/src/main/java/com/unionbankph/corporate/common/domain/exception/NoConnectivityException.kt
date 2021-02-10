package com.unionbankph.corporate.common.domain.exception

import android.content.Context
import com.unionbankph.corporate.R
import java.io.IOException

class NoConnectivityException(context: Context) :
    IOException(context.getString(R.string.msg_no_internet_connection))
