package `in`.okcredit.business_health_dashboard.usecases

import `in`.okcredit.business_health_dashboard.contract.model.usecases.RefreshBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.repository.BusinessHealthDashboardRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class RefreshBusinessHealthDashboardDataImpl @Inject constructor(
    private val businessHealthDashboardRepository: Lazy<BusinessHealthDashboardRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RefreshBusinessHealthDashboardData {
    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            businessHealthDashboardRepository.get().fetchFromRemoteAndSaveToLocal(businessId)
        }
    }
}
