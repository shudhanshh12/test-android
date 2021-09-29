package `in`.okcredit.individual.data.remote

import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class IndividualRemoteServer @Inject constructor(
    private val apiClient: Lazy<IndividualApiClient>,
) {

    suspend fun getIndividual(businessId: String): GetIndividualResponse {
        return apiClient.get().getIndividual(businessId)
    }

    suspend fun updateIndividual(request: UpdateIndividualRequest, businessId: String) {
        return apiClient.get().updateIndividual(request, businessId)
    }

    suspend fun updateBusinessMobile(
        mobile: String,
        currentMobileOtpToken: String,
        newMobileOtpToken: String,
        individualId: String,
        businessId: String,
    ) {
        val request = UpdateIndividualRequest(
            current_mobile_otp_token = currentMobileOtpToken,
            new_mobile_otp_token = newMobileOtpToken,
            update_mobile = true,
            individual_user_id = individualId,
            individual_user = Individual(
                user = IndividualUser(
                    id = individualId,
                    mobile = mobile
                )
            )
        )
        return apiClient.get().updateIndividual(request, businessId)
    }
}
