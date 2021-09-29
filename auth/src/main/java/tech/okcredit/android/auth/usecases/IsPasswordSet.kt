package tech.okcredit.android.auth.usecases

import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.auth.AuthService
import javax.inject.Inject

class IsPasswordSet @Inject constructor(private val authService: Lazy<AuthService>) {

    fun execute() = Single.fromCallable { authService.get().isPasswordSet() }
}
