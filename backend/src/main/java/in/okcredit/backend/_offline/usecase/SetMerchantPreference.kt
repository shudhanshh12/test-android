package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class SetMerchantPreference @Inject constructor(
    private val setIndividualPreference: Lazy<SetIndividualPreference>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(preference: PreferenceKey, value: String, scheduleIfFailed: Boolean = true): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            rxCompletable { setIndividualPreference.get().execute(preference.key, value, businessId) }
                .onErrorResumeNext {
                    rxCompletable {
                        if (scheduleIfFailed) setIndividualPreference.get().schedule(preference.key, value, businessId)
                        else throw it
                    }
                }
        }
    }
}
