package `in`.okcredit.frontend.usecase.onboarding.applock

import `in`.okcredit.individual.contract.PreferenceKey.APP_LOCK
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_SYNCED
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetAppLockStatus @Inject constructor(
    private val rxPreference: Lazy<DefaultPreferences>,
    private val setIndividualPreference: Lazy<SetIndividualPreference>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
) : UseCase<Boolean, Unit> {
    override fun execute(enable: Boolean): Observable<Result<Unit>> {

        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                if (enable) {
                    Completable.mergeArray(
                        rxCompletable {
                            onboardingPreferences.get()
                                .set(
                                    OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK,
                                    false,
                                    Scope.Individual
                                )
                            onboardingPreferences.get()
                                .set(OnboardingPreferences.KEY_APP_LOCK_ENABLED, true, Scope.Individual)
                        },
                        rxCompletable {
                            setIndividualPreference.get().schedule(APP_LOCK.key, true.toString(), businessId)
                        },
                        rxCompletable {
                            rxPreference.get().set(OnboardingPreferences.KEY_NEW_USER, false, Scope.Individual)
                        },
                        rxCompletable {
                            rxPreference.get().set(PREF_INDIVIDUAL_APP_LOCK_SYNCED, true, Scope.Individual)
                        }
                    )
                } else {
                    Completable.mergeArray(
                        rxCompletable {
                            onboardingPreferences.get()
                                .set(
                                    OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK,
                                    false,
                                    Scope.Individual
                                )
                            onboardingPreferences.get()
                                .set(OnboardingPreferences.KEY_APP_LOCK_ENABLED, false, Scope.Individual)
                        },
                        rxCompletable {
                            setIndividualPreference.get().schedule(APP_LOCK.key, false.toString(), businessId)
                        },
                        rxCompletable {
                            rxPreference.get().set(PREF_INDIVIDUAL_APP_LOCK_SYNCED, true, Scope.Individual)
                        }
                    )
                }
            }
        )
    }
}
