package com.unionbankph.corporate.app.common.widget.validator

import java.util.regex.Pattern

class EmailValidator : PatternMatchesValidator {

    constructor() : super(DEFAULT_MESSAGE, android.util.Patterns.EMAIL_ADDRESS)

    constructor(invalidEmailMessage: String) : super(invalidEmailMessage, android.util.Patterns.EMAIL_ADDRESS)

    constructor(invalidEmailMessage: String, pattern: Pattern) : super(invalidEmailMessage, pattern)

    companion object {

        private val DEFAULT_MESSAGE = "Invalid receiveEmail"
    }
}
