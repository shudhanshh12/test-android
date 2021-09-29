package `in`.okcredit.ui._dialog

import `in`.okcredit.R
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import tech.okcredit.android.base.language.LocaleManager

class SmsLanguageDialog {
    interface Listener {
        fun onButtonClicked(lang: String?)
    }

    fun show(activity: Activity, lang: String?, listener: Listener?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        val layoutInflater = activity.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_sms_language, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        if (!activity.isFinishing) {
            alertDialog.show()
        }
        val btnOk = dialogView.findViewById<TextView>(R.id.btn_ok)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btn_cancel)
        val english = dialogView.findViewById<RadioButton>(R.id.english)
        val hindi = dialogView.findViewById<RadioButton>(R.id.hindi)
        val panjabi = dialogView.findViewById<RadioButton>(R.id.panjabi)
        val malayalam = dialogView.findViewById<RadioButton>(R.id.malayalam)
        val hinglish = dialogView.findViewById<RadioButton>(R.id.hinglish)
        val marathi = dialogView.findViewById<RadioButton>(R.id.marathi)
        val tamil = dialogView.findViewById<RadioButton>(R.id.tamil)
        val telugu = dialogView.findViewById<RadioButton>(R.id.telugu)
        val bengali = dialogView.findViewById<RadioButton>(R.id.bengali)
        val gujarati = dialogView.findViewById<RadioButton>(R.id.gujarati)
        val kannada = dialogView.findViewById<RadioButton>(R.id.kannada)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group)
        when (lang) {
            LocaleManager.LANGUAGE_ENGLISH -> english.isChecked = true
            LocaleManager.LANGUAGE_HINDI -> hindi.isChecked = true
            LocaleManager.LANGUAGE_HINGLISH -> hinglish.isChecked = true
            LocaleManager.LANGUAGE_MALAYALAM -> malayalam.isChecked = true
            LocaleManager.LANGUAGE_PUNJABI -> panjabi.isChecked = true
            LocaleManager.LANGUAGE_MARATHI -> marathi.isChecked = true
            LocaleManager.LANGUAGE_TAMIL -> tamil.isChecked = true
            LocaleManager.LANGUAGE_TELUGU -> telugu.isChecked = true
            LocaleManager.LANGUAGE_BENGALI -> bengali.isChecked = true
            LocaleManager.LANGUAGE_GUJARATI -> gujarati.isChecked = true
            LocaleManager.LANGUAGE_KANNADA -> kannada.isChecked = true
        }
        btnOk.setOnClickListener { v: View? ->
            if (listener != null) {
                if (radioGroup.checkedRadioButtonId == english.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_ENGLISH)
                } else if (radioGroup.checkedRadioButtonId == hindi.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_HINDI)
                } else if (radioGroup.checkedRadioButtonId == hinglish.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_HINGLISH)
                } else if (radioGroup.checkedRadioButtonId == panjabi.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_PUNJABI)
                } else if (radioGroup.checkedRadioButtonId == malayalam.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_MALAYALAM)
                } else if (radioGroup.checkedRadioButtonId == marathi.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_MARATHI)
                } else if (radioGroup.checkedRadioButtonId == tamil.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_TAMIL)
                } else if (radioGroup.checkedRadioButtonId == telugu.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_TELUGU)
                } else if (radioGroup.checkedRadioButtonId == bengali.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_BENGALI)
                } else if (radioGroup.checkedRadioButtonId == gujarati.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_GUJARATI)
                } else if (radioGroup.checkedRadioButtonId == kannada.id) {
                    listener.onButtonClicked(LocaleManager.LANGUAGE_KANNADA)
                }
                alertDialog.dismiss()
            }
        }
        btnCancel.setOnClickListener { v: View? -> alertDialog.dismiss() }
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        alertDialog.setCancelable(true)
        return alertDialog
    }
}
