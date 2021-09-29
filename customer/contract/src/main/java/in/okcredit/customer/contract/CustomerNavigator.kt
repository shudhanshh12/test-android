package `in`.okcredit.customer.contract

import android.content.Context
import android.content.Intent

interface CustomerNavigator {

    fun goToBulkReminderV2Activity(context: Context)

    fun getAddPaymentWithExpandedQr(context: Context, customerId: String, source: String): Intent

    fun goToAddRelationshipActivity(
        context: Context,
        relationshipType: RelationshipType,
        canShowTutorial: Boolean,
        showManualFlow: Boolean,
        openForResult: Boolean = false,
    ): Intent
}
