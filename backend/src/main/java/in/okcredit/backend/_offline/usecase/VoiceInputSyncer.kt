package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.VoiceInputResponseBody
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

class VoiceInputSyncer @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(string: String?): Single<Response<VoiceInputResponseBody>> {
        return getActiveBusinessId.get().execute()
            .flatMap { businessId ->
                remoteSource.get().postVoiceInput(string, businessId)
            }
    }
}
