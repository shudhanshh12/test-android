package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface SubmitFeedback {

    fun execute(feedback: String?, rating: Int, businessId: String? = null): Completable

    fun schedule(feedback: String?, rating: Int): Completable
}
