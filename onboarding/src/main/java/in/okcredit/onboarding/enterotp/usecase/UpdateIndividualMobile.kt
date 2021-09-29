package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.individual.contract.UpdateIndividualMobile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.auth.AuthService
import javax.inject.Inject

class UpdateIndividualMobile @Inject constructor(
    private val updateIndividualMobile: Lazy<UpdateIndividualMobile>,
    private val authService: AuthService,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getIndividual: Lazy<GetIndividual>,
) : UseCase<String, Unit> {

    override fun execute(mobile: String): Observable<Result<Unit>> {
        val currentMobileOTPToken = authService.getCurrentMobileOtpToken()!!
        val newMobileOTPToken = authService.getNewMobileOtpToken()!!
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                rxCompletable {
                    val individual = getIndividual.get().execute().first()
                    updateIndividualMobile.get()
                        .execute(mobile, currentMobileOTPToken, newMobileOTPToken, individual.id, businessId)
                }
            }

        )
    }
}
