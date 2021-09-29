package tech.okcredit.userSupport.server

import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.userSupport.SupportRemoteSource
import tech.okcredit.userSupport.model.UserSuccessFeedBackRequest
import javax.inject.Inject

class SupportRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<ApiClient>
) : SupportRemoteSource {

    override fun getHelp(language: String, businessId: String): Single<HelpApiResponse> {
        return apiClient.get().getHelp(businessId, language, businessId)
            .subscribeOn(Schedulers.io())
    }

    override fun submitFeedback(message: String, feedback_type: String, businessId: String): Completable {
        return apiClient.get()
            .submitFeedback(UserSuccessFeedBackRequest(businessId, message, feedback_type), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
    }
}
