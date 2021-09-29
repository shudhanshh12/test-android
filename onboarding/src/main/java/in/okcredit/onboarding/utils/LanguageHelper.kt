package `in`.okcredit.onboarding.utils

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.data.SelectedLanguage
import dagger.Lazy
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class LanguageHelper @Inject constructor(
    private val localeManager: Lazy<LocaleManager>
) {

    fun getLanguage(): SelectedLanguage {

        return when (localeManager.get().getLanguage()) {
            LocaleManager.LANGUAGE_ENGLISH -> SelectedLanguage(R.drawable.ic_letter_english, "English")
            LocaleManager.LANGUAGE_HINDI -> SelectedLanguage(R.drawable.ic_letter_hindi, "हिंदी")
            LocaleManager.LANGUAGE_HINGLISH -> SelectedLanguage(R.drawable.ic_letter_hinglish, "Hinglish")
            LocaleManager.LANGUAGE_PUNJABI -> SelectedLanguage(R.drawable.ic_letter_punjabi, "ਪੰਜਾਬੀ")
            LocaleManager.LANGUAGE_MALAYALAM -> SelectedLanguage(R.drawable.ic_letter_malayalam, "മലയാളം")
            LocaleManager.LANGUAGE_GUJARATI -> SelectedLanguage(R.drawable.ic_letter_gujarati, "ગુજરાતી")
            LocaleManager.LANGUAGE_TAMIL -> SelectedLanguage(R.drawable.ic_letter_tamil, "தமிழ்")
            LocaleManager.LANGUAGE_TELUGU -> SelectedLanguage(R.drawable.ic_letter_telugu, "తెలుగు")
            LocaleManager.LANGUAGE_MARATHI -> SelectedLanguage(R.drawable.ic_letter_marathi, "मराठी")
            LocaleManager.LANGUAGE_BENGALI -> SelectedLanguage(R.drawable.ic_letter_bangala, "বাংলা")
            else -> SelectedLanguage(R.drawable.ic_letter_kannada, "ಕನ್ನಡ")
        }
    }
}
