package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller

import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.PictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.addBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.pictureView
import com.airbnb.epoxy.TypedEpoxyController
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Inject

class PictureListController @Inject constructor(
    private val listener: PictureView.Listener,
    private val listenerAddBill: AddBillsView.Listener
) : TypedEpoxyController<List<CapturedImage>>() {

    override fun buildModels(data: List<CapturedImage>?) {
        if (!data.isNullOrEmpty()) {
            addBillsView {
                id("addbill")
                listener(listenerAddBill)
            }

            data.forEach {
                pictureView {
                    id(it.file.path)
                    picture(it)
                    listener(listener)
                }
            }
        } else {
            addBillsView {
                id("addbill")
                listener(listenerAddBill)
            }
        }
    }
}
