package tech.okcredit.userSupport

import androidx.work.*
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.userSupport.model.Help
import tech.okcredit.userSupport.model.HelpItem
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class SupportRepositoryImpl @Inject constructor(
    private val localSource: Lazy<SupportLocalSource>,
    private val remoteSource: Lazy<SupportRemoteSource>,
    private val workManager: OkcWorkManager,
) : SupportRepository {

    companion object {
        const val WORKER_PROFILE = "userSupport/help"
        const val TAG = "<<<<UserSupportSDK"
    }

    override fun getHelp(): Observable<List<Help>> {
        return localSource.get().getHelp()
            .doOnNext { Timber.d("$TAG Get Helps=$it") }
    }

    override fun getHelpItem(helpId: String): Observable<HelpItem> {
        return localSource.get().getHelp()
            .map {
                var returnItem: HelpItem? = null
                it.forEach {
                    it.help_items?.forEach {
                        if (it.id == helpId) {
                            returnItem = it
                        }
                    }
                }
                if (returnItem == null) {
                    throw NoHelpSessionFound()
                } else {
                    return@map returnItem
                }
            }
    }

    override fun getContextualHelp(displayType: String): Observable<Help> {
        return localSource.get().getHelp()
            .map { help ->
                var returnItem: Help? = null
                help.forEach {
                    if (it.display_type == displayType) {
                        returnItem = it
                    }
                }
                if (returnItem == null) {
                    throw NoHelpFound()
                } else {
                    return@map returnItem
                }
            }
    }

    override fun getContextualHelpIds(displayTypes: List<String>): Observable<List<String>> {
        return localSource.get().getHelp()
            .map { help ->
                help.filter { displayTypes.contains(it.display_type) }.map { it.id }
            }
    }

    override fun executeSyncEverything(language: String, businessId: String): Completable {
        return remoteSource.get().getHelp(language, businessId)
            .doOnSuccess { Timber.d("$TAG Get Help From Server = $it") }
            .doOnError {
                RecordException.recordException(it)
                Timber.d("$TAG Get Help Server Error= ${it.message}")
            }
            .flatMapCompletable {
                localSource.get().setHelp(it.sections)
            }
    }

    override fun scheduleSyncEverything(language: String, businessId: String): Completable {
        Timber.i("scheduling 'sync ab profile'")

        return Completable.fromAction {

            val workRequest = OneTimeWorkRequestBuilder<SupportSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putString("businessId", businessId)
                        .putString("language", language)
                        .build()
                )
                .addTag(WORKER_PROFILE)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager
                .schedule(WORKER_PROFILE, Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
            Timber.i("'sync ab profile' scheduled")
        }
    }
}
