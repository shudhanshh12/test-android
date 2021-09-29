package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.request.EditAction
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EditStaffCollectionLinkTest {

    private val customerRepositoryImpl: CustomerRepositoryImpl = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()
    private val businessId = "businessId"

    private val editStaffCollectionLink = EditStaffCollectionLink({ customerRepositoryImpl }, { getActiveBusinessId })

    @Test
    fun `when edit action given in execute with customer ids then just call repo with that actions`() {
        runBlocking {
            coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
            coJustRun {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_1", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }

            val customerIds = editStaffCollectionLink.execute(
                linkId = "link_id",
                customerIds = listOf("customer_1", "customer_4"),
                action = EditAction.ADD
            )

            coVerify {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_1", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }

            assert(customerIds == listOf("customer_1", "customer_4"))
        }
    }

    @Test
    fun `when any customer id not present in new set then call edit with delete`() {
        runBlocking {
            coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
            coJustRun {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.DELETE,
                    businessId,
                )
            }
            val customerIds = editStaffCollectionLink.execute(
                linkId = "link_id",
                currentCustomerIds = setOf("customer_1", "customer_2", "customer_3", "customer_4"),
                newCustomerIds = setOf("customer_1", "customer_3")
            )
            coVerify {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.DELETE,
                    businessId,
                )
            }
            assert(customerIds == setOf("customer_1", "customer_3"))
        }
    }

    @Test
    fun `when any customer id present in new set but not in current then call edit with add`() {
        runBlocking {
            coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
            coJustRun {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }
            val customerIds = editStaffCollectionLink.execute(
                linkId = "link_id",
                currentCustomerIds = setOf("customer_1", "customer_3"),
                newCustomerIds = setOf("customer_1", "customer_2", "customer_3", "customer_4"),
            )
            coVerify {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }
            assert(customerIds == setOf("customer_1", "customer_2", "customer_3", "customer_4"))
        }
    }

    @Test
    fun `when any customer ids change completely call edit with add and delete both`() {
        runBlocking {
            coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
            coJustRun {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }
            coJustRun {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_1", "customer_3"),
                    action = EditAction.DELETE,
                    businessId,
                )
            }
            val customerIds = editStaffCollectionLink.execute(
                linkId = "link_id",
                currentCustomerIds = setOf("customer_1", "customer_3"),
                newCustomerIds = setOf("customer_2", "customer_4"),
            )
            coVerify {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_2", "customer_4"),
                    action = EditAction.ADD,
                    businessId,
                )
            }
            coVerify {
                customerRepositoryImpl.editCollectionStaffLink(
                    linkId = "link_id",
                    customerIds = listOf("customer_1", "customer_3"),
                    action = EditAction.DELETE,
                    businessId,
                )
            }
            assert(customerIds == setOf("customer_2", "customer_4"))
        }
    }
}
