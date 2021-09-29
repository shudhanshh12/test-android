package `in`.okcredit.onboarding.language.usecase

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class GetLanguages @Inject constructor(
    private val context: Lazy<Context>
) : UseCase<Unit, List<Language>> {

    override fun execute(req: Unit): Observable<Result<List<Language>>> {
        return UseCase.wrapSingle(Single.fromCallable { buildAppLanguageList() })
    }

    suspend fun execute(): List<Language> = withContext(Dispatchers.Default) {
        buildAppLanguageList()
    }

    @VisibleForTesting
    fun buildAppLanguageList(): List<Language> = listOf(
        buildEnglish(),
        buildHindi(),
        buildMarathi(),
        buildHinglish(),
        buildGujarati(),
        buildTamil(),
        buildTelugu(),
        buildPunjabi(),
        buildMalayalam(),
        buildKannada(),
        buildBangala()
    )

    @VisibleForTesting
    fun buildEnglish(): Language {
        return Language(
            LocaleManager.LANGUAGE_ENGLISH,
            languageTitle = "English",
            languageSubTitle = "",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_english_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_english),
            letterDrawable = R.drawable.ic_letter_english
        )
    }

    @VisibleForTesting
    fun buildHindi(): Language {
        return Language(
            LocaleManager.LANGUAGE_HINDI,
            languageTitle = "हिंदी",
            languageSubTitle = "Hindi",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_hindi_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_hindi),
            letterDrawable = R.drawable.ic_letter_hindi
        )
    }

    @VisibleForTesting
    fun buildMarathi(): Language {
        return Language(
            LocaleManager.LANGUAGE_MARATHI,
            languageTitle = "मराठी",
            languageSubTitle = "Marathi",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_marathi_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_marathi),
            letterDrawable = R.drawable.ic_letter_marathi
        )
    }

    @VisibleForTesting
    fun buildHinglish(): Language {
        return Language(
            LocaleManager.LANGUAGE_HINGLISH,
            languageTitle = "Hinglish",
            languageSubTitle = "Hindi in English",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_hinglish_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_hinglish),
            letterDrawable = R.drawable.ic_letter_hinglish
        )
    }

    @VisibleForTesting
    fun buildGujarati(): Language {
        return Language(
            LocaleManager.LANGUAGE_GUJARATI,
            languageTitle = "ગુજરાતી",
            languageSubTitle = "Gujrati",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_gujrati_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_gujrati),
            letterDrawable = R.drawable.ic_letter_gujarati
        )
    }

    @VisibleForTesting
    fun buildTamil(): Language {
        return Language(
            LocaleManager.LANGUAGE_TAMIL,
            languageTitle = "தமிழ்",
            languageSubTitle = "Tamil",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_tamil_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_tamil),
            letterDrawable = R.drawable.ic_letter_tamil
        )
    }

    @VisibleForTesting
    fun buildTelugu(): Language {
        return Language(
            LocaleManager.LANGUAGE_TELUGU,
            languageTitle = "తెలుగు",
            languageSubTitle = "Telugu",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_telegu_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_telegu),
            letterDrawable = R.drawable.ic_letter_telugu
        )
    }

    @VisibleForTesting
    fun buildPunjabi(): Language {
        return Language(
            LocaleManager.LANGUAGE_PUNJABI,
            languageTitle = "ਪੰਜਾਬੀ",
            languageSubTitle = "Punjabi",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_punjabi_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_punjabi),
            letterDrawable = R.drawable.ic_letter_punjabi
        )
    }

    @VisibleForTesting
    fun buildMalayalam(): Language {
        return Language(
            LocaleManager.LANGUAGE_MALAYALAM,
            languageTitle = "മലയാളം",
            languageSubTitle = "Malayalam",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_malayalam_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_malayalam),
            letterDrawable = R.drawable.ic_letter_malayalam
        )
    }

    @VisibleForTesting
    fun buildKannada(): Language {
        return Language(
            LocaleManager.LANGUAGE_KANNADA,
            languageTitle = "ಕನ್ನಡ",
            languageSubTitle = "Kannada",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_kannada_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_kannada),
            letterDrawable = R.drawable.ic_letter_kannada
        )
    }

    @VisibleForTesting
    fun buildBangala(): Language {
        return Language(
            LocaleManager.LANGUAGE_BENGALI,
            languageTitle = "বাংলা",
            languageSubTitle = "Bangla",
            backgroundColor = ContextCompat.getColor(context.get(), R.color.lang_bangla_light),
            fontColor = ContextCompat.getColor(context.get(), R.color.lang_bangla),
            letterDrawable = R.drawable.ic_letter_bangala
        )
    }
}
