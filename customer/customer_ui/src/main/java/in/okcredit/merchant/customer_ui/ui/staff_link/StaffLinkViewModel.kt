package `in`.okcredit.merchant.customer_ui.ui.staff_link

import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionStaffLinkScreen
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class StaffLinkViewModel @Inject constructor(
    initialState: StaffLinkContract.State,
    private val getCollectionStaffLinkScreen: Lazy<GetCollectionStaffLinkScreen>,
    private val staffLinkEventsTracker: Lazy<StaffLinkEventsTracker>,
) : BaseViewModel<StaffLinkContract.State, StaffLinkContract.PartialState, StaffLinkContract.ViewEvent>(initialState) {

    override fun handle(): Observable<StaffLinkContract.PartialState> {
        return Observable.mergeArray(
            loadStaffLinkScreen()
        )
    }

    private fun loadStaffLinkScreen() = intent<StaffLinkContract.Intent.Load>()
        .switchMap { wrap { getCollectionStaffLinkScreen.get().execute() } }
        .map {
            when (it) {
                is Result.Success -> {
                    val screen = it.value
                    when (screen) {
                        is GetCollectionStaffLinkScreen.StaffLinkScreen.ActiveStaffLink -> {
                            staffLinkEventsTracker.get().trackActiveListClicked(
                                linkId = screen.linkId,
                                listCreatedOn = screen.createTime ?: 0L,
                                customerCount = screen.customerIds.size
                            )
                        }
                        GetCollectionStaffLinkScreen.StaffLinkScreen.SelectCustomer -> {
                            staffLinkEventsTracker.get().trackCreateListClicked()
                        }
                        else -> {
                            // do nothing
                        }
                    }
                    emitViewEvent(StaffLinkContract.ViewEvent.MoveToScreen(screen))
                    StaffLinkContract.PartialState.SetLoading(false)
                }
                is Result.Failure -> {
                    StaffLinkContract.PartialState.SetLoading(false)
                }
                else -> {
                    StaffLinkContract.PartialState.SetLoading(true)
                }
            }
        }

    override fun reduce(
        currentState: StaffLinkContract.State,
        partialState: StaffLinkContract.PartialState,
    ): StaffLinkContract.State {
        return when (partialState) {
            StaffLinkContract.PartialState.NoChange -> currentState
            is StaffLinkContract.PartialState.SetLoading -> currentState.copy(loading = partialState.loading)
        }
    }
}
