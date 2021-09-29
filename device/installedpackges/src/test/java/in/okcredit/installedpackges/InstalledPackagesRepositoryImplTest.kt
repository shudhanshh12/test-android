package `in`.okcredit.installedpackges

import `in`.okcredit.installedpackges.InstalledPackagesTestData.PackageInfoList
import `in`.okcredit.installedpackges.data.InstalledPackagesPreference
import `in`.okcredit.installedpackges.server.InstalledPackagesServer
import `in`.okcredit.installedpackges.server.PackageInfo
import `in`.okcredit.installedpackges.server.PackageInfoResponse
import `in`.okcredit.installedpackges.server.PackageUpdateResponse
import `in`.okcredit.installedpackges.server.UpdatedPackagesRequestBody
import `in`.okcredit.shared.usecase.Result
import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.workmanager.OkcWorkManager

class InstalledPackagesRepositoryImplTest {

    private val server: InstalledPackagesServer = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val workManager: OkcWorkManager = mock()
    private val ab: AbRepository = mock()
    private val installedPackagesPreference: InstalledPackagesPreference = mock()
    private val businessId = InstalledPackagesTestData.BUSINESS_ID

    private val installedPackagesApiImplTest =
        InstalledPackagesRepositoryImpl(
            { server },
            { dispatcherProvider },
            { workManager },
            { ab },
            { installedPackagesPreference }
        )

    @Before
    fun setUp() {
        whenever(dispatcherProvider.io()).thenReturn(Unconfined)
    }

    @Test
    fun `getPackageListForTracking return non empty list`() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(server.getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)).thenReturn(
                    PackageInfoResponse(lastTracked = "", packages = PackageInfoList)
                )

                installedPackagesApiImplTest.getPackageListForTracking(businessId)

                assertThat(Result.Success(PackageInfoList))

                verify(server).getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)
            }
        }
    }

    @Test
    fun `getPackageListForTracking return  empty list and  last tracked == null `() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(server.getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)).thenReturn(
                    PackageInfoResponse(lastTracked = null, packages = listOf())
                )

                installedPackagesApiImplTest.getPackageListForTracking(businessId)

                assertThat(Result.Success(listOf<PackageInfo>()))

                verify(server).getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)
                verify(installedPackagesPreference, never()).set(
                    eq("installed_pkgs_last_sync_time"),
                    eq(1597987800000),
                    any()
                )
            }
        }
    }

    @Test
    fun `getPackageListForTracking return empty list`() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(server.getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)).thenReturn(
                    PackageInfoResponse(lastTracked = "", packages = listOf())
                )

                installedPackagesApiImplTest.getPackageListForTracking(businessId)

                assertThat(Result.Success(listOf<PackageInfo>()))

                verify(server).getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)
            }
        }
    }

    @Test(expected = Exception::class)
    fun `getPackageListForTracking throws error`() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(server.getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)).thenThrow(Exception())

                installedPackagesApiImplTest.getPackageListForTracking(businessId)

                assertThat(Result.Failure<List<PackageInfo>>(java.lang.Exception()))

                verify(server).getPackageListForTracking(InstalledPackagesTestData.BUSINESS.id)
            }
        }
    }

    @Test
    fun `updatePackagesStatus successfully update on server`() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(
                    server.updatePackagesStatus(
                        InstalledPackagesTestData.BUSINESS.id,
                        UpdatedPackagesRequestBody(
                            report = InstalledPackagesTestData.PackageReportList
                        )
                    )
                ).thenReturn(
                    PackageUpdateResponse(status = true)
                )

                installedPackagesApiImplTest.updatePackagesStatus(
                    InstalledPackagesTestData.PackageReportList,
                    businessId
                )

                assertThat(ListenableWorker.Result.success())

                verify(server).updatePackagesStatus(
                    InstalledPackagesTestData.BUSINESS.id,
                    UpdatedPackagesRequestBody(
                        report = InstalledPackagesTestData.PackageReportList
                    )
                )
            }
        }
    }

    @Test(expected = Exception::class)
    fun `updatePackagesStatus have error`() {
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(
                    server.updatePackagesStatus(
                        InstalledPackagesTestData.BUSINESS.id,
                        UpdatedPackagesRequestBody(
                            report = InstalledPackagesTestData.PackageReportList
                        )
                    )
                ).thenThrow(
                    Exception()
                )

                installedPackagesApiImplTest.updatePackagesStatus(
                    InstalledPackagesTestData.PackageReportList,
                    businessId
                )

                assertThat(ListenableWorker.Result.retry())

                verify(server).updatePackagesStatus(
                    InstalledPackagesTestData.BUSINESS.id,
                    UpdatedPackagesRequestBody(
                        report = InstalledPackagesTestData.PackageReportList
                    )
                )
            }
        }
    }

    @Test
    fun `clearLocalData return completable`() {

        whenever(installedPackagesPreference.clearInstalledPkgsPref())
            .thenReturn(Completable.complete())

        val testObserver = installedPackagesApiImplTest.cleanInstalledPkgsLocalData().test()

        testObserver.assertComplete()

        verify(installedPackagesPreference).clearInstalledPkgsPref()
    }
}
