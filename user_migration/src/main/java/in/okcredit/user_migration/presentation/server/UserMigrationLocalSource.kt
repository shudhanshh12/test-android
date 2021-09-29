package `in`.okcredit.user_migration.presentation.server

import `in`.okcredit.user_migration.presentation.server.migration_preferences.MigrationPreferences
import `in`.okcredit.user_migration.presentation.server.migration_preferences.MigrationPreferences.Keys.PREF_BUSINESS_PREDICTED_DATA
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.user_migration.contract.models.PredictedData
import javax.inject.Inject

class UserMigrationLocalSource @Inject constructor(
    private val preference: Lazy<MigrationPreferences>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
) {
    companion object {
        private val gson = Gson()
    }

    suspend fun getPredictedData(businessId: String): PredictedData? {
        return withContext(dispatcherProvider.get().io()) {
            val json = preference.get().getString(PREF_BUSINESS_PREDICTED_DATA, Scope.Business(businessId)).first()
            gson.fromJson(json, PredictedData::class.java)
        }
    }

    fun getFileObjectIdFromPredictedData(businessId: String): Observable<String> {
        return preference.get().getString(PREF_BUSINESS_PREDICTED_DATA, Scope.Business(businessId))
            .asObservable()
            .map { json -> gson.fromJson(json, PredictedData::class.java).fileObjectId }
            .onErrorReturnItem("")
    }

    suspend fun setPredictedData(predictedData: PredictedData, businessId: String) {
        preference.get().set(PREF_BUSINESS_PREDICTED_DATA, gson.toJson(predictedData), Scope.Business(businessId))
    }

    fun clearPredictedData(businessId: String) = rxCompletable {
        preference.get().remove(PREF_BUSINESS_PREDICTED_DATA, Scope.Business(businessId))
    }
}
