package tech.okcredit.android.ab

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.ab.sdk.ABExperiments
import tech.okcredit.android.ab.store.AbLocalSource
import tech.okcredit.android.ab.usecase.AcknowledgeExperiment
import tech.okcredit.android.ab.usecase.SyncAbProfile
import tech.okcredit.android.ab.workers.AbSyncWorker
import tech.okcredit.android.ab.workers.ClearAbData
import tech.okcredit.android.ab.workers.ExperimentAcknowledgeWorker
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AbRepositoryImpl @Inject constructor(
    private val localSource: Lazy<AbLocalSource>,
    private val syncAbProfile: Lazy<SyncAbProfile>,
    private val acknowledgeExperiment: Lazy<AcknowledgeExperiment>,
    private val workManager: Lazy<OkcWorkManager>,
    private val localeManager: Lazy<LocaleManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val clearAbData: Lazy<ClearAbData>,
) : AbRepository {

    companion object {
        const val WORKER_PROFILE = "ab/profile"
        const val WORKER_ACKNOWLEDGEMENT = "ab/acknowledgement"

        const val EXPERIMENT_NAME = "experiment_name"
        const val EXPERIMENT_VARIANT = "experiment_variant"
        const val EXPERIMENT_STATUS = "experiment_status"
        const val EXPERIMENT_TIME = "experiment_time"
        const val BUSINESS_ID = "business_id"

        enum class ExperimentAckStates(val value: Int) {
            STARTED(0),
        }
    }

    override fun clearLocalData(): Completable = clearAbData.get().execute()

    override fun isFeatureEnabled(feature: String, ignoreCache: Boolean, businessId: String?): Observable<Boolean> =
        getActiveBusinessId.get().thisOrActiveBusinessId(businessId) // TODO move this to a usecase
            .flatMapObservable { _businessId ->
                localSource.get().getProfile(_businessId, ignoreCache)
            }
            .doOnError {
                RecordException.recordException(it)
            }
            .map {
                if (it.features.containsKey(feature)) it.features[feature]
                else false
            }
            .distinctUntilChanged()
            .map { it }

    override fun enabledFeatures(businessId: String?): Observable<List<String>> =
        getActiveBusinessId.get().thisOrActiveBusinessId(businessId) // TODO move this to a usecase
            .flatMapObservable { localSource.get().getProfile(businessId = it) }
            .map { abProfile ->
                abProfile.features.filter { it.value }.keys.toList()
            }

    override fun isExperimentEnabled(experiment: String, businessId: String?): Observable<Boolean> {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId) // TODO move this to a usecase
            .flatMapObservable { localSource.get().getProfile(businessId = it) }
            .map {
                var enabled = false
                for ((key, value) in it.experiments) {

                    if (key.contains(experiment)) {
                        enabled = true
                    }
                }
                enabled
            }.doOnError {
                RecordException.recordException(it)
            }
    }

    override fun getProfile(businessId: String): Single<Profile> {
        return localSource.get().getProfileSingle(businessId)
    }

    override fun getExperimentVariant(name: String, businessId: String?): Observable<String> =
        getActiveBusinessId.get().thisOrActiveBusinessId(businessId) // TODO move this to a usecase
            .flatMapObservable { _businessId ->
                localSource.get().getProfile(_businessId)
                    .map { it.experiments[name]?.variant ?: "" }.doOnNext {
                        if (it.isNullOrEmpty().not()) {
                            startExperiment(name, _businessId)
                        }
                    }
            }

    override fun getVariantConfigurations(name: String, businessId: String?): Observable<Map<String, String>> =
        getActiveBusinessId.get().thisOrActiveBusinessId(businessId) // TODO move this to a usecase
            .flatMapObservable { localSource.get().getProfile(it) }
            .map { it.experiments[name]?.vars ?: mapOf() }

    internal fun startExperiment(name: String, businessId: String) {
        localSource.get().getProfile(businessId).firstOrError().flatMapCompletable { profile ->
            localSource.get().startedExperiments(businessId).firstOrError().flatMapCompletable { startedExperiments ->
                val variant = profile.experiments[name]?.variant
                if (startedExperiments.contains(name).not()) {
                    localSource.get().recordExperimentStarted(name, businessId).doOnComplete {
                        scheduleStartExperiment(
                            name,
                            variant ?: "",
                            ExperimentAckStates.STARTED.value,
                            DateTimeUtils.currentDateTime().millis,
                            businessId
                        )
                    }
                } else {
                    Completable.complete()
                }
            }
        }.onErrorComplete().subscribe()
    }

    override fun startLanguageExperiment(string_resource_id: String, businessId: String?) {
        // TODO move this to a usecase
        getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            localSource.get().getProfile(_businessId).firstOrError().flatMapCompletable { profile ->
                profile.experiments.entries.forEach {
                    if (it.value.vars.containsKey(string_resource_id)) {
                        val expName = it.key.replace(" ", "")
                        if (expName.contains(ABExperiments.LANGUAGE)) {
                            val experimentLanguage = expName.split("-")
                            if (localeManager.get().getLanguage() == experimentLanguage[1]) {
                                startExperiment(expName, _businessId)
                            }
                        }
                    }
                }
                Completable.complete()
            }
        }.onErrorComplete().subscribe()
    }

    internal fun acknowledgeExperiment(
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
        businessId: String?,
    ): Completable {
        return acknowledgeExperiment.get()
            .execute(experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId)
    }

    override fun sync(businessId: String?, sourceType: String): Completable {
        return syncAbProfile.get().execute(businessId, sourceType)
    }

    private fun scheduleStartExperiment(name: String, variant: String, status: Int, time: Long, businessId: String) {
        val workName = "$WORKER_ACKNOWLEDGEMENT/$name"

        val workRequest = OneTimeWorkRequestBuilder<ExperimentAcknowledgeWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(EXPERIMENT_NAME, name)
                    .putString(EXPERIMENT_VARIANT, variant)
                    .putInt(EXPERIMENT_STATUS, status)
                    .putLong(EXPERIMENT_TIME, time)
                    .putString(BUSINESS_ID, businessId)
                    .build()
            )
            .build()

        workManager
            .get()
            .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
    }

    override fun scheduleSync(businessId: String, sourceType: String): Completable {
        Timber.i("scheduling 'sync ab profile'")

        return Completable.fromAction {

            val workRequest = OneTimeWorkRequestBuilder<AbSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        AbSyncWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_PROFILE)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
                .enableWorkerLogging()

            workManager
                .get()
                .schedule(WORKER_PROFILE, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            Timber.i("'sync ab profile' scheduled")
        }
    }

    @Deprecated(message = "Do not use this. This is a temporary work around for login")
    override fun setProfile(profile: Profile, businessId: String) = localSource.get().setProfile(profile, businessId)
}
