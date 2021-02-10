package com.unionbankph.corporate.app.common.widget.edittext.autoformat;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.unionbankph.corporate.R;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

import timber.log.Timber;

public class EasyMoneyEditText extends TextInputEditText {

    private String _currencySymbol;
    private boolean _showCurrency;
    private boolean _showCommas;
    private boolean isEmpty = true;
    private boolean hasLimit = true;
    private boolean hasToolTip = false;
    private long emptyValue = -1;

    private ToolTipListener toolTipListener;

    public EasyMoneyEditText(Context context) {
        super(context);
        initView(context, null);
    }

    public EasyMoneyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public void setToolTipListener(ToolTipListener toolTipListener) {
        this.toolTipListener = toolTipListener;
        this.hasToolTip = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateValue(getText().toString());
    }

    private void initView(Context context, AttributeSet attrs) {
        // Setting Default Parameters
        _currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        _showCurrency = true;
        _showCommas = true;

        // Check for the attributes
        if (attrs != null) {
            // Attribute initialization
            final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.EasyMoneyWidgets, 0, 0);
            try {
                String currnecy = attrArray.getString(R.styleable.EasyMoneyWidgets_currency_symbol);
                if (currnecy == null)
                    currnecy = Currency.getInstance(Locale.getDefault()).getSymbol();
                setCurrency(currnecy);
                _showCurrency = attrArray.getBoolean(R.styleable.EasyMoneyWidgets_show_currency, true);
                _showCommas = attrArray.getBoolean(R.styleable.EasyMoneyWidgets_show_commas, true);
                setTextColor(ContextCompat.getColor(getContext(), R.color.colorHint));
            } finally {
                attrArray.recycle();
            }
        }

