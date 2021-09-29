package `in`.okcredit.storesms.data.server

import dagger.Lazy
import javax.inject.Inject

class StoreSmsServer @Inject constructor(private val apiClient: Lazy<StoreSmsApiClient>) {

    suspend fun sendRawSmsToServer(list: List<RawSms>, businessId: String) = apiClient.get().sendRawSmsToServer(
        RawSmsRequestBody(
            texts = list
        ),
        businessId
    )

    suspend fun getLastRawSmsSyncedTime(businessId: String) = apiClient.get().getLasRawSmsSyncedTime(businessId)
}
