package `in`.okcredit.onboarding.contract.autolang

import androidx.appcompat.app.AppCompatDialogFragment

interface LanguageSelectedListener {
    fun onSelected(language: Language, dialog: AppCompatDialogFragment)

    fun onDismissed()
}
