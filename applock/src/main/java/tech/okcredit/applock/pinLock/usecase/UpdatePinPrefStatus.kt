package tech.okcredit.applock.pinLock.usecase

import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class UpdatePinPrefStatus @Inject constructor(
    private val setIndividualPreference: Lazy<SetIndividualPreference>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(enable: Boolean): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                rxCompletable {
                    setIndividualPreference.get()
                        .execute(PreferenceKey.FOUR_DIGIT_PIN.key, enable.toString(), businessId)
                }
            }
        )
    }
}
