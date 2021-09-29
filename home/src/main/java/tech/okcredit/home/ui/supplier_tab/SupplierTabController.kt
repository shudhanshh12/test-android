package tech.okcredit.home.ui.supplier_tab

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.BroadcastHelper
import `in`.okcredit.shared.performance.PerformanceTracker
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.home.views.homeSupplierView
import tech.okcredit.home.ui.home.views.nativePlayerView
import javax.inject.Inject

@Reusable
class SupplierTabController @Inject constructor(
    private val fragment: SupplierTabFragment,
    private val tracker: Tracker,
    private val performanceTracker: Lazy<PerformanceTracker>,
    private val broadcastHelper: BroadcastHelper
) : AsyncEpoxyController() {

    private var state = SupplierTabContract.State(sourceScreen = "supplier_tab")

    fun setState(
        state: SupplierTabContract.State,
    ) {
        this.state = state
        cancelPendingModelBuild()
        requestModelBuild()
    }

    override fun buildModels() {
        state.let {
            it.supplier?.suppliers?.map {
                homeSupplierView {
                    id("supplier${it.id}")
                    supplierDetails(
                        HomeSupplierView.HomeSupplierData(
                            it.id,
                            it,
                            if (it.lastActivityTime != null) if (state.unSyncSupplierIds.contains(it.id)) HomeSupplierView.SYNC_PENDING
                            else HomeSupplierView.SYNC_COMPLETED else HomeSupplierView.SYNC_NO_TXN,
                        )
                    )
                    performanceTracker(performanceTracker.get())
                    tracker(tracker)
                    listener(fragment)
                }
            }

            if (state.canShowSupplierTabVideo) {
                nativePlayerView {
                    id("native_player")
                    state(state.nativeVideoState)
                    broadcastHelper(broadcastHelper)
                    listener(fragment)
                }
            }
        }
    }
}
