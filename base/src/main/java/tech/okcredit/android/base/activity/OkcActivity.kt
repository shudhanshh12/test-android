package tech.okcredit.android.base.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.BaseContextWrappingDelegate
import androidx.fragment.app.Fragment
import dagger.Lazy
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import tech.okcredit.android.base.extensions.updateLanguage
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

abstract class OkcActivity(private val shouldCallDelegate: Boolean = true) : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var fragmentInjector: Lazy<DispatchingAndroidInjector<Fragment>>

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private var baseContextWrappingDelegate: AppCompatDelegate? = null

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun getDelegate(): AppCompatDelegate {
        if (shouldCallDelegate) {
            return baseContextWrappingDelegate ?: BaseContextWrappingDelegate(super.getDelegate()).apply {
                baseContextWrappingDelegate = this
            }
        }
        return super.getDelegate()
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        updateLanguage(LocaleManager.getLanguage(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }
}
