package tech.okcredit.android.base

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.BaseContextWrappingDelegate
import tech.okcredit.android.base.language.LocaleManager
import java.util.*

open class BaseLanguageActivity : LanguageABExperimentActivity() {

    private var baseContextWrappingDelegate: AppCompatDelegate? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        setApplicationLanguage(
            LocaleManager.getLanguage(base)
        )
    }

    override fun getDelegate() =
        baseContextWrappingDelegate ?: BaseContextWrappingDelegate(super.getDelegate()).apply {
            baseContextWrappingDelegate = this
        }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun setApplicationLanguage(newLanguage: String) {
        val activityRes = resources
        val activityConf = activityRes.configuration
        val newLocale = Locale(newLanguage)
        activityConf.setLocale(newLocale)
        activityRes.updateConfiguration(activityConf, activityRes.displayMetrics)

        val applicationRes = applicationContext.resources
        val applicationConf = applicationRes.configuration
        applicationConf.setLocale(newLocale)
        applicationRes.updateConfiguration(applicationConf, applicationRes.displayMetrics)
    }
}
