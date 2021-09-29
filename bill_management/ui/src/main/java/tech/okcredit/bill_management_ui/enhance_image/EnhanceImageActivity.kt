package tech.okcredit.bill_management_ui.enhance_image

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import dagger.android.AndroidInjection
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage

class EnhanceImageActivity : BaseLanguageActivity() {

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
        ) = Intent(context, EnhanceImageActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("flow", flow)
            .putExtra("relation", relation)
            .putExtra("type", type)
            .putExtra("screen", screen)
            .putExtra("mobile", mobile)
            .putExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID, accountId)
            .putExtra("txnId", txnId)
            .putExtra("addedImages", addedImages)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enhance_image)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
