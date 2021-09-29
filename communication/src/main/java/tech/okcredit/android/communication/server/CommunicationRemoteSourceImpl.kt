package tech.okcredit.android.communication.server

import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.communication.CommunicationRemoteSource
import javax.inject.Inject

class CommunicationRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<ApiClient>
) : CommunicationRemoteSource {

    override fun acknowledge(msgId: String, businessId: String): Completable {
        return apiClient.get().ack(AckRequest(msgId, System.currentTimeMillis()), businessId)
            .subscribeOn(Schedulers.io())
    }
}
