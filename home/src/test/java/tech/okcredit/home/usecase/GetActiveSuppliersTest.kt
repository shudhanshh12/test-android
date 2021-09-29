// package tech.okcredit.home.usecase
//
// import `in`.okcredit.merchant.suppliercredit.SupplierCreditAPI
// import `in`.okcredit.shared.usecase.Result
// import com.google.common.truth.Truth
// import com.nhaarman.mockitokotlin2.mock
// import com.nhaarman.mockitokotlin2.whenever
// import dagger.Lazy
// import io.reactivex.Observable
// import org.junit.Before
// import org.junit.Test
// import tech.okcredit.account_chat_sdk.use_cases.IncludeChatUnreadCountWithNewActivityCountsForAllSuppliers
// import tech.okcredit.home.TestData.SUPPLIER_1
// import tech.okcredit.home.TestData.SUPPLIER_2
// import tech.okcredit.home.TestData.SUPPLIER_3
// import tech.okcredit.home.TestData.SUPPLIER_4
// import tech.okcredit.home.ui.homesearch.HomeConstants
//
//
// class GetActiveSuppliersTest {
//
//    private val supplierCreditAPI: SupplierCreditAPI = mock()
//
//    private val includeChatUnreadCountWithNewActivityCountsForAllSuppliers: IncludeChatUnreadCountWithNewActivityCountsForAllSuppliers =
//        mock()
//
//    private val getActiveSuppliers =
//        GetActiveSuppliers(
//            supplierCreditAPI,
//            includeChatUnreadCountWithNewActivityCountsForAllSuppliers,
//            mock(),
//            mock()
//        )
//
//    @Before
//    fun setup() {
//        whenever(
//            includeChatUnreadCountWithNewActivityCountsForAllSuppliers.execute(Unit)
//        ).thenReturn(Observable.just(Result.Success(hashMapOf())))
//    }
//
//    @Test
//    fun `should return all active suppliers if search is empty`() {
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1
//                )
//            )
//        )
//
//        val searchQuery = ""
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_NAME to searchQuery
//            )
//        ).test()
//
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_1
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return right list for sort=SORT_TYPE_NAME`() {
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val searchQuery = ""
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_NAME to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_3,
//                        SUPPLIER_4,
//                        SUPPLIER_2,
//                        SUPPLIER_1
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return right list for sort=SORT_TYPE_ABS_BALANCE`() {
//        val searchQuery = ""
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_ABS_BALANCE to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_1,
//                        SUPPLIER_2,
//                        SUPPLIER_4,
//                        SUPPLIER_3
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return right list for sort=SORT_TYPE_RECENT_ACTIVITY`() {
//        val searchQuery = ""
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_RECENT_ACTIVITY to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_1,
//                        SUPPLIER_2,
//                        SUPPLIER_3,
//                        SUPPLIER_4
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should return right list for sort=SORT_TYPE_LAST_PAYMENT`() {
//        val searchQuery = ""
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_LAST_PAYMENT to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_1,
//                        SUPPLIER_3,
//                        SUPPLIER_4,
//                        SUPPLIER_2
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//
//    @Test
//    fun `should filter by name 1`() {
//        val searchQuery = "Br"
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_LAST_PAYMENT to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_3,
//                        SUPPLIER_4
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//
//    @Test
//    fun `should filter by name 2`() {
//        val searchQuery = "Brandom"
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_LAST_PAYMENT to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should filter by phone 1`() {
//        val searchQuery = "9"
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_LAST_PAYMENT to searchQuery
//            )
//        ).test()
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_1,
//                        SUPPLIER_3,
//                        SUPPLIER_4
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//    @Test
//    fun `should filter by phone 2`() {
//        val searchQuery = "88888"
//
//        whenever(supplierCreditAPI.listActiveSuppliers()).thenReturn(
//            Observable.just(
//                listOf(
//                    SUPPLIER_1,
//                    SUPPLIER_2,
//                    SUPPLIER_3,
//                    SUPPLIER_4
//                )
//            )
//        )
//
//        val testObserver = getActiveSuppliers.execute(
//            GetActiveSuppliers.Request(
//                HomeConstants.SORT_TYPE_LAST_PAYMENT to searchQuery
//            )
//        ).test()
//
//
//        Truth.assertThat(
//            testObserver.values().last() == Result.Success(
//                GetActiveSuppliers.Response(
//                    listOf(
//                        SUPPLIER_2
//                    ),
//                    0
//                )
//            )
//        )
//
//        testObserver.dispose()
//    }
//
//
// }
