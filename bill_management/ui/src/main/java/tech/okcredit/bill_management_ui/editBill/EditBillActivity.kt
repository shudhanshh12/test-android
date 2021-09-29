package tech.okcredit.bill_management_ui.editBill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bills.BILL_INTENT_EXTRAS

class EditBillActivity : OkcActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bill)
    }

    companion object {
        fun createIntent(context: Context, position: Int, billId: String): Intent {
            return Intent(context, EditBillActivity::class.java)
                .putExtra(BILL_INTENT_EXTRAS.BILL_ID, billId)
                .putExtra(BILL_INTENT_EXTRAS.BILL_POSITION, position)
        }
    }
}
