package tech.okcredit.bill_management_ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.AndroidInjection
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.bills.BILL_INTENT_EXTRAS

class BillActivity : BaseLanguageActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill)
    }

    companion object {

        fun getIntent(
            context: Context,
            accountId: String,
            role: String,
            name: String?
        ): Intent {
            return Intent(context, BillActivity::class.java).apply {
                putExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID, accountId)
                putExtra(BILL_INTENT_EXTRAS.ROLE, role)
                putExtra(BILL_INTENT_EXTRAS.ACCOUNT_NAME, name)
            }
        }
    }
}
