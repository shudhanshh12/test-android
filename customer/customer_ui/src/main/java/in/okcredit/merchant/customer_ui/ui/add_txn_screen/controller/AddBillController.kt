package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller

import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.PictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.RoboflowPictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.addBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.pictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.roboflowPictureView
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import javax.inject.Inject

class AddBillController @Inject constructor(
    private val listener: Lazy<RoboflowPictureView.Listener>,
    private val listenerAddBill: Lazy<AddBillsView.Listener>,
    private val listenerPictureView: Lazy<PictureView.Listener>
) : TypedEpoxyController<List<AddBillModel>>() {

    override fun buildModels(data: List<AddBillModel>?) {
        data?.forEach {
            when (it) {
                is AddBillModel.AddBill -> addBillsView {
                    id("addBill")
                    listener(listenerAddBill.get())
                }
                is AddBillModel.PictureView -> pictureView {
                    id(it.image.file.path)
                    picture(it.image)
                    listener(listenerPictureView.get())
                }
                is AddBillModel.RoboflowPicture -> roboflowPictureView {
                    id(it.image.file.path)
                    picture(it)
                    listener(listener.get())
                }
            }
        }
    }
}
