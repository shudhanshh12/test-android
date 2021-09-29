package tech.okcredit.android.base.language

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import dagger.Lazy
import dagger.Reusable
import java.util.*
import javax.inject.Inject

@Reusable
class LocaleManager @Inject constructor(
    private val context: Lazy<Context>
) {

    companion object {
        private const val KEY_LANGUAGE = "locale.language"
        private const val SHARED_PREF_NAME = "in.okcredit.locale"

        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_HINDI = "hi"
        const val LANGUAGE_PUNJABI = "pa"
        const val LANGUAGE_MALAYALAM = "ml"
        const val LANGUAGE_HINGLISH = "afh"

        const val LANGUAGE_GUJARATI = "gu"
        const val LANGUAGE_MARATHI = "mr"
        const val LANGUAGE_TAMIL = "ta"
        const val LANGUAGE_TELUGU = "te"

        const val LANGUAGE_BENGALI = "bn"
        const val LANGUAGE_KANNADA = "kn"

        internal val SUPPORTED_LANGUAGES: Collection<String> = listOf(
            LANGUAGE_ENGLISH,
            LANGUAGE_HINDI,
            LANGUAGE_PUNJABI,
            LANGUAGE_MALAYALAM,
            LANGUAGE_HINGLISH,
            LANGUAGE_GUJARATI,
            LANGUAGE_MARATHI,
            LANGUAGE_TAMIL,
            LANGUAGE_TELUGU,
            LANGUAGE_BENGALI,
            LANGUAGE_KANNADA
        )

        val englishLocale: Locale
            get() = Locale("en", "IN")

        internal var languageCache: String? = null

        @JvmStatic
        fun setLocale(context: Context): Context {
            val language = getLanguage(context)
            return if (!isSupportedLanguage(language)) {
                context
            } else setNewLocale(context, getLanguage(context))
        }

        @JvmStatic
        fun setNewLocale(context: Context, language: String): Context {
            if (language != languageCache) {
                persistLanguage(context, language)
            }
            return updateResources(context, language)
        }

        @JvmStatic
        @Deprecated(message = "Use dagger instead")
        fun getLanguage(context: Context): String {
            var language = getLanguageFromCacheOrPreferences(context)
            if (language == null) {
                persistLanguage(context, LANGUAGE_ENGLISH)
                language = LANGUAGE_ENGLISH
            }
            return language
        }

        internal fun getLanguageFromCacheOrPreferences(context: Context): String? {
            if (languageCache == null) {
                val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                languageCache = sharedPreferences.getString(KEY_LANGUAGE, null)
            }
            return languageCache
        }

        @JvmStatic
        fun getLanguageForDateFormat(context: Context): String {
            val language = getLanguageFromCacheOrPreferences(context)
            return if (language == null || language == LANGUAGE_HINGLISH) {
                LANGUAGE_ENGLISH
            } else language
        }

        internal fun persistLanguage(context: Context, language: String) {
            val sharedPreferences = context.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.putString(KEY_LANGUAGE, language)
            editor.apply()
            languageCache = language
        }

        internal fun updateResources(context: Context, language: String): Context {
            var context = context
            val locale = Locale(language)
            Locale.setDefault(locale)

            val res = context.resources
            val config = Configuration(res.configuration)
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
            return context
        }

        internal fun isSupportedLanguage(language: String): Boolean {
            return SUPPORTED_LANGUAGES.contains(language)
        }

        fun getDeviceLanguage(): String {
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales.get(0)
            } else {
                Resources.getSystem().configuration.locale
            }
            return locale.language
        }

        fun fixWebViewLocale(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val backedUpLocale = Locale(getLanguage(context))

                val resources = context.resources
                val config = resources.configuration
                if (config.locales.get(0) != backedUpLocale) {
                    Locale.setDefault(backedUpLocale)
                    val newConfig = Configuration(config)
                    newConfig.setLocale(Locale(backedUpLocale.language, backedUpLocale.country))
                    resources.updateConfiguration(newConfig, null)
                }

                val appResources = context.resources
                val appConfig: Configuration = appResources!!.configuration
                if (appConfig.locales.get(0) != backedUpLocale) {
                    Locale.setDefault(backedUpLocale)
                    val newConfig = Configuration(appConfig)
                    newConfig.setLocale(Locale(backedUpLocale.language, backedUpLocale.country))
                    appResources.updateConfiguration(newConfig, null)
                }
            }
        }
    }

    fun getLanguage(): String {
        var language = getLanguageFromCacheOrPreferences(context.get())
        if (language.isNullOrEmpty()) {
            persistLanguage(context.get(), LANGUAGE_ENGLISH)
            language = LANGUAGE_ENGLISH
        }
        return language
    }

    fun isHindiLocale() = LANGUAGE_HINDI == getLanguage()

    fun isMalayalamLocale() = LANGUAGE_MALAYALAM == getLanguage()

    fun isMarathiLocale() = LANGUAGE_MARATHI == getLanguage()

    fun isEnglishLocale() = LANGUAGE_ENGLISH == getLanguage()
}
