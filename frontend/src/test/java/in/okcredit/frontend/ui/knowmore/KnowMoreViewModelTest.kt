package `in`.okcredit.frontend.ui.knowmore

import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.frontend.ui.know_more.KnowMoreContract
import `in`.okcredit.frontend.ui.know_more.KnowMoreViewModel
import `in`.okcredit.frontend.usecase.GetKnowMoreVideoLinks
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.usecase.GetActiveBusinessImpl
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class KnowMoreViewModelTest {
    private lateinit var viewModel: KnowMoreViewModel
    private val initialState: KnowMoreContract.State = mock()
    private val id: String = "id"
    private val submitFeedback: SubmitFeedbackImpl = mock()
    private val getCustomer: GetCustomer = mock()
    private val getSupplier: GetSupplier = mock()
    private val accountType: String = "account_type"
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val getActiveBusiness: GetActiveBusinessImpl = mock()
    private val navigator: KnowMoreContract.Navigator = mock()
    private val getKnowMoreVideoLinks: GetKnowMoreVideoLinks = mock()

    private fun createViewModel() {
        viewModel = KnowMoreViewModel(
            initialState = initialState,
            id = id,
            submitFeedback = submitFeedback,
            getCustomer = getCustomer,
            getSupplier = getSupplier,
            accountType = accountType,
            checkNetworkHealth = Lazy { checkNetworkHealth },
            getActiveBusiness = getActiveBusiness,
            navigator = navigator,
            getKnowMoreVideoLinks = getKnowMoreVideoLinks
        )
    }

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val merchant = Business(
            id = "id",
            name = "name",
            mobile = "mobile",
            createdAt = dt,
            updateCategory = true,
            updateMobile = true
        )
        val commonLedgerVideoLinks = "commonLedgerVideoLinks"
        val commonLedgerSellerLinks = "commonLedgerSellerLinks"

        val knowMoreVideoLinksResponse = GetKnowMoreVideoLinks.Response(commonLedgerVideoLinks, commonLedgerSellerLinks)
    }

    @Test
    fun `on load`() {
        createViewModel()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(KnowMoreContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                networkError = false
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `setAccountTypeAndIDTest`() {
        createViewModel()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(KnowMoreContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                accountID = id,
                accountType = accountType
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `getActiveBusinessTest`() {
        createViewModel()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(KnowMoreContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                merchantName = merchant.name,
                merchantPic = merchant.profileImage
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `SetVideosTest`() {
        createViewModel()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))
        whenever(getKnowMoreVideoLinks.execute(Unit)).thenReturn(
            UseCase.wrapObservable(
                Observable.just(
                    knowMoreVideoLinksResponse
                )
            )
        )

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(KnowMoreContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                commonLedgerBuyerVideo = commonLedgerVideoLinks,
                commonLedgerSellerVideo = commonLedgerSellerLinks
            )
        )
        testObserver.dispose()
    }
}
