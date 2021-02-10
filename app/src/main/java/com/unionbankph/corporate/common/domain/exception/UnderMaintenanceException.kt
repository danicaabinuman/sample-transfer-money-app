package com.unionbankph.corporate.common.domain.exception

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import java.io.IOException

class UnderMaintenanceException(context: Context) :
    IOException(context.formatString(R.string.error_msg_under_maintenance))
