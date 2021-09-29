package tech.okcredit.home.ui.home

import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Reusable
import tech.okcredit.home.BuildConfig
import tech.okcredit.home.ui.home.views.homeScreenHorizontalLoaderView
import tech.okcredit.home.ui.home.views.homeScreenInAppUpdateLoaderView
import tech.okcredit.home.ui.home.views.homeScreenTapToSyncView
import javax.inject.Inject

@Reusable
class HomeController @Inject
constructor(private val homeFragment: HomeFragment) : AsyncEpoxyController() {
    private lateinit var state: HomeContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: HomeContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        when {
            state.inAppDownloadLoader -> homeScreenInAppUpdateLoaderView {
                id("homeScreenInAppUpdateLoaderView")
            }
            state.homeSyncLoader -> homeScreenHorizontalLoaderView {
                id("homeScreenTapToSyncView")
            }
            // Todo Refactor/moved this in viewModel (harshit)
            state.unSyncTxnCount > 0 || state.unSyncCustomerCount > 0 -> homeScreenTapToSyncView {
                id("homeScreenTapToSyncView")
                unSyncCount(state.unSyncTxnCount)
                listener(homeFragment)
            }
        }
    }
}
