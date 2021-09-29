package tech.okcredit.use_case

import `in`.okcredit.backend.contract.RxSharedPrefValues.BILL_SEEN_FIRST_TIME
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.bills.GetAndUpdateBillSeenFirstTime
import javax.inject.Inject

class GetAndUpdateBillSeenFirstTimeImpl @Inject constructor(
    private val rxSharedPreference: Lazy<DefaultPreferences>,
) : GetAndUpdateBillSeenFirstTime {
    /**
     * @return - true if bill seen for first time, else returns false
     */
    override fun execute(): Observable<Boolean> {
        return rxSharedPreference.get().getBoolean(BILL_SEEN_FIRST_TIME, Scope.Individual).asObservable()
            .switchMap {
                if (it.not()) {
                    rxCompletable { rxSharedPreference.get().set(BILL_SEEN_FIRST_TIME, true, Scope.Individual) }
                        .andThen(Observable.just(true))
                } else {
                    Observable.just(false)
                }
            }
    }
}
