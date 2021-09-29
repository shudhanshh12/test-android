package `in`.okcredit.merchant.customer_ui.ui.add_discount

import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.TestViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.base.preferences.DefaultPreferences

class AddDiscountViewModelTest :
    TestViewModel<AddDiscountContract.State, AddDiscountContract.PartialState, AddDiscountContract.ViewEvent>() {

    private val customerId: String = TestData.CUSTOMER.id
    private val txType = Transaction.CREDIT

    private val isPasswordSet: IsPasswordSet = mock()
    private val getCustomer: GetCustomer = mock()
    private val getMerchantPreference: GetMerchantPreferenceImpl = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val ab: AbRepository = mock()
    private val rxSharedPreference: DefaultPreferences = mock()

    @After
    fun teardown() {
        // reset date time
        DateTimeUtils.setCurrentMillisSystem()
    }

    @Test
    fun `change date`() {
        // provide intent
        pushIntent(AddDiscountContract.Intent.OnChangeDate(TestData.CURRENT_TIME))

        // expectations
        assertLastValue { it.date == TestData.CURRENT_TIME }
    }

    @Test
    fun `on forgot password should navigate to on forgot password navigation`() {
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(TestData.MERCHANT))
        // setup
        val initialState = lastState()

        pushIntent(AddDiscountContract.Intent.OnForgotPasswordClicked)

        assertLastViewEvent<AddDiscountContract.ViewEvent.GoToForgotPasswordScreen>()
        assertLastState(initialState)
    }

    @Test
    fun `customer profile should navigate to customer profile screen navigation`() {
        // setup
        val initialState = lastState()

        // provide intent
        pushIntent(AddDiscountContract.Intent.GoToCustomerProfile)

        assertLastViewEvent<AddDiscountContract.ViewEvent.GoToCustomerProfile>()
        assertLastState(initialState)
    }

    override fun createViewModel() = AddDiscountViewModel(
        initialState = AddDiscountContract.State(),
        customerId = customerId,
        txnType = txType,
        txnAmount = 0L,
        getCustomer = { getCustomer },
        getMerchantPreference = { getMerchantPreference },
        getActiveBusiness = { getActiveBusiness },
        isPasswordSet = { isPasswordSet },
        voiceId = "",
        inputType = "",
        voiceAmount = 0L,
        rxSharedPreference = { rxSharedPreference },
        addDiscount = mock(),
    )
}
