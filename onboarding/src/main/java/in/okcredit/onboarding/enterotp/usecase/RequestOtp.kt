package `in`.okcredit.onboarding.enterotp.usecase

import io.reactivex.Single
import io.reactivex.exceptions.UndeliverableException
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class RequestOtp @Inject constructor(private val authService: AuthService) {
    fun execute(req: String?): Single<OtpToken> {
        return Single.fromCallable {
            try {
                return@fromCallable if (req.isNullOrEmpty()) {
                    authService.requestOtp()
                } else {
                    authService.requestOtp(req)
                }
            } catch (e: UndeliverableException) {
                throw RuntimeException(e.cause)
            }
        }.subscribeOn(ThreadUtils.api())
    }
}
