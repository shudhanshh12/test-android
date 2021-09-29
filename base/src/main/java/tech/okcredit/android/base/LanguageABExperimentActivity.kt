package tech.okcredit.android.base

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.BaseContextWrappingDelegate
import tech.okcredit.android.base.activity.OkcActivity

open class LanguageABExperimentActivity : OkcActivity() {

    private var baseContextWrappingDelegate: AppCompatDelegate? = null

    override fun getDelegate() =
        baseContextWrappingDelegate ?: BaseContextWrappingDelegate(super.getDelegate()).apply {
            baseContextWrappingDelegate = this
        }
}
