package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_SYNCED
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetMerchantAppLockPreference @Inject constructor(
    private val rxPreference: DefaultPreferences,
    private val setMerchantPreference: SetMerchantPreference,
    private val onboardingPreferences: Lazy<OnboardingPreferences>,
) : UseCase<Unit, Unit> {

    override fun execute(req: Unit): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            rxPreference.getBoolean(PREF_INDIVIDUAL_APP_LOCK_SYNCED, Scope.Individual).asObservable().firstOrError()
                .flatMapCompletable { synced ->
                    if (synced) {
                        Completable.complete()
                    } else {
                        if (onboardingPreferences.get().isAppLockEnabled()) {
                            setMerchantPreference
                                .execute(PreferenceKey.APP_LOCK, "true")
                                .andThen(
                                    rxCompletable {
                                        rxPreference.set(PREF_INDIVIDUAL_APP_LOCK_SYNCED, true, Scope.Individual)
                                    }
                                )
                        } else {
                            Completable.complete()
                        }
                    }
                }
        )
    }
}
