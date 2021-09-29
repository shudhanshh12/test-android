package `in`.okcredit.installedpackges.worker

import `in`.okcredit.installedpackges.InstalledPackagesRepository
import `in`.okcredit.installedpackges.server.PackageInfo
import `in`.okcredit.installedpackges.server.PackageReport
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import tech.okcredit.android.base.extensions.isAppPackageInstalled
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import javax.inject.Inject

class SyncInstalledPackagesWorker constructor(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val installedPackagesRepository: InstalledPackagesRepository,
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val BUSINESS_ID = "business_id"
    }

    override suspend fun doWork(): Result {
        return try {
            val businessId = inputData.getString(BUSINESS_ID)!!
            if (runAttemptCount < 3)
                updatePackagesInstalledInfoToServer(businessId)
            else
                Result.failure()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun updatePackagesInstalledInfoToServer(businessId: String): Result {
        when (val result = installedPackagesRepository.getPackageListForTracking(businessId)) {
            is `in`.okcredit.shared.usecase.Result.Success -> {
                if (result.value.isNotEmpty()) {
                    return installedPackagesRepository.updatePackagesStatus(
                        getUpdatedPackageReport(result.value),
                        businessId
                    )
                }
            }
            else -> return Result.failure()
        }

        return Result.failure()
    }

    private fun getUpdatedPackageReport(report: List<PackageInfo>): List<PackageReport> {
        val packageReport = mutableListOf<PackageReport>()
        report.forEach {
            packageReport.add(
                PackageReport(
                    packageId = it.packageId,
                    packageName = it.packageName,
                    isInstalled = context.isAppPackageInstalled(it.packageName)
                )
            )
        }

        return packageReport
    }

    class Factory @Inject constructor(
        private val installedPackagesRepository: InstalledPackagesRepository,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return SyncInstalledPackagesWorker(context, params, installedPackagesRepository)
        }
    }
}
