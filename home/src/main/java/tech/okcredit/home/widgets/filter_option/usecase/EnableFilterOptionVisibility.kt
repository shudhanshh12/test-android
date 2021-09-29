package tech.okcredit.home.widgets.filter_option.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.home.HomePreferences
import `in`.okcredit.home.HomePreferences.Keys.PREF_BUSINESS_FILTER_ENABLED
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class EnableFilterOptionVisibility @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val homePreference: Lazy<HomePreferences>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                canEnabledFilterOption()
                    .flatMap { canShow ->
                        isFilterEnabledPreference(businessId)
                            .flatMapObservable { isFilterShownBefore ->
                                setFilterEnabledPreference(canShow, businessId)
                                    .andThen(Observable.just(isFilterShownBefore || canShow))
                            }
                    }
            }
                .startWith(false)
                .distinctUntilChanged()
        )
    }

    fun canEnabledFilterOption(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.zip(
                isCustomersLessThanThree(businessId), isSuppliersLessThanThree(businessId),
                BiFunction { customerCheck: Boolean, supplierCheck: Boolean ->
                    return@BiFunction customerCheck || supplierCheck
                }
            )
        }
    }

    private fun isSuppliersLessThanThree(businessId: String): Observable<Boolean> {
        return supplierCreditRepository.get().getSuppliersCount(businessId).map { isCountGreaterThan3(it) }
    }

    private fun isCustomersLessThanThree(businessId: String): Observable<Boolean> {
        return customerRepo.get().getCustomersCount(businessId).map { isCountGreaterThan3(it.toLong()) }
    }

    private fun isCountGreaterThan3(count: Long): Boolean = count >= 3

    private fun isFilterEnabledPreference(businessId: String) =
        homePreference.get().getBoolean(PREF_BUSINESS_FILTER_ENABLED, Scope.Business(businessId))
            .asObservable().firstOrError()

    private fun setFilterEnabledPreference(shown: Boolean, businessId: String) = rxCompletable {
        homePreference.get().set(PREF_BUSINESS_FILTER_ENABLED, shown, Scope.Business(businessId))
    }
}
