package `in`.okcredit.business_health_dashboard.usecases

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import `in`.okcredit.business_health_dashboard.contract.model.usecases.GetBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.repository.BusinessHealthDashboardRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetBusinessHealthDashboardDataImpl @Inject constructor(
    private val businessHealthDashboardRepository: Lazy<BusinessHealthDashboardRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetBusinessHealthDashboardData {
    override fun execute(): Observable<BusinessHealthDashboardModel> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            businessHealthDashboardRepository.get().getBusinessHealthDashboardData(businessId)
        }
    }
}
