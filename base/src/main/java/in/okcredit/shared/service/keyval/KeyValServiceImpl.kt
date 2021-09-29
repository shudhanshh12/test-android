package `in`.okcredit.shared.service.keyval

import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils.database
import javax.inject.Inject

@Deprecated("Use shared preferences")
class KeyValServiceImpl @Inject internal constructor(
    private val defaultPreferences: Lazy<DefaultPreferences>,
) : KeyValService {
    override fun contains(key: String, scope: Scope): Single<Boolean> {
        return rxSingle { defaultPreferences.get().contains(key, scope) }
            .subscribeOn(database())
    }

    override fun get(key: String, scope: Scope): Observable<String> {
        return defaultPreferences.get().getString(key, scope)
            .asObservable()
            .subscribeOn(database())
    }

    override fun put(key: String, value: String, scope: Scope): Completable {
        return rxCompletable { defaultPreferences.get().set(key, value, scope) }
            .subscribeOn(database())
    }

    override fun delete(key: String, scope: Scope): Completable {
        return rxCompletable { defaultPreferences.get().remove(key, scope) }
            .subscribeOn(database())
    }
}
