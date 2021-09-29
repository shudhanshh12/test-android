package merchant.okcredit.accounting.usecases

import dagger.Lazy
import merchant.okcredit.accounting.repo.AccountingRepositoryImpl
import javax.inject.Inject

class SetContactPermissionAskedOnce @Inject constructor(
    private val accountingRepositoryImpl: Lazy<AccountingRepositoryImpl>,
) {
    suspend fun execute(value: Boolean) =
        accountingRepositoryImpl.get().setContactPermissionAskedOnce(value)
}
