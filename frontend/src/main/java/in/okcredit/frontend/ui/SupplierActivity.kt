package `in`.okcredit.frontend.ui

import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.supplier.SupplierFragment
import `in`.okcredit.shared.base.BaseScreen
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.BaseContextWrappingDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.updateLanguage
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.ui.activity.HomeActivity
import javax.inject.Inject

class SupplierActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {

        const val SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE = 42342

        const val ADD_SUPPLIER_TXN_SCREEN = 21
        const val SUPPLIER_SCREEN = 22
        const val HOME_ACTIVITY = 0

        private const val ARG_SUPPLIER_ID = "supplier_id"
        private const val ARG_SCREEN = "starting_screen"
        private const val NAME = "name"
        private const val REACTIVATE = "reactivate"

        @JvmStatic
        fun startingIntentForSupplierScreen(context: Context, supplierId: String): Intent {
            val intent = Intent(context, SupplierActivity::class.java)
            intent.putExtra(ARG_SUPPLIER_ID, supplierId)
            intent.putExtra(ARG_SCREEN, SUPPLIER_SCREEN)
            return intent
        }

        @JvmStatic
        fun startingIntentForSupplierPaymentScreen(
            context: Context,
            supplierId: String,
            redirectToPaymentScreen: Boolean
        ): Intent {
            val intent = Intent(context, SupplierActivity::class.java)
            intent.putExtra(ARG_SUPPLIER_ID, supplierId)
            intent.putExtra(SupplierFragment.ARG_SCREEN_REDIRECT_TO_PAYMENT, redirectToPaymentScreen)
            intent.putExtra(ARG_SCREEN, SUPPLIER_SCREEN)
            return intent
        }

        @JvmStatic
        fun startingSupplierScreenForReactivation(
            context: Context,
            supplierId: String,
            supplierName: String? = null
        ): Intent {
            val intent = Intent(context, SupplierActivity::class.java)
            intent.putExtra(ARG_SUPPLIER_ID, supplierId)
            intent.putExtra(NAME, supplierName)
            intent.putExtra(REACTIVATE, true)
            intent.putExtra(ARG_SCREEN, SUPPLIER_SCREEN)
            return intent
        }

        @JvmStatic
        fun startingSupplierIntent(context: Context, supplierId: String): Intent {
            val intent = Intent(context, SupplierActivity::class.java)
            intent.putExtra(ARG_SUPPLIER_ID, supplierId)
            intent.putExtra(ARG_SCREEN, SUPPLIER_SCREEN)
            return intent
        }

        @JvmStatic
        fun startingAddTxnSupplierIntent(context: Context, supplierId: String): Intent {
            val intent = Intent(context, SupplierActivity::class.java)
            intent.putExtra(ARG_SUPPLIER_ID, supplierId)
            intent.putExtra(ARG_SCREEN, ADD_SUPPLIER_TXN_SCREEN)
            return intent
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private var baseContextWrappingDelegate: AppCompatDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        lifecycleScope.launch { startFragment() }
    }

    private fun startFragment() {
        var finalHost: NavHostFragment = NavHostFragment.create(R.navigation.customer_flow_v2)
        when (intent.getIntExtra(ARG_SCREEN, HOME_ACTIVITY)) {
            HOME_ACTIVITY -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
                return
            }
            SUPPLIER_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.supplier_flow)
            }
            ADD_SUPPLIER_TXN_SCREEN -> {
                finalHost = NavHostFragment.create(R.navigation.add_supplier_credit_flow)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, finalHost)
            .setPrimaryNavigationFragment(finalHost)
            .commit()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        updateLanguage(LocaleManager.getLanguage(base))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE) {
            val finalHost: NavHostFragment = NavHostFragment.create(R.navigation.supplier_flow)
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, finalHost)
                .setPrimaryNavigationFragment(finalHost)
                .commit()
            return
        }
    }

    override fun getDelegate() =
        baseContextWrappingDelegate ?: BaseContextWrappingDelegate(super.getDelegate()).apply {
            baseContextWrappingDelegate = this
        }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_content)
        if (navHostFragment?.childFragmentManager != null) {
            val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
            if (fr is BaseScreen<*>) {
                if (!fr.onBackPressed()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }
}
