import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.Category
import `in`.okcredit.merchant.contract.NumberCheckResponse
import `in`.okcredit.merchant.server.BusinessRemoteServer
import `in`.okcredit.merchant.server.internal.ApiMessages
import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import `in`.okcredit.merchant.usecase.BusinessSyncer
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.ThreadUtils

class MerchantApiImplTest {
    private val localSource: BusinessLocalSource = mock()
    private val remoteServer: BusinessRemoteServer = mock()
    private val syncer: BusinessSyncer = mock()
    private val imageUploader: IUploadFile = mock()
    private val businessPreference: BusinessPreferences = mock()
    private val merchantAPIImpl =
        BusinessRepositoryImpl(
            { localSource },
            { remoteServer },
            { syncer },
            { imageUploader },
            { businessPreference },
        )

    companion object {
        private val mobile = "mobileNumber"
        private val numberCheckResponse = NumberCheckResponse()
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dateTime: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")
        private val merchant = Business(
            id = "merchant_id",
            name = "name",
            mobile = "mobile",
            createdAt = dateTime
        )
        private val category = Category(
            id = "categoryId",
            type = 101
        )
        private val bussinessType = BusinessType(
            id = "bussinessId"
        )
        private val businessId = "business-id"
        private val bussinessTypeList = listOf(bussinessType)
        private val categoryList = listOf(category)
    }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()

        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit
    }

    @Test
    fun `when checkNewNumberExists`() {
        // given
        whenever(remoteServer.checkNewNumber(mobile, businessId)).thenReturn(Single.just(numberCheckResponse))

        // when
        val result = merchantAPIImpl.checkNewNumberExists(mobile, businessId).test()

        // then
        result.assertValue(numberCheckResponse)
    }

    @Test
    fun `scheduleSyncMerchant return completable`() {
        // given
        whenever(syncer.scheduleSyncBusiness(businessId)).thenReturn(Completable.complete())
        // when
        val result = merchantAPIImpl.scheduleSyncBusiness(businessId).test()
        // then
        result.assertComplete()
    }

    @Test
    fun `executeSyncMerchant return completable`() {
        // given
        whenever(syncer.executeSyncBusiness(businessId)).thenReturn(Completable.complete())
        // when
        val result = merchantAPIImpl.executeSyncBusiness(businessId).test()
        // then
        result.assertComplete()
        verify(syncer).executeSyncBusiness(businessId)
    }

    @Test
    fun `scheduleSyncMerchantCategoriesAndBusinessTypes return completable`() {
        // given
        whenever(syncer.scheduleSyncBusinessCategoriesAndBusinessTypes(businessId)).thenReturn(Completable.complete())
        // when
        val result = merchantAPIImpl.scheduleSyncBusinessCategoriesAndBusinessTypes(businessId).test()
        // then
        result.assertComplete()
    }

    @Test
    fun `getMerchant return merchant`() {
        // given
        whenever(localSource.getBusiness(businessId)).thenReturn(Observable.just(merchant))
        // when
        val result = merchantAPIImpl.getBusiness(businessId).test()
        // then
        result.assertValue(merchant)
        verify(localSource).getBusiness(businessId)
    }

    @Test
    fun `getCategories return listCategory`() {
        // given
        whenever(localSource.getCategories()).thenReturn(Observable.just(categoryList))
        // when
        val result = merchantAPIImpl.getCategories().test()
        // then
        result.assertValue(categoryList)
    }

    @Test
    fun `getBusinessTypes return listOfBussinessType`() {
        // given
        whenever(localSource.getBusinessTypes()).thenReturn(Observable.just(bussinessTypeList))
        // when
        val result = merchantAPIImpl.getBusinessTypes().test()
        // then
        result.assertValue(bussinessTypeList)
    }

    @Test
    fun `clearLocalData should deleteMerchantTable`() {
        runBlocking {
            // given
            whenever(localSource.deleteBusinessTable()).thenReturn(Completable.complete())

            // when
            val result = merchantAPIImpl.clearLocalData().test()
            // then
            result.assertComplete()
            verify(localSource).deleteBusinessTable()
            verify(businessPreference).clear()
        }
    }

    @Test
    fun `refreshLanguageInCategories should schedule sync merchant and categories and business types`() {
        // given
        whenever(syncer.scheduleSyncBusiness(businessId)).thenReturn(Completable.complete())
        whenever(syncer.scheduleSyncBusinessCategoriesAndBusinessTypes(businessId)).thenReturn(Completable.complete())
        // when
        val result = merchantAPIImpl.refreshLanguageInCategories(businessId).test()
        // then
        result.assertComplete()
        verify(syncer).scheduleSyncBusiness(businessId)
        verify(syncer).scheduleSyncBusinessCategoriesAndBusinessTypes(businessId)
    }

    @Test
    fun `verify the value of server is Merchant is Activated`() {
        runBlocking {
            whenever(localSource.getBusinessActivated()).thenReturn(false)
            whenever(remoteServer.isMerchantActivated()).thenReturn(ApiMessages.IsMerchantActivated(true))

            val result = merchantAPIImpl.isMerchantActivated()

            Truth.assertThat(result).isEqualTo(true)
        }
    }

    @Test
    fun `verify the value of server is Merchant is not Activated`() {
        runBlocking {
            whenever(localSource.getBusinessActivated()).thenReturn(false)
            whenever(remoteServer.isMerchantActivated()).thenReturn(ApiMessages.IsMerchantActivated(false))

            val result = merchantAPIImpl.isMerchantActivated()

            Truth.assertThat(result).isEqualTo(false)
        }
    }

    @Test
    fun `verify the value of store is true than method won't call server`() {
        runBlocking {
            whenever(localSource.getBusinessActivated()).thenReturn(true)

            val result = merchantAPIImpl.isMerchantActivated()

            verify(remoteServer, never()).isMerchantActivated()
            Truth.assertThat(result).isEqualTo(true)
        }
    }

    @Test
    fun `should return default value true from store returned when api call fails returned null`() {
        runBlocking {
            whenever(remoteServer.isMerchantActivated()).thenReturn(null)
            whenever(localSource.getBusinessActivated()).thenReturn(true)

            val testObserver = merchantAPIImpl.isMerchantActivated()

            Truth.assertThat(testObserver).isEqualTo(true)
        }
    }

    @Test
    fun `should return default value false from store returned when api call fails returned null`() {
        runBlocking {
            whenever(remoteServer.isMerchantActivated()).thenReturn(null)
            whenever(localSource.getBusinessActivated()).thenReturn(false)

            val testObserver = merchantAPIImpl.isMerchantActivated()

            Truth.assertThat(testObserver).isEqualTo(false)
        }
    }
}
