package `in`.okcredit.frontend.ui.add_supplier_transaction

import `in`.okcredit.backend._offline.usecase.AddSupplierTransaction
import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.frontend.usecase.IsPasswordSet
import `in`.okcredit.individual.contract.PreferenceKey.FOUR_DIGIT_PIN
import `in`.okcredit.individual.contract.PreferenceKey.PAYMENT_PASSWORD
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.use_case.ExperimentCanShowMidCamera
import `in`.okcredit.merchant.usecase.GetActiveBusinessImpl
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import android.content.Context
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Before
import org.junit.Test
import tech.okcredit.contract.MerchantPrefSyncStatus

class AddSupplierTransactionFragmentPresenterTest {
    lateinit var testObserver: TestObserver<AddSupplierTransactionContract.State>
    lateinit var viewModel: AddSupplierTxnScreenViewModel
    private val initialState: AddSupplierTransactionContract.State = AddSupplierTransactionContract.State()
    private val isPasswordSet: IsPasswordSet = mock()
    private val context: Context = mock()
    private val addSupplierTransaction: AddSupplierTransaction = mock()
    private val getSupplier: GetSupplier = mock()
    private val getMerchantPreference: GetMerchantPreferenceImpl = mock()
    private val getActiveBusiness: GetActiveBusinessImpl = mock()
    private val navigator: AddSupplierTransactionContract.Navigator = mock()
    private val experimentCanShowMidCamera: ExperimentCanShowMidCamera = mock()
    private val merchantPrefSyncStatus: MerchantPrefSyncStatus = mock()

    fun createViewModel(initialState: AddSupplierTransactionContract.State) {
        viewModel = AddSupplierTxnScreenViewModel(
            initialState = initialState,
            supplierId = "accountId",
            txnType = 1,
            txnAmount = 106L,
            isPasswordSet = Lazy { isPasswordSet },
            addSupplierTransaction = Lazy { addSupplierTransaction },
            getSupplier = Lazy { getSupplier },
            getMerchantPreference = Lazy { getMerchantPreference },
            getActiveBusiness = Lazy { getActiveBusiness },
            navigator = Lazy { navigator },
            experimentCanShowMidCamera = Lazy { experimentCanShowMidCamera },
            merchantPrefSyncStatus = Lazy { merchantPrefSyncStatus },
        )
    }

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val supplierId = "accountId"
        val supplier = Supplier(
            id = "accountId",
            registered = true,
            deleted = true,
            createTime = dt,
            txnStartTime = 100L,
            name = "supplierName",
            mobile = "mobile number",
            address = "address",
            profileImage = "profileImage",
            balance = 10L,
            newActivityCount = 10L,
            lastActivityTime = null,
            lastViewTime = null,
            txnAlertEnabled = true,
            lang = null,
            syncing = true,
            lastSyncTime = null,
            addTransactionRestricted = true,
            state = Supplier.ACTIVE,
            blockedBySupplier = true,
            restrictContactSync = true
        )

        val merchant = Business(
            id = "id",
            name = "name",
            mobile = "mobile",
            createdAt = dt,
            updateCategory = true,
            updateMobile = true
        )
    }

    @Before
    fun setUp() {
        createViewModel(initialState)
        testObserver = TestObserver()
        viewModel.state().subscribe(testObserver)

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun checkSupplierData() {
        whenever(getSupplier.execute(supplierId)).thenReturn(UseCase.wrapObservable(Observable.just(supplier)))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isLoading = false,
                supplier = supplier,
                txType = 1
            )
        )

        testObserver.dispose()
    }

    @Test
    fun isPasswordSetTest() {
        whenever(isPasswordSet.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(true)))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isPasswordSet = true
            )
        )

        testObserver.dispose()
    }

    @Test
    fun SetIsFourdigitPinTest() {
        whenever(getMerchantPreference.execute(FOUR_DIGIT_PIN)).thenReturn(Observable.just("true"))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isFourDigitPin = true
            )
        )

        testObserver.dispose()
    }

    @Test
    fun getMerchantPreferenceTest() {
        whenever(getMerchantPreference.execute(PAYMENT_PASSWORD)).thenReturn(Observable.just("true"))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isPassWordEnable = true
            )
        )

        testObserver.dispose()
    }

    @Test
    fun experimentCanShowMidCameraTest() {

        whenever(experimentCanShowMidCamera.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(true)))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                canShowMidCamera = true
            )
        )

        testObserver.dispose()
    }

    @Test
    fun showAlertTest() {

        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.ShowAlert("msg")))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                isAlertVisible = true,
                alertMessage = "msg"
            )
        )

        testObserver.dispose()
    }

    @Test
    fun goToCustomerProfile() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.GoToCustomerProfile))

        Truth.assertThat(testObserver.values().contains(initialState))
        verify(navigator).gotoCustomerProfile(supplierId)

        testObserver.dispose()
    }

    @Test
    fun onLongPressBackSpaceTest() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnLongPressBackSpace))
        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                amountCalculation = "",
                amount = 0L,
                amountError = false
            )
        )
        testObserver.dispose()
    }

    @Test
    fun onChangeInputModeTest() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnChangeInputMode(1)))
        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                activeInputMode = 1,
                isIncorrectPassword = false
            )
        )
        testObserver.dispose()
    }

    @Test
    fun onChangeDate() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnChangeDate(dt)))
        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                date = dt
            )
        )
        testObserver.dispose()
    }

    @Test
    fun onDeleteImageTest() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnDeleteImage))
        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                imageLocal = null
            )
        )
        testObserver.dispose()
    }

    @Test
    fun onChangePasswordTest() {
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnChangePassword("password")))
        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                password = "password",
                isIncorrectPassword = false
            )
        )
        testObserver.dispose()
    }

    @Test
    fun onForgotPasswordClickedTest() {
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))
        viewModel.attachIntents(Observable.just(AddSupplierTransactionContract.Intent.OnForgotPasswordClicked))
        Truth.assertThat(testObserver.values().contains(initialState))
        navigator.gotoForgotPasswordScreen(merchant.mobile)
        testObserver.dispose()
    }
}
