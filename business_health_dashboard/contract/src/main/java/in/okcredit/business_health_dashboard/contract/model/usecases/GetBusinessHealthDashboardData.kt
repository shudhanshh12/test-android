package `in`.okcredit.business_health_dashboard.contract.model.usecases

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import io.reactivex.Observable

interface GetBusinessHealthDashboardData {
    fun execute(): Observable<BusinessHealthDashboardModel>
}
