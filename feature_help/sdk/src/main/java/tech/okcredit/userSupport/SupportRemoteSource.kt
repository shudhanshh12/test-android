package tech.okcredit.userSupport

import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.userSupport.server.HelpApiResponse

interface SupportRemoteSource {

    fun getHelp(language: String, businessId: String): Single<HelpApiResponse>

    fun submitFeedback(message: String, feedback_type: String, businessId: String): Completable
}
