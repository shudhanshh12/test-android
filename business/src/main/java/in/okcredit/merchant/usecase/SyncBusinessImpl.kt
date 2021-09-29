package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.SyncBusiness
import `in`.okcredit.merchant.store.database.DbEntityMapper.toBusiness
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

/**
 * [SyncBusinessImpl] fetches merchant from remote, save to local.
 */
class SyncBusinessImpl @Inject constructor(
    private val businessSyncer: Lazy<BusinessSyncerImpl>,
) : SyncBusiness {
    override fun execute(businessId: String): Single<Business> =
        businessSyncer.get().fetchBusiness(businessId)
            .flatMap { business ->
                businessSyncer.get().saveBusiness(business)
                    .andThen(Single.just(business.toBusiness()))
            }
}
