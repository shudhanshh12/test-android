package tech.okcredit.applock.usecase

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_ENABLED
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_SYNCED
import `in`.okcredit.individual.contract.PreferenceKey
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetMerchantFingerprintPreference @Inject constructor(
    private val getMerchantPreference: Lazy<GetMerchantPreference>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
) {
    fun execute(): Observable<Boolean> {
        return defaultPreferences.get().getBoolean(FINGERPRINT_LOCK_SYNCED, Scope.Individual).asObservable()
            .switchMap { synced ->
                if (synced) {
                    defaultPreferences.get().getBoolean(FINGERPRINT_LOCK_ENABLED, Scope.Individual).asObservable()
                } else {
                    syncPreference().andThen(
                        defaultPreferences.get().getBoolean(FINGERPRINT_LOCK_ENABLED, Scope.Individual).asObservable()
                    )
                }
            }
    }

    fun syncPreference(): Completable {
        return getMerchantPreference.get().execute(PreferenceKey.FINGER_PRINT_LOCK).firstOrError()
            .flatMapCompletable { isFingerprintLockEnabled ->
                rxCompletable {
                    defaultPreferences.get()
                        .set(FINGERPRINT_LOCK_ENABLED, isFingerprintLockEnabled.toBoolean(), Scope.Individual)
                    defaultPreferences.get().set(FINGERPRINT_LOCK_SYNCED, true, Scope.Individual)
                }
            }
    }
}
