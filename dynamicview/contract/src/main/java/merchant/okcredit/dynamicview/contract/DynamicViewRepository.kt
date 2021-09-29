package merchant.okcredit.dynamicview.contract

import io.reactivex.Completable

interface DynamicViewRepository {

    fun clearLocalData(): Completable

    fun scheduleSyncCustomizations(businessId: String): Completable
}
