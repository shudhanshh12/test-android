package tech.okcredit.home.ui.add_transaction_home_search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.ActivityHomeSearchBinding
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment.Companion.ARG_ADD_TRANSACTION_SHORTCUT_SOURCE

class AddTxnShortcutSearchActivity : OkcActivity() {

    companion object {
        fun getIntent(context: Context) =
            Intent(context, AddTxnShortcutSearchActivity::class.java)

        fun start(context: Context) {
            val intent = Intent(context, AddTxnShortcutSearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val binding: ActivityHomeSearchBinding by viewLifecycleScoped(ActivityHomeSearchBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val fragment = AddTransactionShortcutSearchFragment.newInstance(
            intent?.getStringExtra(ARG_ADD_TRANSACTION_SHORTCUT_SOURCE)
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_host, fragment)
            .commit()
    }
}
