package merchant.okcredit.supplier.contract

import io.reactivex.Completable

interface SyncSupplierEnabledCustomerIds {
    fun execute(): Completable
}
