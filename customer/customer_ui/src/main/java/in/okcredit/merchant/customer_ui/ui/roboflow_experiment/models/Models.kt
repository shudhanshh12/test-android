package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.models

import tech.okcredit.user_migration.contract.models.AmountBox

sealed class RoboflowState {
    object ShowAddBillToolTip : RoboflowState()

    object RoboflowFetchInProgress : RoboflowState()

    data class RoboflowFetchSuccess(
        val width: Int,
        val height: Int,
        val amountBox: AmountBox,
    ) : RoboflowState()

    object RoboflowFetchFailed : RoboflowState()

    object InternetNotAvailable : RoboflowState()

    object EnterAmountManuallyCancelUploadReceipt : RoboflowState()

    object EditAmount : RoboflowState()

    object MultipleReceiptAreAdded : RoboflowState()
}
