package tech.okcredit.bill_management_ui.selected_bills

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import dagger.android.AndroidInjection
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage

class SelectedBillActivity : BaseLanguageActivity() {

    companion object {
        fun createIntent(
            context: Context,
            flow: String?,
            relation: String?,
            type: String?,
            screen: String?,
            mobile: String?,
            accountId: String?,
            txnId: String? = null,
            addedImages: ArrayList<CapturedImage>? = null
        ): Intent {
            val intent = Intent(context, SelectedBillActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("flow", flow)
            intent.putExtra("relation", relation)
            intent.putExtra("type", type)
            intent.putExtra("screen", screen)
            intent.putExtra("mobile", mobile)
            intent.putExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID, accountId)
            intent.putExtra("txnId", txnId)
            intent.putExtra("addedImages", addedImages)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_bill)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
