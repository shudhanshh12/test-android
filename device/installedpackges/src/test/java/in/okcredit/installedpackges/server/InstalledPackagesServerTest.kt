package `in`.okcredit.installedpackges.server

import `in`.okcredit.installedpackges.InstalledPackagesTestData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test

class InstalledPackagesServerTest {
    private val installedPackagesApiClient: InstalledPackagesApiClient = mock()
    private val businessId = InstalledPackagesTestData.BUSINESS.id

    private val installedPackagesServer =
        InstalledPackagesServer { installedPackagesApiClient }

    @Test
    fun `getPackageListForTracking return non empty list`() {
        runBlocking {
            whenever(installedPackagesApiClient.getPackageListForTracking(businessId, businessId)).thenReturn(
                PackageInfoResponse(lastTracked = "", packages = InstalledPackagesTestData.PackageInfoList)
            )

            installedPackagesServer.getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)

            assertThat(InstalledPackagesTestData.PackageInfoList)

            verify(installedPackagesApiClient).getPackageListForTracking(businessId, businessId)
        }
    }

    @Test
    fun `getPackageListForTracking return  empty list`() {
        runBlocking {
            whenever(installedPackagesApiClient.getPackageListForTracking(businessId, businessId)).thenReturn(
                PackageInfoResponse(lastTracked = "", packages = listOf())
            )

            installedPackagesServer.getPackageListForTracking(businessId)

            assertThat(listOf<PackageInfo>())

            verify(installedPackagesApiClient).getPackageListForTracking(businessId, businessId)
        }
    }

    @Test(expected = Exception::class)
    fun `getPackageListForTracking return error`() {
        runBlocking {
            whenever(installedPackagesApiClient.getPackageListForTracking(businessId, businessId)).thenThrow(
                java.lang.Exception()
            )

            installedPackagesServer.getPackageListForTracking(businessId)

            assertThat(java.lang.Exception())

            verify(installedPackagesApiClient).getPackageListForTracking(businessId, businessId)
        }
    }

    @Test
    fun `updatePackagesStatus successfully`() {
        runBlocking {
            whenever(
                installedPackagesApiClient.updatePackagesStatus(
                    businessId,
                    UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
                    businessId,
                )
            ).thenReturn(
                PackageUpdateResponse(status = true)
            )

            installedPackagesServer.updatePackagesStatus(
                businessId,
                UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
            )

            assertThat(PackageUpdateResponse(status = true))

            verify(installedPackagesApiClient).updatePackagesStatus(
                businessId,
                UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
                businessId,
            )
        }
    }

    @Test(expected = Exception::class)
    fun `updatePackagesStatus return error`() {
        runBlocking {
            whenever(
                installedPackagesApiClient.updatePackagesStatus(
                    businessId,
                    UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
                    businessId,
                )
            ).thenThrow(Exception())

            installedPackagesServer.updatePackagesStatus(
                businessId,
                UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
            )

            assertThat(Exception())

            verify(installedPackagesApiClient).updatePackagesStatus(
                businessId,
                UpdatedPackagesRequestBody(report = InstalledPackagesTestData.PackageReportList),
                businessId,
            )
        }
    }
}
