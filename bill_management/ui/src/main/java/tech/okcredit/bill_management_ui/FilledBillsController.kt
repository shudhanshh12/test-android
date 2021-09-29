package tech.okcredit.bill_management_ui

import com.airbnb.epoxy.AsyncEpoxyController
import tech.okcredit.android.base.utils.ImageCache

class FilledBillsController constructor(
    private val fragment: BillFragment,
    private val imageCache: ImageCache
) : AsyncEpoxyController() {

    private lateinit var state: BillContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: BillContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        state.map?.let {
            for (i in it.entries) {
                val billLists = i.value.filter { localBill ->
                    localBill.localBillDocList.isNotEmpty()
                }
                if (billLists.isNotEmpty()) {
                    monthNameView {
                        id(i.key)
                        name(i.key)
                    }
                    var l = 0
                    while (l < billLists.size) {
                        if (l + 1 < billLists.size) {
                            // show two images
                            filledBillsView {
                                id(billLists[l].id)
                                items(arrayOf(billLists[l], billLists[l + 1]))
                                listener(fragment)
                                role(state.role!!)
                                lastSeenTime(state.lastSeenTime)
                                imageCache(imageCache)
                            }
                        } else {
                            // show one image
                            filledBillsView {
                                id(billLists[l].id)
                                items(arrayOf(billLists[l]))
                                listener(fragment)
                                role(state.role!!)
                                lastSeenTime(state.lastSeenTime)
                                imageCache(imageCache)
                            }
                        }
                        l += 2
                    }
                }
            }
        }
    }
}
