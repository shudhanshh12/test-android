package tech.okcredit.android.auth.usecases

import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.rxjava.SchedulerProvider
import javax.inject.Inject

class ResetPassword @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val schedulerProvider: Lazy<SchedulerProvider>
) {

    fun execute(newPassword: String?): Completable {
        return Completable.fromAction { authService.get().setPassword(newPassword!!) }
            .subscribeOn(schedulerProvider.get().io())
    }
}
