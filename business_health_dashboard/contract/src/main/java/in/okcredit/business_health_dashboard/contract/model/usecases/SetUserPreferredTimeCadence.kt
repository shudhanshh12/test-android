package `in`.okcredit.business_health_dashboard.contract.model.usecases

import io.reactivex.Completable

interface SetUserPreferredTimeCadence {
    fun execute(userPreferredTimeCadence: String): Completable
}
