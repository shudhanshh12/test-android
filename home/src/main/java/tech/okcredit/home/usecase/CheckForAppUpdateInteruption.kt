package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class CheckForAppUpdateInteruption @Inject constructor(
    private val appUpdateManager: Lazy<AppUpdateManager>
) {

    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(checkInteruption())
    }

    private fun checkInteruption(): Observable<Boolean> {
        return Observable.create { emitter ->
            appUpdateManager.get()
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    ) {
                        emitter.onNext(true)
                    } else {
                        emitter.onNext(false)
                    }
                }
        }
    }
}
