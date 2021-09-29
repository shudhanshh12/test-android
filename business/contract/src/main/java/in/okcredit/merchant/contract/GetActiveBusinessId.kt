package `in`.okcredit.merchant.contract

import io.reactivex.Single

interface GetActiveBusinessId {
    /**
     * Returns default business id if user is logged in. If user is not logged in, returns a blank string
     */
    fun execute(): Single<String>

    /**
     * [thisOrActiveBusinessId] returns @param businessId if it is not null or else returns the default business id
     */
    fun thisOrActiveBusinessId(businessId: String?): Single<String>
}
