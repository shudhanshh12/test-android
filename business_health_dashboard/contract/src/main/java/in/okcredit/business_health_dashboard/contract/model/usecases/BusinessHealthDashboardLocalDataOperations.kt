package `in`.okcredit.business_health_dashboard.contract.model.usecases

import io.reactivex.Completable

interface BusinessHealthDashboardLocalDataOperations {
    fun executeClearLocalData(): Completable
}
