package com.unionbankph.corporate.app.common.extension

import android.util.Patterns
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoFragment
import java.util.regex.Pattern


fun String?.isValidEmail(): Boolean {
    return !this.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String?.isValidPhone(): Boolean {
    val phonePh = Pattern.compile("^[8-9]\\d+\$")
    return !this.isNullOrBlank() && phonePh.matcher(this).matches()
}

