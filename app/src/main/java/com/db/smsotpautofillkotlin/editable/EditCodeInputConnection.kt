package com.db.smsotpautofillkotlin.editable

import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.text.Editable
import android.view.KeyEvent

/**
 * Created by DB on 03-01-2018.
 */
class EditCodeInputConnection(targetView: View, fullEditor: Boolean, textLength: Int): BaseInputConnection(targetView, fullEditor) {

    private var _editable: Editable? = null
    private var textLength: Int = 0

    init {
        val view = targetView as EditCodeView
        this.textLength = textLength
        this._editable = view.getEditable()
    }

    override fun getEditable(): Editable {
        return this._editable!!
    }

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.getAction() === KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9) {
                val c = event.getKeyCharacterMap().getNumber(event.getKeyCode())
                commitText(c.toString(), 1)
            } else if (event.getKeyCode() === KeyEvent.KEYCODE_DEL) {
                deleteSurroundingText(1, 0)
            }
        }
        return super.sendKeyEvent(event)
    }

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        return _editable!!.length + text.length <= textLength && super.commitText(text.subSequence(0, 1), newCursorPosition)
    }

    override fun setComposingText(text: CharSequence, newCursorPosition: Int): Boolean {
        var text = text
        if (text.length > textLength) {
            text = text.subSequence(0, textLength)
        }
        return super.setComposingText(text, newCursorPosition)
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        return super.setComposingRegion(start, end)
    }

    override fun finishComposingText(): Boolean {
        return super.finishComposingText()
    }
}