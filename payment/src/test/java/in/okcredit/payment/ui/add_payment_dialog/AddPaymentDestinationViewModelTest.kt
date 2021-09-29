package `in`.okcredit.payment.ui.add_payment_dialog
//
// import `in`.okcredit.collection.contract.CollectionCustomerProfile
// import `in`.okcredit.collection.contract.CollectionDestinationType
// import `in`.okcredit.collection.contract.IsUpiVpaValid
// import `in`.okcredit.frontend.FrontendTestData
// import `in`.okcredit.frontend.usecase.SetSupplierCollectionDestination
// import `in`.okcredit.merchant.collection.server.internal.common.CollectionServerErrors
// import `in`.okcredit.shared.usecase.Result
// import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
// import `in`.okcredit.supplier.usecase.GetSupplier
// import com.google.common.truth.Truth
// import com.google.firebase.crashlytics.FirebaseCrashlytics
// import com.nhaarman.mockitokotlin2.any
// import com.nhaarman.mockitokotlin2.doNothing
// import com.nhaarman.mockitokotlin2.mock
// import com.nhaarman.mockitokotlin2.verify
// import com.nhaarman.mockitokotlin2.whenever
// import io.mockk.every
// import io.mockk.mockkStatic
// import io.reactivex.Observable
// import io.reactivex.Single
// import io.reactivex.observers.TestObserver
// import io.reactivex.schedulers.Schedulers
// import org.junit.Before
// import org.junit.Test
// import tech.okcredit.android.auth.Unauthorized
// import tech.okcredit.base.network.NetworkError
//
// class AddPaymentDestinationViewModelTest {
//
//    lateinit var testObserverViewEvent: TestObserver<AddPaymentDestinationContract.ViewEvents>
//    lateinit var testObserver: TestObserver<AddPaymentDestinationContract.State>
//
//    private val initialState = AddPaymentDestinationContract.State()
//    private val supplierId: String = "supplier_id"
//    private val paymentMethodType: String = CollectionDestinationType.UPI.value
//    private val supplierAnalyticsEvents: SupplierAnalyticsEvents = mock()
//    private val setSupplierCollectionDestination: SetSupplierCollectionDestination = mock()
//    private val getSupplier: GetSupplier = mock()
//    private val isValidUpi: IsUpiVpaValid = mock()
//
//    private lateinit var viewModel: AddPaymentDestinationViewModel
//
//    @Before
//    fun setUp() {
//        viewModel = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            paymentMethodType,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//
//        viewModel.supplier = FrontendTestData.SUPPLIER
//
//        testObserver = viewModel.state().test()
//
//        testObserverViewEvent = viewModel.viewEvent().test()
//
//        mockkStatic(Schedulers::class)
//        every { Schedulers.newThread() } returns Schedulers.trampoline()
//        every { Schedulers.io() } returns Schedulers.trampoline()
//        every { Schedulers.single() } returns Schedulers.trampoline()
//
//        val firebaseCrashlytics: FirebaseCrashlytics = mock()
//        doNothing().whenever(firebaseCrashlytics).recordException(any())
//
//        mockkStatic(FirebaseCrashlytics::class)
//        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
//    }
//
//
//    @Test
//    fun `setBankDestinationObservable() success`() {
//        val collectionCustomerProfile: CollectionCustomerProfile = mock()
//
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = CollectionDestinationType.BANK.value
//                )
//            )
//        ).thenReturn(Observable.just(collectionCustomerProfile))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(upiLoaderStatus = false)
//        )
//
//        verify(supplierAnalyticsEvents).trackPaymentDetailsValidated(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            type = "BANK"
//        )
//        testObserverViewEvent.assertValue(
//            AddPaymentDestinationContract.ViewEvents.OnAccountAddedSuccessfully(
//                supplierId,
//                "bank"
//            )
//        )
//    }
//
//    @Test
//    fun `setBankDestinationObservable() failed with error InvalidAccountNumber`() {
//        val mockError = CollectionServerErrors.InvalidAccountNumber()
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = "bank"
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values().last() == initialState.copy(
//                isLoading = false,
//                invalidBankAccountError = true,
//                invalidBankAccountCode = 101,
//                upiLoaderStatus = false
//            )
//        )
//
//        verify(supplierAnalyticsEvents).trackEnteredInvalidPaymentDetails(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            error = "invalid_account_number",
//            type = "BANK"
//        )
//
//    }
//
//
//    @Test
//    fun `setBankDestinationObservable() failed with error InvalidIfscCode`() {
//        val mockError = CollectionServerErrors.InvalidIFSCcode()
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = CollectionDestinationType.BANK.value
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isLoading = false,
//                invalidBankAccountError = true,
//                invalidBankAccountCode = 102,
//                upiLoaderStatus = false
//            )
//        )
//
//        verify(supplierAnalyticsEvents).trackEnteredInvalidPaymentDetails(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            error = "invalid_ifsc",
//            type = "BANK"
//        )
//
//    }
//
//    @Test
//    fun `setBankDestinationObservable() failed with error InvalidName`() {
//        val mockError = CollectionServerErrors.InvalidName()
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = CollectionDestinationType.BANK.value
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isLoading = false,
//                invalidBankAccountError = true,
//                invalidBankAccountCode = 103,
//                upiLoaderStatus = false
//            )
//        )
//
//        verify(supplierAnalyticsEvents).trackEnteredInvalidPaymentDetails(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            error = "invalid_name",
//            type = "BANK"
//        )
//    }
//
//
//    @Test
//    fun `setBankDestinationObservable() failed with error InvalidAccountOrIFSCcode`() {
//        val mockError = CollectionServerErrors.InvalidAccountOrIFSCcode()
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = CollectionDestinationType.BANK.value
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isLoading = false,
//                invalidBankAccountError = true,
//                invalidBankAccountCode = 104,
//                upiLoaderStatus = false
//            )
//        )
//
//        verify(supplierAnalyticsEvents).trackEnteredInvalidPaymentDetails(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            error = "invalid_account_number_or_ifsc",
//            type = "BANK"
//        )
//    }
//
//    @Test
//    fun `setBankDestinationObservable() failed with error NetworkError`() {
//        val mockError = NetworkError("network_error", Throwable("network error"))
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = "bank"
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isNetworkError = true,
//                upiLoaderStatus = false
//            )
//        )
//    }
//
//    @Test
//    fun `setBankDestinationObservable() failed with other error`() {
//        val mockError = Exception("Some Error")
//
//        whenever(
//            setSupplierCollectionDestination.execute(
//                CollectionCustomerProfile(
//                    accountId = supplierId,
//                    paymentAddress = "payment@address",
//                    type = CollectionDestinationType.BANK.value
//                )
//            )
//        ).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ConfirmBankAccount("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState.copy(upiLoaderStatus = true))
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isLoading = false,
//                upiLoaderStatus = false,
//                errorMessage = mockError.localizedMessage
//            )
//        )
//    }
//
//
//    @Test
//    fun `isValidUpi returns true`() {
//        whenever(
//            isValidUpi.execute("payment@address")
//        ).thenReturn(Single.just(Pair(true, "")))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.SetPaymentVpa("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                upiErrorServer = false,
//                upiLoaderStatus = false
//            )
//        )
//    }
//
//
//    @Test
//    fun `isValidUpi returns false`() {
//        whenever(
//            isValidUpi.execute("payment@address")
//        ).thenReturn(Single.just(Pair(false, "")))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.SetPaymentVpa("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                upiErrorServer = true,
//                upiLoaderStatus = false
//            )
//        )
//    }
//
//    @Test
//    fun `isValidUpi() failed with network error`() {
//        val mockError = NetworkError("network_error", Throwable("network error"))
//
//        whenever(
//            isValidUpi.execute("payment@address")
//        ).thenReturn(Single.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.SetPaymentVpa("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                isNetworkError = true,
//                upiLoaderStatus = false
//            )
//        )
//    }
//
//    @Test
//    fun `isValidUpi() failed with InvalidAPaymentAddress`() {
//        val mockError = CollectionServerErrors.InvalidAPaymentAddress()
//        whenever(
//            isValidUpi.execute("payment@address")
//        ).thenReturn(Single.error(mockError))
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.SetPaymentVpa("payment@address")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values()
//                .last() == initialState.copy(
//                upiErrorServer = true,
//                upiLoaderStatus = false
//            )
//        )
//        verify(supplierAnalyticsEvents).trackEnteredInvalidPaymentDetails(
//            accountId = viewModel.supplier?.id ?: "",
//            mobile = viewModel.supplier?.mobile ?: "",
//            screen = "Payment Address Details",
//            relation = "Supplier",
//            flow = "Internal Supplier Collection",
//            error = "invalid_payment_address",
//            type = "UPI"
//        )
//    }
//
//    @Test
//    fun `ShareRequestToWhatsApp() success`() {
//
//        viewModel.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.ShareRequestToWhatsApp("sharing_text")))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        testObserverViewEvent.assertValue(AddPaymentDestinationContract.ViewEvents.ShareRequestToWhatsapp("sharing_text"))
//    }
//
//
//    @Test
//    fun `paymentMethodType is upi`() {
//
//        val addPaymentPresenter = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            CollectionDestinationType.UPI.value,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//        testObserver = addPaymentPresenter.state().test()
//        addPaymentPresenter.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.Load))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.UPI
//            )
//        )
//    }
//
//    @Test
//    fun `paymentMethodType is PAY_TM`() {
//
//        val addPaymentPresenter = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            CollectionDestinationType.PAY_TM.value,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//        testObserver = addPaymentPresenter.state().test()
//        addPaymentPresenter.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.Load))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.PAY_TM
//            )
//        )
//    }
//
//    @Test
//    fun `paymentMethodType is BANK`() {
//
//        val addPaymentPresenter = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            CollectionDestinationType.PAY_TM.value,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//        testObserver = addPaymentPresenter.state().test()
//        addPaymentPresenter.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.Load))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.BANK
//            )
//        )
//    }
//
//
//    @Test
//    fun `paymentMethodType is I_DONT_KNOW`() {
//
//        val addPaymentPresenter = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            CollectionDestinationType.PAY_TM.value,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//        testObserver = addPaymentPresenter.state().test()
//        addPaymentPresenter.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.Load))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.I_DONT_KNOW
//            )
//        )
//    }
//
//    @Test
//    fun `paymentMethodType is empty`() {
//
//        val addPaymentPresenter = AddPaymentDestinationViewModel(
//            initialState,
//            supplierId,
//            CollectionDestinationType.NONE.value,
//            { supplierAnalyticsEvents },
//            { setSupplierCollectionDestination },
//            { getSupplier },
//            { isValidUpi }
//        )
//        testObserver = addPaymentPresenter.state().test()
//        addPaymentPresenter.attachIntents(Observable.just(AddPaymentDestinationContract.Intent.Load))
//
//        // expectations
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.NONE
//            )
//        )
//    }
//
//    @Test
//    fun `SetAdoptionMode() to upi`() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.SetAdoptionMode(
//                    CollectionDestinationType.UPI
//                )
//            )
//        )
//
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.UPI
//            )
//        )
//    }
//
//    @Test
//    fun `SetAdoptionMode() to PAY_TM`() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.SetAdoptionMode(
//                    CollectionDestinationType.PAY_TM
//                )
//            )
//        )
//
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.PAY_TM
//            )
//        )
//    }
//
//    @Test
//    fun `SetAdoptionMode() to BANK`() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.SetAdoptionMode(
//                    CollectionDestinationType.BANK
//                )
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.BANK
//            )
//        )
//    }
//
//    @Test
//    fun `SetAdoptionMode() to I_DONT_KNOW`() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.SetAdoptionMode(
//                    CollectionDestinationType.I_DONT_KNOW
//                )
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.I_DONT_KNOW
//            )
//        )
//    }
//
//
//    @Test
//    fun `SetAdoptionMode() to NONE`() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.SetAdoptionMode(
//                    CollectionDestinationType.NONE
//                )
//            )
//        )
//
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                adoptionMode = CollectionDestinationType.NONE
//            )
//        )
//    }
//
//
//    @Test
//    fun `set intent to ClearUpiError `() {
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.ClearUpiError
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                upiErrorServer = false,
//                upiLoaderStatus = false
//            )
//        )
//    }
//
//    @Test
//    fun `getSupplier() execute successfully `() {
//        whenever(getSupplier.execute(supplierId)).thenReturn(Observable.just(Result.Success(FrontendTestData.SUPPLIER)))
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.Load
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().first() == initialState.copy(
//                supplier = FrontendTestData.SUPPLIER
//            )
//        )
//    }
//
//    @Test
//    fun `getSupplier() return  authError `() {
//        val mockError = Unauthorized()
//        whenever(getSupplier.execute(supplierId)).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.Load
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().last() == initialState
//        )
//    }
//
//    @Test
//    fun `getSupplier() return  network error `() {
//        val mockError = NetworkError("network_error", Throwable("network error"))
//        whenever(getSupplier.execute(supplierId)).thenReturn(Observable.error(mockError))
//
//        viewModel.attachIntents(
//            Observable.just(
//                AddPaymentDestinationContract.Intent.Load
//            )
//        )
//
//        Truth.assertThat(testObserver.values().first() == initialState)
//        Truth.assertThat(
//            testObserver.values().last() == initialState.copy(
//                isNetworkError = true
//            )
//        )
//    }
//
// }
