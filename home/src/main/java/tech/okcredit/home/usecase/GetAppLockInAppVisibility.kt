package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

@Reusable
class GetAppLockInAppVisibility @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val preferences: Lazy<DefaultPreferences>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, Boolean> {

    override fun execute(req: Unit): Observable<Result<Boolean>> {

        return if (isAppLockInAppExperimentEnabled()) {
            UseCase.wrapObservable(shouldShowInApp().startWith(false).distinctUntilChanged())
        } else {
            UseCase.wrapObservable(Observable.just(false))
        }
    }

    private fun shouldShowInApp(): Observable<Boolean> {
        val observables = listOf(
            isAppLockEnabled(),
            isInAppCancelled(),
            lessThan5Customers()
        )
        return Observable.combineLatest(observables) {
            var show = false
            val enabled = it[0] as Boolean
            val cancelled = it[1] as Boolean
            val lessThan5Customers = it[2] as Boolean
            when {
                enabled -> show = false
                cancelled -> show = false
                lessThan5Customers -> show = true
            }
            return@combineLatest show
        }
    }

    private fun isAppLockEnabled() = preferences.get()
        .getBoolean(OnboardingPreferences.KEY_APP_LOCK_ENABLED, Scope.Individual).asObservable()

    private fun isInAppCancelled() = preferences.get()
        .getBoolean(OnboardingPreferences.KEY_INAPP_APP_LOCK_CANCELLED, Scope.Individual).asObservable()

    private fun lessThan5Customers() =
        getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            customerRepo.get().getCustomersCount(businessId).map { it in 1..4 }
        }

    private fun isAppLockInAppExperimentEnabled() =
        true // TODO: Anjal. Should Remove this hardcoded experiment after confirmation from product
}
