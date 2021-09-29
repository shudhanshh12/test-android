package tech.okcredit.android.auth.usecases

import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.auth.AuthService
import javax.inject.Inject

class VerifyPassword @Inject constructor(private val authService: Lazy<AuthService>) {

    fun execute(password: String?) = Completable.fromAction { authService.get().verifyPassword(password!!) }
}
