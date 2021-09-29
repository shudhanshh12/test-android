package `in`.okcredit.business_health_dashboard.datasource.local

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.json.GsonUtils
import javax.inject.Inject

@Reusable
class BusinessHealthDashboardLocalSource @Inject constructor(
    private val businessHealthDashboardPreferences: Lazy<BusinessHealthDashboardPreferences>,
) {

    private val gson = GsonUtils.gsonVanillaInstance()

    fun getBusinessHealthDashboardData(businessId: String): Observable<BusinessHealthDashboardModel> {
        return businessHealthDashboardPreferences.get().getBusinessHealthDashboardData(businessId)
            .map { serialisedObject ->
                if (serialisedObject.isEmpty()) {
                    throw BusinessHealthDashboardNoLocalDataException
                } else {
                    return@map gson.fromJson(serialisedObject, BusinessHealthDashboardModel::class.java)
                }
            }
    }

    fun setBusinessHealthDashboardData(
        businessHealthDashboardModel: BusinessHealthDashboardModel,
        businessId: String,
    ): Completable {
        return businessHealthDashboardPreferences.get().setBusinessHealthDashboardData(
            gson.toJson(businessHealthDashboardModel),
            businessId
        )
    }

    fun getUserPreferredTimeCadence(businessId: String): Observable<String> {
        return businessHealthDashboardPreferences.get().getUserPreferredTimeCadence(businessId)
    }

    fun setUserPreferredTimeCadence(userPreferredTimeCadence: String, businessId: String): Completable {
        return businessHealthDashboardPreferences.get()
            .setUserPreferredTimeCadence(userPreferredTimeCadence, businessId)
    }

    fun clear(): Completable {
        return rxCompletable { businessHealthDashboardPreferences.get().clear() }
    }
}

object BusinessHealthDashboardNoLocalDataException : Exception()
