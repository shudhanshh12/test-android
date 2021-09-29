package `in`.okcredit.collection.contract

import io.reactivex.Single

interface GetBlindPayLinkId {
    fun execute(accountId: String): Single<String>
}
