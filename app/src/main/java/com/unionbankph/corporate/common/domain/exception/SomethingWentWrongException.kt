package com.unionbankph.corporate.common.domain.exception

import android.content.Context
import com.unionbankph.corporate.R
import java.io.IOException

class SomethingWentWrongException(context: Context) :
    IOException(context.getString(R.string.error_something_went_wrong))
