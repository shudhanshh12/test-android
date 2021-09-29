package `in`.okcredit.sales_ui

import `in`.okcredit.shared.base.BaseScreen
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class SalesActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {
        const val SCREEN = "SCREEN"
        private const val ADD_SALE_DEEP_LINK = "sales://add/sale"
        private const val ADD_BILL_DEEP_LINK = "sales://add/bill"

        fun getSalesScreenIntent(context: Context): Intent {
            return Intent(context, SalesActivity::class.java)
        }

        fun getAddSaleScreenIntent(context: Context): Intent {
            val intent = Intent(context, SalesActivity::class.java)
            intent.putExtra(SCREEN, ADD_SALE_DEEP_LINK)
            return intent
        }

        fun getAddBillScreenIntent(context: Context): Intent {
            val intent = Intent(context, SalesActivity::class.java)
            intent.putExtra(SCREEN, ADD_BILL_DEEP_LINK)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales)
    }

    override fun onStart() {
        super.onStart()
        intent.extras?.let {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.sales_nav_host) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(Uri.parse(it.getString(SCREEN)))
        }
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.sales_nav_host)
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
