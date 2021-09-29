package `in`.okcredit.installedpackges

import `in`.okcredit.installedpackges.server.PackageInfo
import `in`.okcredit.installedpackges.server.PackageReport
import androidx.work.ListenableWorker
import io.reactivex.Completable

interface InstalledPackagesRepository {

    suspend fun getPackageListForTracking(businessId: String): `in`.okcredit.shared.usecase.Result<List<PackageInfo>>

    suspend fun updatePackagesStatus(report: List<PackageReport>, businessId: String): ListenableWorker.Result

    fun syncInstalledPackagesToServer(businessId: String): Completable

    fun cleanInstalledPkgsLocalData(): Completable
}
