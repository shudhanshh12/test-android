package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.GetCustomerCollectionProfile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCustomerMenuOptionsTest {

    private lateinit var getCustomerMenuOptions: GetCustomerMenuOptions

    private val abRepository: AbRepository = mock()
    private val transactionRepo: TransactionRepo = mock()
    private val showCollectWithGPay: CanShowCollectWithGPay = mock()
    private val getCustomerCollectionProfile: GetCustomerCollectionProfile = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        getCustomerMenuOptions = GetCustomerMenuOptions(
            abRepository = { abRepository },
            transactionRepo = { transactionRepo },
            showCollectWithGPay = { showCollectWithGPay },
            getCustomerCollectionProfile = { getCustomerCollectionProfile },
            getActiveBusinessId = { getActiveBusinessId },
            getChatUnreadMessages = mock(),
        )

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
    }

    @Test
    fun `if empty transaction then only return help menu`() {
        mockListTransactions(emptyList())
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(googlePayEnabled = true))
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue {
                it.toolbarOptions.isEmpty() &&
                    it.menuOptions.size == 1 &&
                    it.menuOptions.first() is MenuOptionsBottomSheet.Companion.MenuOptions.Help &&
                    it.canShowContextualHelp
            }
            dispose()
        }
    }

    @Test
    fun `if transaction not empty toolbar shows call and customer statement`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(googlePayEnabled = true))
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue {
                it.menuOptions.isNotEmpty() &&
                    it.toolbarOptions.isNotEmpty() &&
                    it.menuOptions.first() is MenuOptionsBottomSheet.Companion.MenuOptions.Call &&
                    it.menuOptions[1] is MenuOptionsBottomSheet.Companion.MenuOptions.CustomerStatements &&
                    it.toolbarOptions.first() is MenuOptionsBottomSheet.Companion.MenuOptions.Call &&
                    it.toolbarOptions[1] is MenuOptionsBottomSheet.Companion.MenuOptions.CustomerStatements
            }
            dispose()
        }
    }

    @Test
    fun `if collection activated and qr_intent present then show qr code`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue {
                it.menuOptions.isNotEmpty() &&
                    it.toolbarOptions.isNotEmpty() &&
                    it.menuOptions[2] is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode &&
                    it.toolbarOptions[2] is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode
            }
            dispose()
        }
    }

    @Test
    fun `if collection not activated then dont show qr code`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )

        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode } == null &&
                    menuOptionsResponse.toolbarOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode } == null
            }
            dispose()
        }
    }

    @Test
    fun `if collection activated but qr_intent is null then dont show qr code`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(qr_intent = null))
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode } == null &&
                    menuOptionsResponse.toolbarOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.QrCode } == null
            }
            dispose()
        }
    }

    @Test
    fun `if chat enabled then show chat`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat } != null
            }
            dispose()
        }
    }

    @Test
    fun `if chat not enabled then don't add chat`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", false)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat } == null &&
                    menuOptionsResponse.toolbarOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat } == null
            }
            dispose()
        }
    }

    @Test
    fun `if qr code not present in toolbar then add chat`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat } != null &&
                    menuOptionsResponse.toolbarOptions[2] is MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat
            }
            dispose()
        }
    }

    @Test
    fun `if bill enabled then show bill`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.Bill } != null
            }
            dispose()
        }
    }

    @Test
    fun `if bill not enabled then don't add bill`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", false)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.Bill } == null &&
                    menuOptionsResponse.toolbarOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.Bill } == null
            }
            dispose()
        }
    }

    @Test
    fun `if qr code and chat not present in toolbar then add bill`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", false)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue {
                it.toolbarOptions[2] is MenuOptionsBottomSheet.Companion.MenuOptions.Bill
            }
            dispose()
        }
    }

    @Test
    fun `if subscription enabled then show subscription`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.Subscriptions } != null
            }
            dispose()
        }
    }

    @Test
    fun `if subscription not enabled then don't add subscription`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", false)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.Subscriptions } == null
            }
            dispose()
        }
    }

    @Test
    fun `if gpay enabled then show collect with gpay`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(true)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", true)
        mockFeature("subscription", true)
        mockFeature("accounts_chat", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.CollectWithGooglePay } != null
            }
            dispose()
        }
    }

    @Test
    fun `if gpay not enabled then don't add collect with gpay`() {
        mockListTransactions(
            listOf(
                TestData.TRANSACTION1,
                TestData.TRANSACTION2
            )
        )
        mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE)
        mockShowCollectWithGPay(false)
        mockFeature("accounts_chat", true)
        mockFeature("bill_manager", false)
        mockFeature("subscription", true)
        mockFeature("give_discount", true)
        getCustomerMenuOptions.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { menuOptionsResponse ->
                menuOptionsResponse.menuOptions.find { it is MenuOptionsBottomSheet.Companion.MenuOptions.CollectWithGooglePay } == null
            }
            dispose()
        }
    }

    private fun mockCustomerCollectionProfile(profile: CollectionCustomerProfile) {
        whenever(getCustomerCollectionProfile.execute(TestData.CUSTOMER.id)).thenReturn(
            Observable.create {
                it.onNext(profile)
            }
        )
    }

    private fun mockListTransactions(list: List<Transaction>) {
        whenever(transactionRepo.listTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)).thenReturn(
            Observable.create {
                it.onNext(list)
            }
        )
    }

    private fun mockFeature(name: String, enabled: Boolean) {
        whenever(abRepository.isFeatureEnabled(name)).thenReturn(
            Observable.create {
                it.onNext(enabled)
            }
        )
    }

    private fun mockShowCollectWithGPay(enabled: Boolean) {
        whenever(showCollectWithGPay.execute()).thenReturn(
            Observable.create {
                it.onNext(enabled)
            }
        )
    }
}
