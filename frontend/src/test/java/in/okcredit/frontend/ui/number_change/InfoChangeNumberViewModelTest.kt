package `in`.okcredit.frontend.ui.number_change

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.usecase.GetActiveBusinessImpl
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class InfoChangeNumberViewModelTest {
    private val initialState: InfoChangeNumberContract.State = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val getActiveMerchant: GetActiveBusinessImpl = mock()
    private val navigator: InfoChangeNumberContract.Navigator = mock()
    private lateinit var infoChangeNumberViewModel: InfoChangeNumberViewModel

    private fun createViewModel() {
        infoChangeNumberViewModel = InfoChangeNumberViewModel(
            initialState = initialState,
            Lazy { checkNetworkHealth },
            getActiveMerchant,
            navigator
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
    }

    @Test
    fun checkNetworkHealthTest() {
        createViewModel()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapCompletable(Completable.complete()))

        val testObserver = infoChangeNumberViewModel.state().test()
        infoChangeNumberViewModel.attachIntents(Observable.just(InfoChangeNumberContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                networkError = false
            )
        )
        testObserver.dispose()
    }

    @Test
    fun getActiveMerchantTest() {
        createViewModel()
        whenever(getActiveMerchant.execute()).thenReturn(Observable.just(merchant))

        val testObserver = infoChangeNumberViewModel.state().test()
        infoChangeNumberViewModel.attachIntents(Observable.just(InfoChangeNumberContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                mobile = merchant.mobile
            )
        )
        testObserver.dispose()
    }
}
