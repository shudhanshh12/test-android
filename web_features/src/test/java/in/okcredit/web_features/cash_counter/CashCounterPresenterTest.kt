package `in`.okcredit.web_features.cash_counter

class CashCounterPresenterTest {

//    private lateinit var viewModel: CashCounterPresenter
//
//    private val initialState: CashCounterContract.State = mock()
//    private val merchantAPI: MerchantApi = mock()
//    private val authService: AuthService = mock()
//
//    @Before
//    fun setup() {
//        viewModel = CashCounterPresenter(
//            initialState = initialState,
//            merchantAPI = merchantAPI,
//            authService = authService
//        )
//
//        mockkStatic(Schedulers::class)
//        every { Schedulers.newThread() } returns Schedulers.trampoline()
//    }
//
//    @Test
//    fun `load merchant id`() {
//        val merchant: Merchant = mock()
//        whenever(merchantAPI.getMerchant()).thenReturn(Observable.just(merchant))
//
//        viewModel.attachIntents(Observable.just(CashCounterContract.Intent.Load))
//
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState.copy(merchantId = merchant.id))
//    }
//
//    @Test
//    fun `load auth token`() {
//        whenever(authService.getAuthToken()).thenReturn("auth")
//
//        viewModel.attachIntents(Observable.just(CashCounterContract.Intent.Load))
//
//        val testObserver = viewModel.state().test()
//        testObserver.values().contains(initialState.copy(authToken = "auth"))
//    }
}
