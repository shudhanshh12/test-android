package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.UndeliverableException
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import javax.inject.Inject

class AuthenticateOtp @Inject constructor(private val authService: AuthService) : UseCase<Credential, Boolean> {
    override fun execute(req: Credential): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            Single
                .fromCallable {
                    try {
                        authService.authenticatePhoneChangeCredential(req)
                    } catch (e: UndeliverableException) {
                        throw RuntimeException(e)
                    }
                }
        )
    }
}
