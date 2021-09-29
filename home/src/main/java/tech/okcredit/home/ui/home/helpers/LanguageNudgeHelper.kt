package tech.okcredit.home.ui.home.helpers

import `in`.okcredit.home.ILanguageNudgeHelper
import android.content.Context
import dagger.Lazy
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_BENGALI
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_ENGLISH
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_GUJARATI
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_HINDI
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_HINGLISH
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_KANNADA
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_MALAYALAM
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_MARATHI
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_PUNJABI
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_TAMIL
import tech.okcredit.android.base.language.LocaleManager.Companion.LANGUAGE_TELUGU
import tech.okcredit.home.R
import javax.inject.Inject

@Suppress("HardCodedStringLiteral", "SpellCheckingInspection")
class LanguageNudgeHelper @Inject constructor(private val context: Lazy<Context>) : ILanguageNudgeHelper {

    // output :
    // Do you want to change language to
    // Hindi (हिंदी)?  Yes, change now
    override fun getEnglishWithAutoLangText(lang: String): String {
        return context.get().resources.getString(R.string.change_language_to_eng, mappedLang().getValue(lang))
    }

    // output :
    // क्या आप भाषा को हिंदी में बदलना चाहते हैं| हां, अब बदलो
    override fun getAutoLangText(lang: String): String {
        return mappedString().getValue(lang)
    }

    override fun mappedLang() = mapOf(
        LANGUAGE_ENGLISH to "English (English)?",
        LANGUAGE_HINDI to "Hindi (हिंदी)?",
        LANGUAGE_PUNJABI to "Punjabi (ਪੰਜਾਬੀ)?",
        LANGUAGE_MALAYALAM to "Malayalam (മലയാളം)?",
        LANGUAGE_HINGLISH to "Hinglish (Hindi in English)?",
        LANGUAGE_GUJARATI to "Gujrati (ગુજરાતી)?",
        LANGUAGE_MARATHI to "Marathi (मराठी)?",
        LANGUAGE_TAMIL to "Tamil (தமிழ்)?",
        LANGUAGE_TELUGU to "Telugu (తెలుగు)?",
        LANGUAGE_BENGALI to "Bangla (বাংলা)?",
        LANGUAGE_KANNADA to "Kannada (ಕನ್ನಡ)?"
    )

    override fun mappedString() = mapOf(
        LANGUAGE_ENGLISH to "Do you want to change the language to English? <action><b><font color=#0574e3><u>Yes, Change now.</u></font></b></action>",
        LANGUAGE_HINDI to "क्या आप भाषा को हिंदी में बदलना चाहते हैं|  <action><b><font color=#0574e3><u> हां, अब बदलो</u></font></b></action>",
        LANGUAGE_PUNJABI to "ਕੀ ਤੁਸੀਂ ਭਾਸ਼ਾ ਨੂੰ ਪੰਜਾਬੀ ਵਿੱਚ ਬਦਲਣਾ ਚਾਹੁੰਦੇ ਹੋ?  <action><b><font color=#0574e3><u> ਹਾਂ, ਹੁਣੇ ਬਦਲੋ।</u></font></b></action>",
        LANGUAGE_MALAYALAM to "നിങ്ങൾക്ക് ആപ്പ് ഭാഷ മലയാളമാക്കി മാറ്റണമെന്നുണ്ടോ?  <action><b><font color=#0574e3><u> എങ്കിൽ ഉടൻ തന്നെ മാറ്റൂ.</u></font></b></action>",
        LANGUAGE_HINGLISH to "Kya aap bhaasha ko Hinglish me badalna chahte hain? <action><b><font color=#0574e3><u> Haan, Abhi badalein.</u></font></b></action>",
        LANGUAGE_GUJARATI to "શું તમે ભાષા બદલીને ગુજરાતી કરવા માંગો છો?  <action><b><font color=#0574e3><u> હા અત્યારે જ બદલો.</u></font></b></action>",
        LANGUAGE_MARATHI to "आपल्याला भाषा बदलून मराठी करायची आहे का?  <action><b><font color=#0574e3><u> होय, आत्ताच बदला.</u></font></b></action>",
        LANGUAGE_TAMIL to "தமிழ் மொழிக்கு மாற்ற விரும்புகிறீர்களா?  <action><b><font color=#0574e3><u> ஆம், உடனே மாற்று.</u></font></b></action>",
        LANGUAGE_TELUGU to "మీరు మీ భాషను తెలుగులోకి  మార్చాలనుకుంటున్నారా?  <action><b><font color=#0574e3><u> అవును, ఇప్పుడే మార్చండి.</u></font></b></action>",
        LANGUAGE_BENGALI to "আপনি কি ভাষা বাংলাতে বদলাতে চাইছেন? হ্যাঁ,  <action><b><font color=#0574e3><u> এখনই বদলান।</u></font></b></action>",
        LANGUAGE_KANNADA to "ನೀವು ಭಾಷೆಯನ್ನು ಕನ್ನಡಕ್ಕೆ ಬದಲಾಯಿಸಲು ಬಯಸಿರುವಿರಾ?  <action><b><font color=#0574e3><u> ಹೌದು, ಇದೀಗ ಬದಲಾಯಿಸಿ.</u></font></b></action>"
    )
}
