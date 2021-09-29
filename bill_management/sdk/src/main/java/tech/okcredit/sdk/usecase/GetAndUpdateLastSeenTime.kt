package tech.okcredit.sdk.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.BillUtils
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.sdk.store.BillLocalSource
import javax.inject.Inject

class GetAndUpdateLastSeenTime @Inject constructor(
    private val billLocalSource: Lazy<BillLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(accountId: String): Single<String> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            billLocalSource.get().getLastSeenTime(accountId).flatMap {
                billLocalSource.get().updateSeenTime(accountId, BillUtils.currentTimestamp().toString(), businessId)
                    .andThen(getLastSeenTime(it, businessId))
            }
        }
    }

    private fun getLastSeenTime(lastSeen: String, businessId: String): Single<String> {
        return if (lastSeen.isBlank() || lastSeen == "0") {
            billLocalSource.get().getBillAdoptionTime(DateTimeUtils.currentDateTime().millis, businessId).firstOrError()
                .map { it.toString() }
        } else {
            Single.just(lastSeen)
        }
    }
}
