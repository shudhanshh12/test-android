package `in`.okcredit.individual.usecase

import `in`.okcredit.individual.IndividualRepositoryImpl
import `in`.okcredit.individual.contract.UpdateIndividualMobile
import dagger.Lazy
import javax.inject.Inject

class UpdateIndividualMobileImpl @Inject constructor(
    private val repository: Lazy<IndividualRepositoryImpl>,
) : UpdateIndividualMobile {
    override suspend fun execute(
        mobile: String,
        currentMobileOtpToken: String,
        newMobileOtpToken: String,
        individualId: String,
        businessId: String,
    ) {
        repository.get()
            .updateBusinessMobile(mobile, currentMobileOtpToken, newMobileOtpToken, individualId, businessId)
    }
}
