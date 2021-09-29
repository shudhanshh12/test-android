package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models

import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.user_migration.contract.models.AmountBox

sealed class AddBillModel {

    object AddBill : AddBillModel()

    data class PictureView(val image: CapturedImage) : AddBillModel()

    data class RoboflowPicture(
        val image: CapturedImage,
        val width: Int,
        val height: Int,
        val amountBox: AmountBox,
    ) : AddBillModel()
}
