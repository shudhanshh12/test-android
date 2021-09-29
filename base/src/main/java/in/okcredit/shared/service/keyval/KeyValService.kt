package `in`.okcredit.shared.service.keyval

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.preferences.Scope

// TODO replace usages of KeyValService by DefaultPreferences
@Deprecated("Use shared preferences")
interface KeyValService {
    fun contains(key: String, scope: Scope): Single<Boolean>

    operator fun get(key: String, scope: Scope): Observable<String>

    fun put(key: String, value: String, scope: Scope): Completable

    fun delete(key: String, scope: Scope): Completable
}
