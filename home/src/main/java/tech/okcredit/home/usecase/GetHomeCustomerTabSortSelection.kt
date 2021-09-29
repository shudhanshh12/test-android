package tech.okcredit.home.usecase

import `in`.okcredit.home.HomePreferences
import `in`.okcredit.home.HomePreferences.Keys.PREF_BUSINESS_CUSTOMER_TAB_SORT
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.home.ui.homesearch.Sort.sortByDefault
import javax.inject.Inject

class GetHomeCustomerTabSortSelection @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val homePreferences: Lazy<HomePreferences>,
) {
    fun execute(): Single<String> {
        return getActiveBusinessId.get().execute()
            .flatMap { businessId ->
                homePreferences.get()
                    .getString(PREF_BUSINESS_CUSTOMER_TAB_SORT, Scope.Business(businessId), sortByDefault)
                    .asObservable()
                    .firstOrError()
                    .onErrorReturn { sortByDefault }
            }
    }
}
