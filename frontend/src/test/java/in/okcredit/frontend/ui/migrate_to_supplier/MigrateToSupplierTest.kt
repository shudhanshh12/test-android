package `in`.okcredit.frontend.ui.migrate_to_supplier

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.frontend.usecase.MigrateRelation
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.customer_ui.usecase.IsSupplierCreditEnabledCustomer
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class MigrateToSupplierTest {
    private val initialState: MoveToSupplierContract.State = mock()
    val customerId: String = "customerId"
    private val getCustomer: GetCustomer = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val migrateRelation: MigrateRelation = mock()
    private val navigator: MoveToSupplierContract.Navigator = mock()
    private val tracker: Tracker = mock()
    private val isSupplierCreditEnabledCustomer: IsSupplierCreditEnabledCustomer = mock()
    private lateinit var viewModel: MoveToSupplierViewModel

    private fun createViewModel() {
        viewModel = MoveToSupplierViewModel(
            initialState = initialState,
            customerId = customerId,
            getCustomer = getCustomer,
            getActiveBusinessId = { getActiveBusinessId },
            migrateRelation = migrateRelation,
            navigator = navigator,
            tracker = tracker,
            isSupplierCreditEnabledCustomer = { isSupplierCreditEnabledCustomer }
        )
    }

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val customer = Customer(
            "customerId",
            CLEAN.code,
            1,
            "mobile",
            "description",
            dt,
            1001L,
            100L,
            5,
            dt,
            dt,
            "accountUrl",
            "profileImage",
            "address",
            "email",
            101L,
            dt,
            true,
            dt,
            true,
            "lang",
            "reminderMode",
            true,
            true,
            Customer.State.ACTIVE,
            true,
            true
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

    @Test
    fun getCustomerTest() {
        createViewModel()
        whenever(getCustomer.execute(customerId)).thenReturn(Observable.just(customer))

        val testObserver = viewModel.state().test()
        viewModel.attachIntents(Observable.just(MoveToSupplierContract.Intent.Load))

        Truth.assertThat(
            testObserver.values().last() == initialState.copy(
                customer = customer,
                networkError = false,
                error = false
            )
        )
        testObserver.dispose()
    }
}
