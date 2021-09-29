package `in`.okcredit.business_health_dashboard.contract.model.usecases

import io.reactivex.Completable

interface RefreshBusinessHealthDashboardData {
    fun execute(): Completable
}
