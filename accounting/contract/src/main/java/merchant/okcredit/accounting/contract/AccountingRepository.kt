package merchant.okcredit.accounting.contract

import io.reactivex.Completable

interface AccountingRepository {
    fun clearAccountingData(): Completable
    fun get24x7String(): String
    fun getCustomerCareCallNumber(): String
    fun getCustomerCareChatNumber(): String
}
