package `in`.okcredit.user_migration.presentation

import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.presentation.server.UserMigrationLocalSource
import `in`.okcredit.user_migration.presentation.server.UserMigrationRemoteSource
import androidx.fragment.app.Fragment
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.extensions.navigate
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.ParsedMigrationFileResponse
import tech.okcredit.user_migration.contract.models.PredictedData
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers
import javax.inject.Inject

class UserMigrationRepositoryImpl @Inject constructor(
    private val remoteSource: Lazy<UserMigrationRemoteSource>,
    private val localSource: Lazy<UserMigrationLocalSource>
) : UserMigrationRepository {

    override fun navigate(fragment: Fragment) {
        fragment.navigate(R.id.user_migration)
    }

    override fun getParseFileData(
        urls: List<String?>,
        businessId: String
    ): Single<List<ParsedMigrationFileResponse>> {
        return remoteSource.get().getParseFileData(urls, businessId)
    }

    override fun createCustomerAndTransaction(
        customers: List<Customers>,
        businessId: String
    ): Completable {
        return remoteSource.get().createCustomerAndTransaction(customers, businessId)
    }

    override suspend fun getPredictedData(url: String, businessId: String) =
        remoteSource.get().getPredictedDataFromRemote(url, businessId).apply {
            localSource.get().setPredictedData(predictedData = this, businessId = businessId)
        }

    override suspend fun getPredictedDataFromLocalSource(businessId: String): PredictedData? {
        return localSource.get().getPredictedData(businessId)
    }

    override fun getFileObjectIdFromPredictedData(businessId: String): Observable<String> {
        return localSource.get().getFileObjectIdFromPredictedData(businessId)
    }

    override suspend fun setAmountAmended(request: SetAmountAmendedApiRequest, businessId: String): Completable {
        return remoteSource.get().setAmountAmended(request, businessId)
    }

    override fun setAmountAmend(request: SetAmountAmendedApiRequest, businessId: String): Completable {
        return remoteSource.get().setAmountAmended(request, businessId)
    }

    override fun clearPredictionData(businessId: String) = localSource.get().clearPredictedData(businessId)
}
