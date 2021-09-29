package `in`.okcredit.backend._offline.serverV2

import `in`.okcredit.backend._offline.server.internal.ApiClient
import `in`.okcredit.backend._offline.serverV2.internal.ApiMessagesV2
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class ServerV2 @Inject constructor(private val apiClient: ApiClient) {

    fun getCustomerTransactions(startDate: Long, source: String, businessId: String): Single<ApiMessagesV2.GetTransactionsResponse?> {
        return apiClient.getTransactions(
            ApiMessagesV2.GetTransactionsRequest(
                ApiMessagesV2.TransactionsRequest(
                    type = 0,
                    role = 1,
                    start_time = startDate
                )
            ),
            source,
            businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    fun getTransactionFile(id: String, businessId: String): Single<ApiMessagesV2.GetTransactionFileResponse?> {
        return apiClient.getTransactionFile(ApiMessagesV2.GetTransactionFileRequest(id), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }
}
