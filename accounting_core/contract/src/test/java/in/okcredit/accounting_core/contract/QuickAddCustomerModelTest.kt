package `in`.okcredit.accounting_core.contract

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuickAddCustomerModelTest {

    @Test
    fun `should set default values for all properties except name`() {
        val model =
            QuickAddCustomerModel("Baburao Ganpatrao Apte")

        assertThat(model).isEqualTo(
            QuickAddCustomerModel(
                "Baburao Ganpatrao Apte",
                null,
                null,
                null,
                null,
                false
            )
        )
    }

    @Test
    fun `should return true when supplier id is not null or blank`() {
        val model = QuickAddCustomerModel(
            "Baburao Ganpatrao Apte",
            supplierId = "Star Garage"
        )

        val result = model.isSupplier()

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when supplier id is null`() {
        val model =
            QuickAddCustomerModel("Baburao Ganpatrao Apte")

        val result = model.isSupplier()

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when supplier id is blank`() {
        val model = QuickAddCustomerModel(
            "Baburao Ganpatrao Apte",
            supplierId = "   "
        )

        val result = model.isSupplier()

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when customer id is not null or blank`() {
        val model = QuickAddCustomerModel(
            "Baburao Ganpatrao Apte",
            customerId = "Raju"
        )

        val result = model.isExistingCustomer()

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when customer id is null`() {
        val model =
            QuickAddCustomerModel("Baburao Ganpatrao Apte")

        val result = model.isExistingCustomer()

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when customer id is blank`() {
        val model = QuickAddCustomerModel(
            "Baburao Ganpatrao Apte",
            customerId = "   "
        )

        val result = model.isExistingCustomer()

        assertThat(result).isFalse()
    }
}
