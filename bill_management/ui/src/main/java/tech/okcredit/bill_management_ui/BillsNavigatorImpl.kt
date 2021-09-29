package tech.okcredit.bill_management_ui

import android.content.Context
import android.content.Intent
import tech.okcredit.bills.BillsNavigator
import javax.inject.Inject

class BillsNavigatorImpl @Inject constructor() : BillsNavigator {
    override fun getBillActivityIntent(context: Context, accountId: String, role: String, name: String?): Intent {
        return BillActivity.getIntent(
            context = context,
            accountId = accountId,
            role = role,
            name = name
        )
    }
}
