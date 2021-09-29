package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.PreferenceKey
import dagger.Lazy
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class GetMerchantPreferenceImpl @Inject constructor(
    private val individualRepository: Lazy<IndividualRepository>,
) : GetMerchantPreference {

    override fun execute(preference: PreferenceKey) =
        individualRepository.get().getPreference(preference).asObservable()
}
