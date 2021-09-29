package tech.okcredit.home.ui.settings.usecase

import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_ENABLED
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_SYNCED
import `in`.okcredit.individual.contract.PreferenceKey.FINGER_PRINT_LOCK
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetFingerprintLockStatus @Inject constructor(
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val setIndividualPreference: Lazy<SetIndividualPreference>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(enable: Boolean): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            rxCompletable { rxSharedPreference.get().set(FINGERPRINT_LOCK_ENABLED, enable, Scope.Individual) }
                .andThen(
                    rxCompletable {
                        setIndividualPreference.get().schedule(FINGER_PRINT_LOCK.key, enable.toString(), businessId)
                        rxSharedPreference.get().set(FINGERPRINT_LOCK_SYNCED, true, Scope.Individual)
                    }.onErrorComplete()
                )
        }
    }
}
