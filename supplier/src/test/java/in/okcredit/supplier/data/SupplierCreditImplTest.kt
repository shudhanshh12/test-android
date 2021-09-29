package `in`.okcredit.supplier.data

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.BusinessScopedPreferenceWithActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.ISyncer
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierLocalSource
import `in`.okcredit.merchant.suppliercredit.SupplierRemoteSource
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.accounting.contract.HomeSortType
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.concurrent.TimeUnit

class SupplierCreditImplTest {

    private val store: SupplierLocalSource = mock()
    private val server: SupplierRemoteSource = mock()
    private val syncer: ISyncer = mock()
    private val imageUploader: IUploadFile = mock()
    private val keyValService: KeyValService = mock()
    private val ab: AbRepository = mock()
    private val businessScopedPreferenceWithActiveBusinessId: BusinessScopedPreferenceWithActiveBusinessId = mock()
    private val defaultPreferences: DefaultPreferences = mock()
    private val isNetworkReminderEnabled: IsNetworkReminderEnabled = mock()

    private val supplierCreditApiImpl = SupplierCreditRepositoryImpl(
        { store },
        { server },
        { syncer },
        { imageUploader },
        { businessScopedPreferenceWithActiveBusinessId },
        { defaultPreferences },
        { ab },
        { isNetworkReminderEnabled }
    )

