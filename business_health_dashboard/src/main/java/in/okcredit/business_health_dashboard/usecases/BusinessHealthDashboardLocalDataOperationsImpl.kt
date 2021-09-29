package `in`.okcredit.business_health_dashboard.usecases

import `in`.okcredit.business_health_dashboard.contract.model.usecases.BusinessHealthDashboardLocalDataOperations
import `in`.okcredit.business_health_dashboard.repository.BusinessHealthDashboardRepository
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class BusinessHealthDashboardLocalDataOperationsImpl @Inject constructor(
    private val businessHealthDashboardRepository: Lazy<BusinessHealthDashboardRepository>,
) : BusinessHealthDashboardLocalDataOperations {

    override fun executeClearLocalData(): Completable {
        return businessHealthDashboardRepository.get().clearLocalData()
    }
}
