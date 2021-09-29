package tech.okcredit.bills

import android.content.Context
import android.content.Intent

interface BillsNavigator {
    fun getBillActivityIntent(
        context: Context,
        accountId: String,
        role: String,
        name: String?
    ): Intent
}
