package com.unionbankph.corporate.app.common.widget.edittext

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.util.ViewUtil

class ImeOptionEditText : TextView.OnEditorActionListener, View.OnFocusChangeListener {

    private var preEditTextList: MutableList<EditText> = mutableListOf()

    private var onImeOptionListener: OnImeOptionListener? = null

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        onImeOptionListener?.onFocusEditText(v, hasFocus)
        if (hasFocus) {
            val filteredEditText = preEditTextList.filter { checkerEditText(it) }
            if (filteredEditText.isEmpty()) {
                (v as EditText).imeOptions =
                    EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            } else if (filteredEditText.size == 1) {
                val findFocusedEditText = filteredEditText.last()
                if (findFocusedEditText.id != v.id) {
                    (v as EditText).imeOptions = EditorInfo.IME_ACTION_NEXT
                } else {
                    (v as EditText).imeOptions =
                        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                }
            } else {
                filteredEditText.find { checkerEditText(it) }?.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            var findIndex = 0
            preEditTextList.forEachIndexed { index, editText ->
                if (v?.id == editText.id) {
                    findIndex = index
                    return@forEachIndexed
                }
            }
            if (findIndex.plus(1) == preEditTextList.size) {
                preEditTextList.find { checkerEditText(it) }?.requestFocus()
            } else {
                val filteredEditText = preEditTextList
                    .subList(findIndex.plus(1), preEditTextList.size)
                val foundEditText = filteredEditText.find { checkerEditText(it) }
                if (foundEditText == null) {
                    preEditTextList.find { checkerEditText(it) }?.requestFocus()
                } else {
                    foundEditText.requestFocus()
                }
                if (preEditTextList.filter { checkerEditText(it) }.size != 1) {
                    foundEditText?.imeOptions = EditorInfo.IME_ACTION_NEXT
                }
            }
            return true
        } else if (actionId == EditorInfo.IME_ACTION_DONE) {
            onImeOptionListener?.onDoneKeyAction(v, actionId, event)
            return false
        }
        return false
    }

    private fun checkerEditText(editText: EditText): Boolean {
        return if (editText.id == R.id.et_amount){
            val regex = ViewUtil.REGEX_FORMAT_AMOUNT.toRegex()
            !regex.containsMatchIn(input = editText.text)
        } else {
            editText.text.isEmpty()
        }
    }

    fun addEditText(vararg args: EditText) {
        preEditTextList.addAll(args)
    }

    fun startListener() {
        preEditTextList.forEach {
            it.setOnEditorActionListener(this)
            it.onFocusChangeListener = this
        }
    }

    fun removeListener() {
        preEditTextList.clear()
        preEditTextList.forEach {
            it.setOnEditorActionListener(this)
            it.onFocusChangeListener = null
        }
    }

    fun setOnImeOptionListener(onImeOptionListener: OnImeOptionListener) {
        this.onImeOptionListener = onImeOptionListener
    }

    interface OnImeOptionListener {
        fun onDoneKeyAction(v: TextView?, actionId: Int, event: KeyEvent?) = Unit
        fun onFocusEditText(v: View, hasFocus: Boolean) = Unit
    }
}
