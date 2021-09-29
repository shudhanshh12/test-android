package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase

import `in`.okcredit.backend.contract.SyncCustomers
import `in`.okcredit.backend.contract.SyncTransaction
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers
import javax.inject.Inject

class CreateCustomerAndTransaction @Inject constructor(
    private val userMigrationRepository: Lazy<UserMigrationRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val syncTransactions: Lazy<SyncTransaction>,
    private val syncCustomers: Lazy<SyncCustomers>
) {
    fun execute(list: List<Customers>): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                userMigrationRepository.get().createCustomerAndTransaction(customers = list, businessId = businessId)
                    .andThen(syncTransactions.get().executeForceSync(businessId))
                    .andThen(syncCustomers.get().execute(businessId))
            }
        )
    }
}
