package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection_ui.BuildConfig
import `in`.okcredit.collection_ui.ui.passbook.payments.views.onlinePaymentsView
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class OnlinePaymentsController @Inject
constructor(private val fragment: OnlinePaymentsFragment) : AsyncEpoxyController() {
    private lateinit var state: OnlinePaymentsContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: OnlinePaymentsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        state.filteredList.map {
            onlinePaymentsView {
                id(it.collectionOnlinePayment.id)
                onlineCollection(it)
                listener(fragment)
            }
        }
    }
}
