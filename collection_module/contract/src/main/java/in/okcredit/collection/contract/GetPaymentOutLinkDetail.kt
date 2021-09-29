package `in`.okcredit.collection.contract

import io.reactivex.Single

interface GetPaymentOutLinkDetail {

    fun execute(
        accountId: String,
        accountType: String
    ): Single<ApiMessages.PaymentOutLinkDetailResponse>
}
