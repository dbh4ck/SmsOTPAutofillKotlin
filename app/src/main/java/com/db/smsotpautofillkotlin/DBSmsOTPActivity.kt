package com.db.smsotpautofillkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.db.smsotpautofillkotlin.interfaces.EditCodeWatcher
import com.db.smsotpautofillkotlin.interfaces.EditCodeListener
import android.view.WindowManager
import com.db.smsotpautofillkotlin.editable.EditCodeView
import com.db.smsotpautofillkotlin.interfaces.SmsListener
import com.db.smsotpautofillkotlin.receivers.SmsReceiver
import java.util.regex.Pattern


class DBSmsOTPActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dbsms_otp)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        val editCodeView = findViewById(R.id.dbotp) as EditCodeView
        editCodeView.setEditCodeListener(object : EditCodeListener {
            override fun onCodeReady(code: String) {

            }
        })

        editCodeView.setEditCodeWatcher(object : EditCodeWatcher {
            override fun onCodeChanged(code: String) {
                Log.e("CodeWatcher", " changed : " + code)
            }
        })

        editCodeView.requestFocus()

        SmsReceiver.Companion.bindListener(object : SmsListener {
            override fun messageReceived(messageText: String) {

                //From the received text string you may do string operations to get the required OTP
                //It depends on your SMS format
                Log.e("Message", messageText)
                //  Toast.makeText(DbSmsOTPActivity.this,"Message: " + messageText, Toast.LENGTH_LONG).show();

                // If your OTP is six digits number, you may use the below code

                val pattern = Pattern.compile(OTP_REGEX)
                val matcher = pattern.matcher(messageText)
                var otp: String? = null
                while (matcher.find()) {
                    otp = matcher.group()
                }

                // Toast.makeText(DbSmsOTPActivity.this, "OTP: " + otp , Toast.LENGTH_LONG).show();
                assert(otp != null)
                editCodeView.setCode(otp!!)

            }
        })

    }

    companion object {
        val OTP_REGEX: String? = "[0-9]{1,6}"
    }
}