        // Add Text Watcher for Decimal formatting
        initTextWatchers();
        this.setOnTouchListener((v, event) -> {
            new Handler().post(() -> {
                Timber.d("getValueString() " + getValueString());
                if (getValueString().equals("0.00")) {
                    setSelection(_currencySymbol.length() + 1);
                }
            });
            if (hasToolTip) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= this.getRight() -
                            this.getCompoundDrawables()[2].getBounds().width()) {
                        toolTipListener.onClickedIcon();
                    }
                }
            }
            return false;
        });
        this.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && getValueString().equals("0.00")) {
                setSelection(_currencySymbol.length() + 1);
            }
        });
    }

    public int getCurrencyLength() {
        return _currencySymbol.length();
    }

    private void initTextWatchers() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                EasyMoneyEditText.this.removeTextChangedListener(this);
                String backupString = charSequence.toString();

                try {
                    String originalString;

                    long longval;

                    originalString = getValueString();
                    longval = (Long.parseLong(originalString));
                    String formattedString = getDecoratedStringFromNumber(longval);

                    //setting text after format to EditText
                    setText(formattedString);
                    setSelection(getText().length());
                    isEmpty = false;
                    if (!hasLimit) {
                        hasLimit = true;
                        setEditTextMaxLength(getContext().getResources().getInteger(R.integer.max_length_amount));
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    setText(backupString);

                    String valStr = getValueString();
                    Timber.d("valStr: " + valStr);
                    if (valStr.equals("")) {
                        isEmpty = true;
                        setText(getDecoratedStringFromNumber(emptyValue));
                        setSelection(getText().length());
                    } else {
                        // Some decimal number
                        if (valStr.contains(".")) {
                            if (hasLimit) {
                                hasLimit = false;
                                setEditTextMaxLength(getContext().getResources().getInteger(R.integer.max_length_amount_without_limit));
                            }
                            if ((valStr.equals("0.000") || valStr.equals("0.0")) && isEmpty) {
                                setText(getDecoratedStringFromNumber(emptyValue));
                                isEmpty = true;
                            } else if (valStr.contains("0.00") && isEmpty) {
                                String val = valStr.replace("0.00", "");
                                if (val.equals("") || val.equals(".")) {
                                    setText(getDecoratedStringFromNumber(emptyValue));
                                    isEmpty = true;
                                } else {
                                    setText(getDecoratedStringFromNumber(Long.parseLong(val)));
                                    isEmpty = false;
                                }
                            } else {
                                if (valStr.indexOf(".") == valStr.length() - 1) {
                                    // decimal has been currently put
                                    String front = getDecoratedStringFromNumber(Long.parseLong(valStr.substring(0, valStr.length() - 1)));
                                    setText(front + ".");
                                } else {
                                    String[] nums = getValueString().split("\\.");
                                    String front = getDecoratedStringFromNumber(Long.parseLong(nums[0]));
                                    if (nums.length == 2 && nums[1].length() > 2) {
                                        setText(front + "." + nums[1].substring(0, 2));
                                    } else {
                                        setText(front + "." + nums[1]);
                                    }
                                }
                                isEmpty = false;
                            }
                        }
                        setSelection(getText().length());
                    }
                } catch (Exception nfe) {

                }
                if (isEmpty) {
                    setTextColor(ContextCompat.getColor(getContext(), R.color.colorHint));
                    setSelection(_currencySymbol.length() + 1);
                } else {
                    setTextColor(ContextCompat.getColor(getContext(), R.color.colorInfo));
                }
                EasyMoneyEditText.this.addTextChangedListener(this);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void updateValue(String text) {
        setText(text);
    }

    private String getDecoratedStringFromNumber(long number) {
        String numberPattern = "#,###,###,###";
        String decoStr;

        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        if (number == emptyValue) {
            return _currencySymbol + " 0.00";
        } else if (_showCommas && !_showCurrency)
            formatter.applyPattern(numberPattern);
        else if (_showCommas && _showCurrency)
            formatter.applyPattern(_currencySymbol + " " + numberPattern);
        else if (!_showCommas && _showCurrency)
            formatter.applyPattern(_currencySymbol + " ");
        else if (!_showCommas && !_showCurrency) {
            decoStr = number + "";
            return decoStr;
        }

        decoStr = formatter.format(number);

        return decoStr;
    }

    private void setShowCurrency(boolean value) {
        _showCurrency = value;
        updateValue(getText().toString());
    }

    /**
     * Get the value of the text without any commas and currency.
     * For example, if the edit text value is $ 1,34,000.60 then this method will return 134000.60
     *
     * @return A string of the raw value in the text field
     */
    public String getValueString() {
        String string = getText().toString();
        if (string.contains("..")) {
            string = string.replace("..", ".");
        }
        if (string.contains(",")) {
            string = string.replace(",", "");
        }
        if (string.contains(" ")) {
            string = string.substring(string.indexOf(" ") + 1);
        } else {
            string = string.replace(_currencySymbol, "");
        }
        return string;
    }

    /**
     * Get the value of the text with formatted commas and currency.
     * For example, if the edit text value is $ 1,34,000.60 then this method will return exactly $ 1,34,000.60
     *
     * @return A string of the text value in the text field
     */
    public String getFormattedString() {
        return getText().toString();
    }

    /**
     * Set the currency symbol for the edit text. (Default is US Dollar $).
     *
     * @param newSymbol the new symbol of currency in string
     */
    public void setCurrency(String newSymbol) {
        _currencySymbol = newSymbol;
        updateValue(getText().toString());
    }

    /**
     * Set the currency symbol for the edit text. (Default is US Dollar $).
     *
     * @param locale the locale of new symbol. (Defaul is Locale.US)
     */
    public void setCurrency(Locale locale) {
        setCurrency(Currency.getInstance(locale).getSymbol());
    }

    /**
     * Set the currency symbol for the edit text. (Default is US Dollar $).
     *
     * @param currency the currency object of new symbol. (Defaul is Locale.US)
     */
    public void setCurrency(Currency currency) {
        setCurrency(currency.getSymbol());
    }

    /**
     * Whether currency is shown in the text or not. (Default is true)
     *
     * @return true if the currency is shown otherwise false.
     */
    public boolean isShowCurrency() {
        return _showCurrency;
    }

    /**
     * Shows the currency in the text. (Default is shown).
     */
    public void showCurrencySymbol() {
        setShowCurrency(true);
    }

    /**
     * Hides the currency in the text. (Default is shown).
     */
    public void hideCurrencySymbol() {
        setShowCurrency(false);
    }

    /**
     * Shows the commas in the text. (Default is shown).
     */
    public void showCommas() {
        _showCommas = true;
        updateValue(getText().toString());
    }

    /**
     * Hides the commas in the text. (Default is shown).
     */
    public void hideCommas() {
        _showCommas = false;
        updateValue(getText().toString());
    }

    /**
     * set max length of the amount
     */
    private void setEditTextMaxLength(int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(_currencySymbol.length() + 1 + length);
        this.setFilters(filterArray);
    }

    public interface ToolTipListener {
        void onClickedIcon();
    }

}