    companion object {
        private const val SUPPLIER_CREDIT = "supplier_credit"
        private val businessId = "business_Id"
        private val supplierId = "supplier_Id"
        private val updateName = "update_Name"
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")
        private val customerTxnStartTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(DateTime.now().millis)
        private val startTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(DateTime.now().millis)
        private val endTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(DateTime.now().millis)
        private val accountId = "account_Id"
        private val type = "type"
        private val txnId = "txnId"
        private val accountType = 101
        private val name = "SupplierName"
        private val mobile = "SupplierMobile"
        private val profileImage = "profileImage"
        private val supplier = Supplier(
            id = supplierId,
            createTime = dt,
            txnStartTime = TimeUnit.MILLISECONDS.toSeconds(dt.millis),
            name = "supplierName",
            mobile = "SupplierMobile",
            profileImage = "profileImage",
            restrictContactSync = false
        )
        private val transaction = Transaction(
            id = txnId,
            supplierId = supplierId,
            amount = 100,
            billDate = dt,
            createTime = dt,
            updateTime = dt,
            collectionId = "collectionId"
        )

        private val transactionList = listOf<Transaction>(transaction)

        private val supplierList = listOf<Supplier>(supplier)
    }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `when removeAllTransaction return completable`() {
        // given
        whenever(store.removeAllTransaction(supplierId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.removeAllTransaction(supplierId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when syncSuppliers return completable`() {
        // given
        whenever(syncer.syncAllSuppliers(businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.syncSuppliers(businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when syncSpecificSupplier return completable`() {
        // given
        whenever(syncer.syncSpecificSupplier(supplierId, businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.syncSpecificSupplier(supplierId, businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when syncEverything return completable`() {
        // given
        whenever(syncer.syncEverything(businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.syncEverything(businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when syncAllTransactions return completable`() {
        // given
        whenever(syncer.syncAllTransactions(businessId = businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.syncAllTransactions(businessId = businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when syncSupplierEnabledCustomerIds return completable`() {
        // given
        whenever(syncer.syncSupplierEnabledCustomerIds(businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.syncSupplierEnabledCustomerIds(businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when scheduleSyncSupplierEnabledCustomerIds return completable`() {
        // given
        whenever(syncer.scheduleSyncSupplierEnabledCustomerIds(businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.scheduleSyncSupplierEnabledCustomerIds(businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when executeSyncSupplierAndTransactions return completable`() {
        // given
        whenever(syncer.executeSyncSupplierAndTransactions(supplierId, businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.executeSyncSupplierAndTransactions(supplierId, businessId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when updateSupplerName return completable`() {
        // given
        whenever(store.updateSupplierName(updateName, supplierId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.updateSupplerName(updateName, supplierId).test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when listSupplierTransactionsBetweenBillDate return listOfTransactions`() {
        // given
        whenever(
            store.listSupplierTransactionsBetweenBillDate(
                supplierId, customerTxnStartTimeInMilliSec,
                startTimeInMilliSec, endTimeInMilliSec,
                businessId
            )
        ).thenReturn(Observable.just(transactionList))

        // when
        val result = supplierCreditApiImpl.listSupplierTransactionsBetweenBillDate(
            supplierId, customerTxnStartTimeInMilliSec,
            startTimeInMilliSec, endTimeInMilliSec,
            businessId
        ).test()

        // then
        result.assertValue(transactionList)
    }

    @Test
    fun `when getStatementUrl return listOfTransactions`() {
        // given
        whenever(
            server.getStatementUrl(
                accountId,
                type,
                accountType,
                startTimeInMilliSec,
                endTimeInMilliSec,
                businessId
            )
        ).thenReturn(Single.just("statementUrl"))

        // when
        val result = supplierCreditApiImpl.getStatementUrl(
            accountId, type, accountType, startTimeInMilliSec,
            endTimeInMilliSec,
            businessId
        ).test()

        // then
        result.assertValue("statementUrl")
    }

    // test
    @Test
    fun `when listActiveSuppliers return listOfActiveSuppliers`() {
        // given
        whenever(store.listActiveSuppliers(businessId)).thenReturn(Observable.just(supplierList))

        // when
        val result = supplierCreditApiImpl.listActiveSuppliers(businessId).test()

        // then
        result.assertValue(supplierList)
    }

    @Test
    fun `when getSuppliers return listOfActiveSuppliers`() {
        // given
        whenever(store.getSuppliers(businessId)).thenReturn(Observable.just(supplierList))

        // when
        val result = supplierCreditApiImpl.getSuppliers(businessId).test()

        // then
        result.assertValue(supplierList)
    }

    @Test
    fun `when getSupplier return listOfActiveSuppliers`() {
        // given
        whenever(store.getSupplier(supplierId, businessId)).thenReturn(Observable.just(supplier))
        whenever(syncer.scheduleSyncSupplier(supplierId, businessId)).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.getSupplier(supplierId, businessId).test()

        // then
        result.assertValue(supplier)
    }

    @Test
    fun `when listTransactions return listOfTransactions`() {
        // given
        whenever(store.listTransactions(supplierId, businessId)).thenReturn(Observable.just(transactionList))

        // when
        val result = supplierCreditApiImpl.listTransactions(supplierId, businessId).test()

        // then
        result.assertValue(transactionList)
    }

    @Test
    fun `when listTransactions based on txnStartTime return listOfTransactions`() {
        // given
        whenever(store.listTransactions(supplierId, startTimeInMilliSec, businessId)).thenReturn(Observable.just(transactionList))

        // when
        val result = supplierCreditApiImpl.listTransactions(supplierId, startTimeInMilliSec, businessId).test()

        // then
        result.assertValue(transactionList)
    }

    @Test
    fun `when listDirtyTransactions return listOfTransactions`() {
        // given
        whenever(store.listDirtyTransactions(businessId)).thenReturn(Observable.just(transactionList))

        // when
        val result = supplierCreditApiImpl.listDirtyTransactions(businessId).test()

        // then
        result.assertValue(transactionList)
    }

    @Test
    fun `when getTransaction return listOfTransactions`() {
        // given
        whenever(store.getTransaction(txnId, businessId)).thenReturn(Observable.just(transaction))

        // when
        val result = supplierCreditApiImpl.getTransaction(txnId, businessId).test()

        // then
        result.assertValue(transaction)
    }

    @Test
    fun `when addSupplier return string`() {
        // given
        whenever(server.addSupplier(name, mobile, profileImage, businessId)).thenReturn(Single.just(supplier))
        whenever(syncer.syncSupplier(supplierId, businessId)).thenReturn(Completable.complete())
        // when
        val result = supplierCreditApiImpl.addSupplier(name, mobile, profileImage, businessId).test()

        // then
        result.assertValue(supplier)
    }

    @Test
    fun `when reactivateSupplier return Complete`() {
        // given
        whenever(store.getSupplier(supplierId, businessId)).thenReturn(Observable.just(supplier))
        whenever(server.addSupplier(name, mobile, profileImage, businessId)).thenReturn(Single.just(supplier))
        whenever(syncer.syncSupplier(supplierId, businessId)).thenReturn(Completable.complete())
        // when
        val result = supplierCreditApiImpl.reactivateSupplier(supplierId, name, businessId).test()

        // then
        verify(server).addSupplier(name, mobile, profileImage, businessId)
        verify(syncer).syncSupplier(supplierId, businessId)
    }

    @Test
    fun `when deleteSupplier return Complete`() {
        // given
        whenever(store.removeAllTransaction(supplierId)).thenReturn(Completable.complete())
        whenever(server.deleteSupplier(supplierId, businessId)).thenReturn(Completable.complete())
        whenever(syncer.syncSupplier(supplierId, businessId)).thenReturn(Completable.complete())

        // when
        supplierCreditApiImpl.deleteSupplier(supplierId, businessId).test()

        // then
        verify(server).deleteSupplier(supplierId, businessId)
        verify(syncer).syncSupplier(supplierId, businessId)
        verify(store).removeAllTransaction(supplierId)
    }

    @Test
    fun `when syncTransaction return transactionId`() {
        // given
        whenever(syncer.syncDirtyTransaction(transaction.id, businessId)).thenReturn(Single.just(transaction.id))

        // when
        val result = supplierCreditApiImpl.syncTransaction(transaction, businessId).test()

        // then
        result.assertValue(transaction.id)
    }

    @Test
    fun `getSupplierByMobile`() {
        // given
        whenever(store.getSupplierByMobile(mobile, businessId)).thenReturn(Single.just(supplier))

        // when
        val result = supplierCreditApiImpl.getSupplierByMobile(mobile, businessId).test()

        // then
        result.assertValue(supplier)
    }

    @Test
    fun `clearLocalData`() {
        // given
        whenever(store.deleteSupplierTable()).thenReturn(Completable.complete())
        whenever(store.deleteTransactionTable()).thenReturn(Completable.complete())
        whenever(store.clearLastSyncEverythingTime()).thenReturn(Completable.complete())
        whenever(store.cancelWorker()).thenReturn(Completable.complete())
        whenever(store.clear()).thenReturn(Completable.complete())
        whenever(businessScopedPreferenceWithActiveBusinessId.delete(any(), any())).thenReturn(Completable.complete())

        // when
        val result = supplierCreditApiImpl.clearLocalData().test()

        // then
        result.assertComplete()
    }

    @Test
    fun `getSortType method should return the value returned by local source`() {
        val businessId = "businessId"
        whenever(store.getSortType(businessId)).thenReturn(Observable.just(HomeSortType.NAME))

        val type = supplierCreditApiImpl.getSortType(businessId).test()

        type.assertValue(HomeSortType.NAME)
    }

    @Test
    fun `setSortType method should return the value returned by local source`() {
        val businessId = "businessId"

        supplierCreditApiImpl.setSortType(HomeSortType.NAME, businessId)

        verify(store).setSortType(HomeSortType.NAME, businessId)
    }

    @Test
    fun `when feature is enabled sync network reminder and complete `() {
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(true))
        whenever(syncer.scheduleNotificationReminderSync(businessId)).thenReturn(Completable.complete())

        val result = supplierCreditApiImpl.syncNotificationReminder(businessId).test()

        result.assertComplete()

        verify(syncer, times(1)).scheduleNotificationReminderSync(businessId)
    }

    @Test
    fun `when feature is disable do not sync network reminder `() {
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(false))

        val result = supplierCreditApiImpl.syncNotificationReminder(businessId).test()

        result.assertComplete()

        verify(syncer, times(0)).scheduleNotificationReminderSync(businessId)
    }

    @Test
    fun `when account id given create notification reminder`() {
        val accountId = "asd1231314faadadsd"
        whenever(server.createNetworkReminder(accountId, businessId)).thenReturn(Single.just(true))

        val result = supplierCreditApiImpl.createNotificationReminder(accountId, businessId).test()
        result.assertValue(true)

        verify(server).createNetworkReminder(accountId, businessId)
    }
}
