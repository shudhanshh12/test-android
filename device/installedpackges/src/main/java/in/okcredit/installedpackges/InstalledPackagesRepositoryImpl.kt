package `in`.okcredit.installedpackges

import `in`.okcredit.installedpackges.data.InstalledPackagesPreference
import `in`.okcredit.installedpackges.data.InstalledPackagesPreference.Keys.PREF_INDIVIDUAL_INSTALLED_PKGS_LAST_SYNC_TIMESTAMP
import `in`.okcredit.installedpackges.server.InstalledPackagesServer
import `in`.okcredit.installedpackges.server.PackageInfo
import `in`.okcredit.installedpackges.server.PackageReport
import `in`.okcredit.installedpackges.server.UpdatedPackagesRequestBody
import `in`.okcredit.installedpackges.utils.InstalledPackagesUtils.getDaysDiffFrmTimestamps
import `in`.okcredit.installedpackges.utils.InstalledPackagesUtils.getTimestampFromString
import `in`.okcredit.installedpackges.worker.SyncInstalledPackagesWorker
import androidx.work.*
import androidx.work.ListenableWorker.Result
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class InstalledPackagesRepositoryImpl @Inject constructor(
    private val server: Lazy<InstalledPackagesServer>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val workManager: Lazy<OkcWorkManager>,
    private val ab: Lazy<AbRepository>,
    private val installedPackagesPreference: Lazy<InstalledPackagesPreference>,
) : InstalledPackagesRepository {

    companion object {
        const val WORKER_SYNC_INSTALLED_APPS = "sync_installed_apps_status"
        const val WORKER_RETRY_INTERVAL_SECONDS = 30L
        const val INSTALLED_PKG_FEATURE = "app_device_info"
    }

    override suspend fun getPackageListForTracking(businessId: String): `in`.okcredit.shared.usecase.Result<List<PackageInfo>> {
        return withContext(dispatcherProvider.get().io()) {
            try {
                val packageResponse = server.get().getPackageListForTracking(businessId)
                // need to save last tracked from server in case of reinstall of app we should have last sync time otherwise
                // keep on asking to server to sync
                if (packageResponse.lastTracked != null && packageResponse.lastTracked.isNotBlank()) {
                    installedPackagesPreference.get().set(
                        PREF_INDIVIDUAL_INSTALLED_PKGS_LAST_SYNC_TIMESTAMP,
                        getTimestampFromString(packageResponse.lastTracked),
                        Scope.Individual
                    )
                }
                `in`.okcredit.shared.usecase.Result.Success(packageResponse.packages)
            } catch (t: Exception) {
                `in`.okcredit.shared.usecase.Result.Failure<List<PackageInfo>>(t)
            }
        }
    }

    override suspend fun updatePackagesStatus(report: List<PackageReport>, businessId: String): Result {
        return withContext(dispatcherProvider.get().io()) {
            try {
                server.get().updatePackagesStatus(businessId, UpdatedPackagesRequestBody(report = report))
                // when successful update to server will save timestamp
                installedPackagesPreference.get()
                    .set(PREF_INDIVIDUAL_INSTALLED_PKGS_LAST_SYNC_TIMESTAMP, System.currentTimeMillis(), Scope.Individual)
                Result.success()
            } catch (t: Exception) {
                Result.retry()
            }
        }
    }

    override fun syncInstalledPackagesToServer(businessId: String): Completable {
        return rxCompletable {
            val isEnabled = ab.get().isFeatureEnabled(INSTALLED_PKG_FEATURE, businessId = businessId).awaitFirst()
            if (isEnabled.not()) return@rxCompletable
            val lastSyncTime = installedPackagesPreference.get()
                .getLong(PREF_INDIVIDUAL_INSTALLED_PKGS_LAST_SYNC_TIMESTAMP, Scope.Individual).first()
            // after every 15 days sync should start
            if (lastSyncTime == 0L || getDaysDiffFrmTimestamps(lastSyncTime, System.currentTimeMillis()) > 14) {
                val workRequest = OneTimeWorkRequestBuilder<SyncInstalledPackagesWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .addTag(WORKER_SYNC_INSTALLED_APPS)
                    .setInputData(workDataOf(SyncInstalledPackagesWorker.BUSINESS_ID to businessId))
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WORKER_RETRY_INTERVAL_SECONDS,
                        TimeUnit.SECONDS
                    )
                    .build()

                workManager.get()
                    .schedule(
                        WORKER_SYNC_INSTALLED_APPS,
                        Scope.Individual,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
            }
        }.subscribeOn(ThreadUtils.newThread())
    }

    override fun cleanInstalledPkgsLocalData() = installedPackagesPreference.get().clearInstalledPkgsPref()
}
