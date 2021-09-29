package tech.okcredit.android.ab.store

import com.f2prateek.rx.preferences2.Preference
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.Profile
import tech.okcredit.android.ab.store.AbPreferences.Keys.PREF_BUSINESS_PROFILE
import tech.okcredit.android.ab.store.AbPreferences.Keys.PREF_BUSINESS_STARTED_EXPERIMENTS
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.offloaded
import timber.log.Timber
import javax.inject.Inject

class AbLocalSourceImpl @Inject constructor(
    private val rxPref: Lazy<AbPreferences>,
) : AbLocalSource {

    companion object {
        var getProfileCache: HashMap<String, BehaviorSubject<Profile>> = HashMap()
    }

    private val profileCodec by lazy {
        object : Preference.Converter<Profile> {
            override fun deserialize(serialized: String): Profile =
                GsonUtils.gson().fromJson(serialized, Profile::class.java)

            override fun serialize(value: Profile): String =
                GsonUtils.gson().toJson(value)
        }
    }

    override fun getProfile(businessId: String, ignoreCache: Boolean): Observable<Profile> {
        return rxPref.offloaded()
            .flatMapObservable {
                when {
                    ignoreCache -> {
                        it.getObject(PREF_BUSINESS_PROFILE, Scope.Business(businessId), Profile(), profileCodec)
                            .asObservable()
                    }
                    getProfileCache.containsKey(businessId) -> { // Cache hit
                        getProfileCache[businessId]
                    }
                    else -> { // Cache miss
                        BehaviorSubject.create<Profile>().apply {
                            getProfileCache[businessId] = this
                            it.getObject(PREF_BUSINESS_PROFILE, Scope.Business(businessId), Profile(), profileCodec)
                                .asObservable()
                                .distinctUntilChanged()
                                .subscribe(this)
                        }
                    }
                }
            }.observeOn(ThreadUtils.worker()).doAfterNext {
                Timber.d("<<<< $it")
            }
    }

    override fun startedExperiments(businessId: String): Observable<List<String>> {
        return rxPref.get().getString(PREF_BUSINESS_STARTED_EXPERIMENTS, Scope.Business(businessId))
            .asObservable()
            .map { it.split(",").toTypedArray().toList() }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(ThreadUtils.worker())
    }

    override fun recordExperimentStarted(name: String, businessId: String): Completable {
        return rxCompletable {
            val experiments =
                rxPref.get().getString(PREF_BUSINESS_STARTED_EXPERIMENTS, Scope.Business(businessId)).first()
            val newValue = when {
                experiments.contains(name) -> experiments
                experiments.isEmpty() -> name
                else -> "$experiments,$name"
            }
            rxPref.get().set(PREF_BUSINESS_STARTED_EXPERIMENTS, newValue.replace(" ", ""), Scope.Business(businessId))
        }.subscribeOn(Schedulers.io())
    }

    override fun getProfileSingle(businessId: String): Single<Profile> {
        return rxPref.get().getObject(PREF_BUSINESS_PROFILE, Scope.Business(businessId), Profile(), profileCodec)
            .asObservable()
            .firstOrError()
            .subscribeOn(Schedulers.io())
    }

    override fun setProfile(profile: Profile, businessId: String): Completable {
        return rxCompletable {
            rxPref.get().set(PREF_BUSINESS_PROFILE, profile, Scope.Business(businessId), profileCodec)
        }.subscribeOn(Schedulers.io())
    }

    override suspend fun clearAbData(businessId: String) {
        rxPref.get().remove(PREF_BUSINESS_PROFILE, Scope.Business(businessId))
        rxPref.get().remove(PREF_BUSINESS_STARTED_EXPERIMENTS, Scope.Business(businessId))
    }
}
