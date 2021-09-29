package tech.okcredit.android.ab

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface AbRepository {
    fun isFeatureEnabled(feature: String, ignoreCache: Boolean = false, businessId: String? = null): Observable<Boolean>

    fun isExperimentEnabled(experiment: String, businessId: String? = null): Observable<Boolean>

    fun getExperimentVariant(name: String, businessId: String? = null): Observable<String>

    fun sync(businessId: String? = null, sourceType: String): Completable

    fun clearLocalData(): Completable

    fun enabledFeatures(businessId: String? = null): Observable<List<String>>

    fun getProfile(businessId: String): Single<Profile>

    fun getVariantConfigurations(name: String, businessId: String? = null): Observable<Map<String, String>>

    fun startLanguageExperiment(string_resource_id: String, businessId: String? = null)

    fun scheduleSync(businessId: String, sourceType: String): Completable

    fun setProfile(profile: Profile, businessId: String): Completable
}
