package tech.okcredit.home.ui.settings.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.R
import javax.inject.Inject

class ActiveLanguage @Inject constructor(val localeManager: Lazy<LocaleManager>) {
    fun execute(): Observable<Int> {
        val languagePref = if (localeManager.get().getLanguage().isEmpty().not()) localeManager.get()
            .getLanguage() else LocaleManager.LANGUAGE_ENGLISH
        val activeLanguage = when (languagePref) {
            LocaleManager.LANGUAGE_ENGLISH -> R.string.language_english
            LocaleManager.LANGUAGE_HINDI -> R.string.language_hindi
            LocaleManager.LANGUAGE_PUNJABI -> R.string.language_punjabi
            LocaleManager.LANGUAGE_MALAYALAM -> R.string.language_malayalam
            LocaleManager.LANGUAGE_HINGLISH -> R.string.hinglish
            LocaleManager.LANGUAGE_GUJARATI -> R.string.language_gujarati
            LocaleManager.LANGUAGE_MARATHI -> R.string.language_marathi
            LocaleManager.LANGUAGE_KANNADA -> R.string.language_kannada
            LocaleManager.LANGUAGE_TAMIL -> R.string.language_tamil
            LocaleManager.LANGUAGE_TELUGU -> R.string.language_telugu
            LocaleManager.LANGUAGE_BENGALI -> R.string.language_bengali
            else -> R.string.language_english
        }
        return Observable.just(activeLanguage)
    }
}
