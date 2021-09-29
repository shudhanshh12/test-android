package `in`.okcredit.installedpackges.server

import dagger.Lazy
import javax.inject.Inject

class InstalledPackagesServer @Inject constructor(private val api: Lazy<InstalledPackagesApiClient>) {

    suspend fun getPackageListForTracking(businessId: String) =
        api.get().getPackageListForTracking(businessId, businessId)

    suspend fun updatePackagesStatus(businessId: String, updatedPackagesRequestBody: UpdatedPackagesRequestBody) =
        api.get().updatePackagesStatus(businessId, updatedPackagesRequestBody, businessId)
}
