package com.unionbankph.corporate.dao.domain.form

import com.unionbankph.corporate.dao.data.form.DaoForm
import java.io.File

data class SignatureForm(
    var file: File? = null,
    var daoForm: DaoForm? = null
)