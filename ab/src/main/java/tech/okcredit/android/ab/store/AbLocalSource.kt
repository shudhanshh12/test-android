package tech.okcredit.android.ab.store

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.ab.Profile

interface AbLocalSource {

    fun getProfile(businessId: String, ignoreCache: Boolean = false): Observable<Profile>

    fun getProfileSingle(businessId: String): Single<Profile>

    fun startedExperiments(businessId: String): Observable<List<String>>

    fun recordExperimentStarted(name: String, businessId: String): Completable

    fun setProfile(profile: Profile, businessId: String): Completable

    suspend fun clearAbData(businessId: String)
}
