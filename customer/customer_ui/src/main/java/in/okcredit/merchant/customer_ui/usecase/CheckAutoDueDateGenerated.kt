package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoParticularCustomerSyncer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Maybe
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class CheckAutoDueDateGenerated @Inject constructor(
    private val dueInfoParticularCustomerSyncer: Lazy<DueInfoParticularCustomerSyncer>,
    private val getCustomerDueInfo: Lazy<GetCustomerDueInfo>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getCustomer: Lazy<GetCustomer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String): Maybe<DateTime> {
        return getActiveBusinessId.get().execute().flatMapMaybe { businessId ->
            getCustomer.get().execute(customerId)
                .firstElement()
                .flatMap {
                    if (it.balanceV2 < 0) {
                        checkForAutoDueDate(customerId, businessId)
                    } else {
                        enableShowAutoDueDate(customerId, businessId).andThen(
                            Maybe.empty()
                        )
                    }
                }
        }
    }

    private fun checkForAutoDueDate(customerId: String, businessId: String): Maybe<DateTime> {
        return rxSharedPreference.get()
            .getBoolean(PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE + customerId, Scope.Business(businessId))
            .asObservable()
            .firstElement()
            .flatMap { show ->
                return@flatMap if (!show) {
                    checkForCustomerDue(customerId, businessId)
                } else {
                    Maybe.empty()
                }
            }
    }

    private fun checkForCustomerDue(customerId: String, businessId: String): Maybe<DateTime> {
        return getCustomerDueInfo.get().execute(GetCustomerDueInfo.Request(customerId))
            .firstElement()
            .flatMap { dueInfo ->
                if (dueInfo.isDueActive && dueInfo.activeDate != null && dueInfo.isAutoGenerated) {
                    Maybe.just(dueInfo.activeDate)
                } else {
                    dueInfoParticularCustomerSyncer.get().execute(customerId, businessId).andThen(
                        getCustomerDueInfo.get().execute(GetCustomerDueInfo.Request(customerId))
                            .firstElement()
                            .flatMap { newDueInfo ->
                                if (newDueInfo.isDueActive && newDueInfo.activeDate != null && newDueInfo.isAutoGenerated) {
                                    Maybe.just(newDueInfo.activeDate)
                                } else {
                                    Maybe.empty()
                                }
                            }
                    )
                }
            }
    }

    private fun enableShowAutoDueDate(customerId: String, businessId: String) = rxCompletable {
        rxSharedPreference.get()
            .set(PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE + customerId, false, Scope.Business(businessId))
    }
}
