package com.unionbankph.corporate.app.common.widget.edittext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R

class PincodeEditText(
    private val activity: Activity,
    private val view: ConstraintLayout,
    private val button: Button,
    private val editTextHidden: EditText,
    private val etPin1: EditText,
    private val etPin2: EditText,
    private val etPin3: EditText,
    private val etPin4: EditText,
    private val etPin5: EditText,
    private val etPin6: EditText
) : View.OnKeyListener, View.OnTouchListener {

    private lateinit var onOTPCallback: OnOTPCallback

    private var etPins = arrayOf<EditText>()

    init {
        etPins = arrayOf(etPin1, etPin2, etPin3, etPin4, etPin5, etPin6)
        editTextHidden.addTextChangedListener(PinCodeTextWatcher(editTextHidden))
        editTextHidden.setOnKeyListener(this)
        editTextHidden.setOnClickListener(null)
        etPin1.setOnTouchListener(this)
        etPin2.setOnTouchListener(this)
        etPin3.setOnTouchListener(this)
        etPin4.setOnTouchListener(this)
        etPin5.setOnTouchListener(this)
        etPin6.setOnTouchListener(this)
        etPin1.setOnClickListener(null)
        etPin2.setOnClickListener(null)
        etPin3.setOnClickListener(null)
        etPin4.setOnClickListener(null)
        etPin5.setOnClickListener(null)
        etPin6.setOnClickListener(null)
        editTextHidden.requestFocus()
    }

    private inner class PinCodeTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.editTextHidden -> {
                    editable.forEachIndexed { index, c ->
                        etPins[index].setText(c.toString())
                    }
                    enableButton()
                    setupCursor()
                    submitOTP()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p1?.action) {
            MotionEvent.ACTION_UP -> {
                when (p0?.id) {
                    R.id.etPin1, R.id.etPin2, R.id.etPin3, R.id.etPin4, R.id.etPin5, R.id.etPin6 -> {
                        setupCursor()
                        editTextHidden.requestFocus()
                        if (!isSoftKeyboardShown(view)) showKeyboard(activity)
                    }
                }
            }
        }
        return false
    }

    override fun onKey(view: View?, p1: Int, p2: KeyEvent?): Boolean {
        if (p2?.action == KeyEvent.ACTION_DOWN && p2.keyCode == KeyEvent.KEYCODE_DEL) {
            if (editTextHidden.selectionEnd >= 1) {
                etPins[editTextHidden.selectionEnd - 1].text?.clear()
                setupCursor()
            }
        }
        return false
    }

    private fun enableButton() {
        if (editTextHidden.text.length == 6) {
            button.isEnabled = true
            button.setTextColor(ContextCompat.getColor(activity, R.color.colorWhite))
            if (editTextHidden.selectionEnd >= 5) dismissKeyboard(activity)
        } else {
            editTextHidden.imeOptions =
                    if (editTextHidden.text.length == 5)
                        EditorInfo.IME_ACTION_DONE
                    else
                        EditorInfo.IME_ACTION_NEXT

            button.isEnabled = false
            button.setTextColor(ContextCompat.getColor(activity, R.color.colorWhite50))
        }
    }

    private fun setupCursor() {
        etPins.forEachIndexed { index, et ->
            if (et.text.isEmpty() && index == editTextHidden.text.length) {
                etPins[index].background = ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_edittext_otp_selected
                )
            } else {
                etPins[index].background = ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_edittext_otp_normal
                )
            }
        }
    }

    private fun submitOTP() {
        if (editTextHidden.text.length == 6) {
            onOTPCallback.onSubmitOTP(editTextHidden.text.toString())
        }
    }

    private fun isSoftKeyboardShown(view: View?): Boolean {
        val r = Rect()
        // r will be populated with the coordinates of your view that area still
        // visible.
        var keypadHeight = 0
        var screenHeight = 0
        // r will be populated with the coordinates of your view that area still visible.
        if (view != null) {
            view.getWindowVisibleDisplayFrame(r)
            screenHeight = view.rootView.height
            keypadHeight = screenHeight - r.bottom
        }
        return keypadHeight > screenHeight * 0.15
    }

    private fun showKeyboard(activity: Activity) {
        val mContext = activity.applicationContext
        val inputMethodManager = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun dismissKeyboard(activity: Activity) {
        val focus = activity.currentFocus
        if (focus != null) {
            val mContext = activity.applicationContext
            val inputMethodManager = mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(focus.windowToken, 0)
        }
    }

    fun setOnOTPCallback(onOTPCallback: OnOTPCallback) {
        this.onOTPCallback = onOTPCallback
    }

    fun clearPinCode() {
        editTextHidden.text?.clear()
        etPin1.text?.clear()
        etPin2.text?.clear()
        etPin3.text?.clear()
        etPin4.text?.clear()
        etPin5.text?.clear()
        etPin6.text?.clear()
    }

    fun getPinCode() = editTextHidden.text.toString()

    fun setPinCode(pinCode: String) {
        editTextHidden.setText(pinCode)
    }

    fun getEditTextHidden(): EditText = editTextHidden

    interface OnOTPCallback {
        fun onSubmitOTP(otpCode: String)
    }
}
