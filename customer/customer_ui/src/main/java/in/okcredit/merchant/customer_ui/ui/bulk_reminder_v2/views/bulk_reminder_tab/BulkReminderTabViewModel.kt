package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab.BulkReminderTabContract.PartialState
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab.BulkReminderTabContract.State
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BulkReminderTabViewModel @Inject constructor(
    var initialState: State,
) : BaseLayoutViewModel<State, PartialState>(
    initialState,
    Schedulers.newThread(),
    Schedulers.newThread()
) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.empty()
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return currentState
    }

    override fun setNavigation(baseLayout: BaseLayout<State>) {}
}
