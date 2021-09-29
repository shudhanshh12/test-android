package tech.okcredit.applock.usecase

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class CheckFingerPrintLockAvailability @Inject constructor(private val context: Lazy<Context>) {
    fun execute(): Observable<Boolean> {
        val biometricManager = BiometricManager.from(context.get())
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Observable.just(true)
            }
            else -> {
                Observable.just(false)
            }
        }
    }
}
