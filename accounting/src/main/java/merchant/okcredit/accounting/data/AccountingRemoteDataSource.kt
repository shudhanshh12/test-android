package merchant.okcredit.accounting.data

import dagger.Lazy
import merchant.okcredit.accounting.server.AccountingApiClient
import javax.inject.Inject

class AccountingRemoteDataSource @Inject constructor(private val apiClient: Lazy<AccountingApiClient>) {

    fun getBackUpURL(businessId: String) = apiClient.get().getBackUp(businessId).map { it.reportUrl }
}
