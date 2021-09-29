package `in`.okcredit.web_features.cash_counter

import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.web_features.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class CashCounterActivity : AppCompatActivity(), HasAndroidInjector {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_cash_counter)
    }

    /****************************************************************
     * Dependency Injection
     */

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onBackPressed() {
        val fragment = supportFragmentManager.fragments[0] as Fragment
        if (fragment is BaseScreen<*>) {
            if (fragment.onBackPressed().not()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
