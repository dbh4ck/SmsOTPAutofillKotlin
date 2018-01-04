package com.db.smsotpautofillkotlin.editable

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.text.*
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.db.smsotpautofillkotlin.R
import com.db.smsotpautofillkotlin.interfaces.EditCodeListener
import android.text.Editable
import com.db.smsotpautofillkotlin.interfaces.EditCodeWatcher

/**
 * Created by DB on 04-01-2018.
 */
class EditCodeView: View, View.OnClickListener, View.OnFocusChangeListener {

    private val textWatcher = CodeTextWatcher()
    private var inputmethodmanager: InputMethodManager? = null
    private var editCodeInputConnection: EditCodeInputConnection? = null
    private var editCodeListener: EditCodeListener? = null
    private var editCodeWatcher: EditCodeWatcher? = null
    private var editable: Editable? = null

    private var textPaint: Paint? = null
    private var underlinePaint: Paint? = null
    private var cursorPaint: Paint? = null

    private var textSize: Float = 0.toFloat()
    private var textPosY: Float = 0.toFloat()
    private var textColor: Int = 0
    private var sectionWidth: Float = 0.toFloat()
    private var codeLength: Int = 0
    private var symbolWidth: Float = 0.toFloat()
    private var symbolMaskedWidth: Float = 0.toFloat()
    private var underlineHorizontalPadding: Float = 0.toFloat()
    private var underlineReductionScale: Float = 0.toFloat()
    private var underlineStrokeWidth: Float = 0.toFloat()
    private var underlineBaseColor: Int = 0
    private var underlineSelectedColor: Int = 0
    private var underlineFilledColor: Int = 0
    private var underlineCursorColor: Int = 0
    private var underlinePosY: Float = 0.toFloat()
    private var fontStyle: Int = 0
    private var cursorEnabled: Boolean = false
    private var codeHiddenMode: Boolean = false
    private var _isSelected: Boolean = false
    private var codeHiddenMask: String? = null
    private val textBounds = Rect()

