package `in`.okcredit.individual.contract

import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface IndividualRepository {
    fun getPreference(preferenceKey: PreferenceKey): Flow<String>
    suspend fun setPreference(key: String, value: String)
    suspend fun isPreferenceAvailable(key: String): Boolean
    fun clearLocalData(): Completable
}
