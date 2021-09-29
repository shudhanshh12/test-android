package `in`.okcredit.merchant.customer_ui

import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivity
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Activity
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentActivity
import android.content.Context
import android.content.Intent
import javax.inject.Inject

class CustomerNavigatorImpl @Inject constructor() : CustomerNavigator {

    override fun goToBulkReminderV2Activity(context: Context) {
        BulkReminderV2Activity.start(context)
    }

    override fun getAddPaymentWithExpandedQr(context: Context, customerId: String, source: String): Intent {
        return AddCustomerPaymentActivity.getIntent(
            context = context,
            customerId = customerId,
            source = source,
            expandedQr = true,
        )
    }

    override fun goToAddRelationshipActivity(
        context: Context,
        relationshipType: RelationshipType,
        canShowTutorial: Boolean,
        showManualFlow: Boolean,
        openForResult: Boolean
    ): Intent {
        return AddRelationshipActivity.getIntent(
            context,
            relationshipType,
            canShowTutorial,
            showManualFlow,
            openForResult
        )
    }
}