    constructor(context: Context): this(context, null){
    }

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0){

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        init(context, attrs)
    }

    private val cursorAnimation = object : Runnable {
        override fun run() {
            val color = if (cursorPaint!!.getColor() === underlineSelectedColor)
                underlineCursorColor
            else
                underlineSelectedColor
            cursorPaint!!.setColor(color)
            invalidate()
            postDelayed(this, 500)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        initDefaultAttrs(context)
        initCustomAttrs(context, attrs)
        initPaints()
        initViewsOptions(context)

        if (isInEditMode) {
            editModePreview()
        }
    }

    private fun initDefaultAttrs(context: Context) {
        val resources = context.resources

        underlineReductionScale = DEFAULT_REDUCTION_SCALE!!
        underlineStrokeWidth = resources.getDimension(R.dimen.underline_stroke_width)
        underlineBaseColor = ContextCompat.getColor(context, R.color.underline_base_color)
        underlineFilledColor = ContextCompat.getColor(context, R.color.underline_filled_color)
        underlineCursorColor = ContextCompat.getColor(context, R.color.underline_cursor_color)
        underlineSelectedColor = ContextCompat.getColor(context, R.color.underline_selected_color)
        textSize = resources.getDimension(R.dimen.code_text_size)
        textColor = ContextCompat.getColor(context, R.color.text_main_color)
        codeLength = DEFAULT_CODE_LENGTH!!
        codeHiddenMask = DEFAULT_CODE_MASK
    }

    private fun initCustomAttrs(context: Context, attributeSet: AttributeSet?) {
        if (attributeSet == null) return

        val attributes = context.obtainStyledAttributes(
                attributeSet, R.styleable.EditCodeView)

        underlineStrokeWidth = attributes.getDimension(
                R.styleable.EditCodeView_underlineStroke, underlineStrokeWidth)

        underlineReductionScale = attributes.getFloat(
                R.styleable.EditCodeView_underlineReductionScale, underlineReductionScale)

        underlineBaseColor = attributes.getColor(
                R.styleable.EditCodeView_underlineBaseColor, underlineBaseColor)

        underlineSelectedColor = attributes.getColor(
                R.styleable.EditCodeView_underlineSelectedColor, underlineSelectedColor)

        underlineFilledColor = attributes.getColor(
                R.styleable.EditCodeView_underlineFilledColor, underlineFilledColor)

        underlineCursorColor = attributes.getColor(
                R.styleable.EditCodeView_underlineCursorColor, underlineCursorColor)

        cursorEnabled = attributes.getBoolean(
                R.styleable.EditCodeView_underlineCursorEnabled, cursorEnabled)

        textSize = attributes.getDimension(
                R.styleable.EditCodeView_textSize, textSize)

        textColor = attributes.getColor(
                R.styleable.EditCodeView_textColor, textColor)

        fontStyle = attributes.getInt(
                R.styleable.EditCodeView_fontStyle, fontStyle)

        codeLength = attributes.getInt(
                R.styleable.EditCodeView_codeLength, DEFAULT_CODE_LENGTH!!)

        codeHiddenMode = attributes.getBoolean(
                R.styleable.EditCodeView_codeHiddenMode, codeHiddenMode)

        val mask = attributes.getString(R.styleable.EditCodeView_codeHiddenMask)
        if (mask != null && mask.length > 0) {
            codeHiddenMask = mask.substring(0, 1)
        }

        attributes.recycle()
    }

    private fun editModePreview() {
        for (i in 0 until codeLength) {
            if (codeHiddenMode) {
                editable!!.append(codeHiddenMask)
            } else {
                editable!!.append(DEFAULT_CODE_SYMBOL)
            }
        }
    }


    @SuppressLint("WrongConstant")
    private fun initPaints() {
        textPaint = Paint()
        textPaint!!.setColor(textColor)
        textPaint!!.setTextSize(textSize)
        textPaint!!.setTypeface(Typeface.create(Typeface.DEFAULT, fontStyle))
        textPaint!!.setAntiAlias(true)

        underlinePaint = Paint()
        underlinePaint!!.setColor(underlineBaseColor)
        underlinePaint!!.setStrokeWidth(underlineStrokeWidth)

        cursorPaint = Paint()
        cursorPaint!!.setColor(underlineBaseColor)
        cursorPaint!!.setStrokeWidth(underlineStrokeWidth)
    }


    private fun initViewsOptions(context: Context) {
        setOnClickListener(this)
        isFocusable = true
        isFocusableInTouchMode = true
        onFocusChangeListener = this

        inputmethodmanager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        editable = Editable.Factory.getInstance().newEditable("")
        editable!!.setSpan(textWatcher, 0, editable!!.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        Selection.setSelection(editable, 0)

        editCodeInputConnection = EditCodeInputConnection(this, true, codeLength)


    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureSizes(w, h)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        drawUnderline(canvas)
        drawText(canvas)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        _isSelected = hasFocus
        if (hasFocus) {
            if (cursorEnabled) {
                post(cursorAnimation)
            }
            showKeyboard()
        } else {
            if (cursorEnabled) {
                removeCallbacks(cursorAnimation)
            }
            hideKeyboard()
        }
    }

    private fun drawText(canvas: Canvas) {
        if (codeHiddenMode) {
            val symbol = charArrayOf(codeHiddenMask!!.get(0))
            for (i in 0 until editable!!.length) {
                val textPosX = sectionWidth * i + sectionWidth / 2 - symbolMaskedWidth / 2
                canvas.drawText(symbol, 0, 1, textPosX, textPosY, textPaint)
            }
        } else {
            for (i in 0 until editable!!.length) {
                val symbol = charArrayOf(editable!!.get(i))
                val textPosX = sectionWidth * i + sectionWidth / 2 - symbolWidth / 2
                canvas.drawText(symbol, 0, 1, textPosX, textPosY, textPaint)
            }
        }
    }

    private fun drawUnderline(canvas: Canvas) {
        for (i in 0 until codeLength) {
            val startPosX = sectionWidth * i + underlineHorizontalPadding
            val endPosX = startPosX + sectionWidth - underlineHorizontalPadding * 2

            if (cursorEnabled && _isSelected && editable!!.length == i) {
                canvas.drawLine(startPosX, underlinePosY, endPosX, underlinePosY, cursorPaint)
            } else {
                if (editable!!.length <= i && _isSelected) {
                    underlinePaint!!.setColor(underlineSelectedColor)
                } else if (editable!!.length <= i && !_isSelected) {
                    underlinePaint!!.setColor(underlineBaseColor)
                } else {
                    underlinePaint!!.setColor(underlineFilledColor)
                }
                canvas.drawLine(startPosX, underlinePosY, endPosX, underlinePosY, underlinePaint)
            }
        }
    }

    private fun measureSizes(viewWidth: Int, viewHeight: Int) {
        if (underlineReductionScale > 1) underlineReductionScale = 1f
        if (underlineReductionScale < 0) underlineReductionScale = 0f

        if (codeLength <= 0) {
            throw IllegalArgumentException("Code length must be over than zero")
        }

        symbolWidth = textPaint!!.measureText(DEFAULT_CODE_SYMBOL)
        symbolMaskedWidth = textPaint!!.measureText(codeHiddenMask)
        textPaint!!.getTextBounds(DEFAULT_CODE_SYMBOL, 0, 1, textBounds)
        sectionWidth = (viewWidth / codeLength).toFloat()
        underlinePosY = (viewHeight - paddingBottom).toFloat()
        underlineHorizontalPadding = sectionWidth * underlineReductionScale / 2
        textPosY = (viewHeight / 2 + textBounds.height() / 2).toFloat()
    }

    private fun measureHeight(measureSpec: Int): Int {
        val size = (paddingBottom.toFloat()
                + paddingTop.toFloat()
                + textBounds.height().toFloat()
                + textSize
                + underlineStrokeWidth).toInt()
        return View.resolveSizeAndState(size, measureSpec, 0)
    }

    private fun measureWidth(measureSpec: Int): Int {
        val size = ((paddingLeft.toFloat() + paddingRight.toFloat() + textSize) * codeLength.toFloat() * 2f).toInt()
        return View.resolveSizeAndState(size, measureSpec, 0)
    }

    fun setEditCodeListener(EditCodeListener: EditCodeListener) {
        this.editCodeListener = EditCodeListener
    }

    fun setCode(code: String) {
        var code = code
        code = code.replace(DEFAULT_REGEX.toRegex(), "")
        editCodeInputConnection!!.setComposingText(code, 1)
        editCodeInputConnection!!.finishComposingText()
    }

    fun clearCode() {
        editCodeInputConnection!!.setComposingRegion(0, codeLength)
        editCodeInputConnection!!.setComposingText("", 0)
        editCodeInputConnection!!.finishComposingText()
    }

    fun getCode(): String {
        return editable.toString()
    }

    fun setReductionScale(scale: Float) {
        var scale = scale
        if (scale > 1) scale = 1f
        if (scale < 0) scale = 0f

        underlineReductionScale = scale
        invalidate()
    }

    fun setCodeHiddenMode(hiddenMode: Boolean) {
        codeHiddenMode = hiddenMode
        invalidate()
    }

    fun setUnderlineBaseColor(@ColorInt colorId: Int) {
        underlineBaseColor = colorId
        invalidate()
    }

    fun setUnderlineFilledColor(@ColorInt colorId: Int) {
        underlineFilledColor = colorId
        invalidate()
    }

    fun setUnderlineSelectedColor(@ColorInt colorId: Int) {
        underlineSelectedColor = colorId
        invalidate()
    }

    fun setUnderlineCursorColor(@ColorInt colorId: Int) {
        underlineCursorColor = colorId
        invalidate()
    }

    fun setTextColor(@ColorInt colorId: Int) {
        textColor = colorId
        invalidate()
    }

    fun setUnderlineStrokeWidth(underlineStrokeWidth: Float) {
        this.underlineStrokeWidth = underlineStrokeWidth
        invalidate()
    }

    fun setCodeLength(length: Int) {
        codeLength = length
        editCodeInputConnection = EditCodeInputConnection(this, true, codeLength)
        editable!!.clear()
        inputmethodmanager!!.restartInput(this)
        invalidate()
    }

    fun setEditCodeWatcher(editCodeWatcher: EditCodeWatcher) {
        this.editCodeWatcher = editCodeWatcher
    }

    fun getCodeLength(): Int {
        return codeLength
    }

    fun showKeyboard() {
        inputmethodmanager!!.showSoftInput(this, 0)
    }

    fun hideKeyboard() {
        inputmethodmanager!!.hideSoftInputFromWindow(
                windowToken,
                InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }

    override fun onClick(v: View) {
        showKeyboard()
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {

        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        outAttrs.initialSelStart = 0

        return this.editCodeInputConnection!!
    }

    override fun setSelected(selected: Boolean) {
        _isSelected = selected
        invalidate()
    }

    fun getEditable(): Editable {
        return editable!!
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    private inner class CodeTextWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            invalidate()
            if (editCodeWatcher != null) {
                editCodeWatcher!!.onCodeChanged(s.toString())
            }
            if (editable!!.length == codeLength) {
                if (editCodeListener != null) {
                    editCodeListener!!.onCodeReady(editable.toString())
                }
            }
        }
    }


    companion object {
        private val DEFAULT_CODE_LENGTH: Int? = 4
        private val DEFAULT_CODE_MASK = "*"
        private val DEFAULT_CODE_SYMBOL = "0"
        private val DEFAULT_REGEX = "[^0-9]"
        private val DEFAULT_REDUCTION_SCALE: Float? = 0.5f
    }
}