package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class SetPaymentPassword @Inject constructor(
    private val setMerchantPreference: SetMerchantPreference
) : UseCase<SetPaymentPassword.Request, Unit> {

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            setMerchantPreference.execute(
                PreferenceKey.PAYMENT_PASSWORD,
                req.enable.toString(),
                scheduleIfFailed = false
            )
        )
    }

    data class Request(val enable: Boolean)
}
