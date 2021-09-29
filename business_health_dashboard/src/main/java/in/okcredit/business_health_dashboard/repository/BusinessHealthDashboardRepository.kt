package `in`.okcredit.business_health_dashboard.repository

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import `in`.okcredit.business_health_dashboard.datasource.local.BusinessHealthDashboardLocalSource
import `in`.okcredit.business_health_dashboard.datasource.local.BusinessHealthDashboardNoLocalDataException
import `in`.okcredit.business_health_dashboard.datasource.remote.BusinessHealthDashboardRemoteSource
import `in`.okcredit.business_health_dashboard.datasource.remote.apiClient.BusinessHealthEntityMapper.BUSINESS_HEALTH_ENTITY_CONVERTER
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@Reusable
class BusinessHealthDashboardRepository @Inject constructor(
    private val businessHealthDashboardRemoteSource: Lazy<BusinessHealthDashboardRemoteSource>,
    private val businessHealthDashboardLocalSource: Lazy<BusinessHealthDashboardLocalSource>,
) {

    fun getBusinessHealthDashboardData(businessId: String): Observable<BusinessHealthDashboardModel> {
        return Observable.combineLatest(
            businessHealthDashboardLocalSource.get().getBusinessHealthDashboardData(businessId)
                .onErrorResumeNext { throwable: Throwable? ->
                    if (throwable is BusinessHealthDashboardNoLocalDataException) {
                        fetchFromRemoteAndSaveToLocal(businessId).andThen(
                            businessHealthDashboardLocalSource.get().getBusinessHealthDashboardData(businessId)
                        )
                    } else {
                        Observable.empty()
                    }
                },
            businessHealthDashboardLocalSource.get().getUserPreferredTimeCadence(businessId),
            BiFunction { dashboardData, userPreferredTimeCadenceString ->
                return@BiFunction dashboardData.timeCadenceList
                    .firstOrNull { it.title == userPreferredTimeCadenceString }
                    ?.let { dashboardData.copy(selectedTimeCadence = it) } ?: dashboardData
            }
        )
    }

    fun fetchFromRemoteAndSaveToLocal(businessId: String): Completable {
        return businessHealthDashboardRemoteSource.get().getBusinessHealthDashboardDataDto(businessId).toObservable()
            .switchMapCompletable { businessHealthDashboardDataDto ->
                businessHealthDashboardLocalSource.get().setBusinessHealthDashboardData(
                    requireNotNull(BUSINESS_HEALTH_ENTITY_CONVERTER.convert(businessHealthDashboardDataDto)),
                    businessId,
                )
            }
    }

    fun submitFeedbackForTrend(trendId: String, response: String, businessId: String): Completable {
        return businessHealthDashboardRemoteSource.get().submitFeedbackForTrend(trendId, response, businessId)
    }

    fun setUserPreferredTimeCadence(userPreferredTimeCadence: String, businessId: String): Completable {
        return businessHealthDashboardLocalSource.get()
            .setUserPreferredTimeCadence(userPreferredTimeCadence, businessId)
    }

    fun clearLocalData() = businessHealthDashboardLocalSource.get().clear()
}
