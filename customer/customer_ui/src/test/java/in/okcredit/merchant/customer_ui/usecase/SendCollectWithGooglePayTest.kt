package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.response.GooglePayPaymentResponse
import `in`.okcredit.payment.contract.model.PaymentAttributes
import `in`.okcredit.payment.contract.usecase.GetPaymentAttributeFromServer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.network.ApiError
import java.util.concurrent.TimeUnit

class SendCollectWithGooglePayTest {

    private val customerRepository: CustomerRepositoryImpl = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getPaymentAttributeFromServer: GetPaymentAttributeFromServer = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()

    private lateinit var sendCollectWithGooglePay: SendCollectWithGooglePay

    private val customerId = "customerId"
    private val mobile = "mobile"
    private val amount = 1000L

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
        sendCollectWithGooglePay = SendCollectWithGooglePay(
            customerRepository = { customerRepository },
            collectionRepository = { collectionRepository },
            getPaymentAttributeFromServer = { getPaymentAttributeFromServer },
            getActiveBusiness = { getActiveBusiness }
        )
    }

    @Test
    fun `throw error if link id is not present`() {
        val businessId = "business-id"
        val business = mock<Business>().apply {
            whenever(this.id).thenReturn(businessId)
            whenever(this.name).thenReturn("business-name")
        }
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
        whenever(collectionRepository.getCollectionCustomerProfile(customerId, businessId)).thenReturn(
            Observable.just(
                CollectionCustomerProfile(accountId = customerId, linkId = null)
            )
        )

        whenever(
            collectionRepository.updateGooglePayEnabledForCustomer(
                customerId,
                false
            )
        ).thenReturn(Completable.complete())

        sendCollectWithGooglePay.execute(customerId, mobile, amount).test().assertError {
            it is IllegalArgumentException
        }
    }

    @Test
    fun `successful API call updates the flag in customer collection profile`() {
        runBlocking {
            val businessId = "business-id"
            val businessName = "business-name"
            val business = mock<Business>().apply {
                whenever(this.id).thenReturn(businessId)
                whenever(this.name).thenReturn(businessName)
            }
            whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
            whenever(collectionRepository.getCollectionCustomerProfile(customerId, businessId)).thenReturn(
                Observable.just(
                    CollectionCustomerProfile(accountId = customerId, linkId = "link_id")
                )
            )

            whenever(getPaymentAttributeFromServer.execute("APP", "link_id")).thenReturn(
                Single.just(
                    PaymentAttributes("payment_id", "polling_type")
                )
            )

            whenever(
                customerRepository.initiateGooglePayPayment(
                    amount = amount,
                    mobile = mobile,
                    transactionId = "payment_id",
                    linkId = "link_id",
                    customerId = customerId,
                    businessName = businessName,
                    businessId = businessId
                )
            ).thenReturn(GooglePayPaymentResponse("200", "Success", ""))

            whenever(
                collectionRepository.updateGooglePayEnabledForCustomer(
                    customerId,
                    false
                )
            ).thenReturn(Completable.complete())

            val testObserver = sendCollectWithGooglePay.execute(customerId, mobile, amount).test()
            testObserver.await(1, TimeUnit.SECONDS)
            testObserver.assertComplete()
            verify(collectionRepository).updateGooglePayEnabledForCustomer(customerId, false)
            testObserver.dispose()
        }
    }

    @Test
    fun `api error throws error from use case`() {
        runBlocking {
            val businessId = "business-id"
            val businessName = "business-name"
            val business = mock<Business>().apply {
                whenever(this.id).thenReturn(businessId)
                whenever(this.name).thenReturn(businessName)
            }
            whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))
            whenever(collectionRepository.getCollectionCustomerProfile(customerId, businessId)).thenReturn(
                Observable.just(
                    CollectionCustomerProfile(accountId = customerId, linkId = "link_id")
                )
            )

            whenever(getPaymentAttributeFromServer.execute("APP", "link_id")).thenReturn(
                Single.just(
                    PaymentAttributes("payment_id", "polling_type")
                )
            )

            whenever(
                customerRepository.initiateGooglePayPayment(
                    amount = amount,
                    mobile = mobile,
                    transactionId = "payment_id",
                    linkId = "link_id",
                    customerId = customerId,
                    businessName = businessName,
                    businessId = businessId
                )
            ).thenThrow(ApiError(409))

            val testObserver = sendCollectWithGooglePay.execute(customerId, mobile, amount).test()
            testObserver.await(1, TimeUnit.SECONDS)
            testObserver.assertError {
                it is ApiError && (it.code == 409)
            }
            testObserver.dispose()
        }
    }
}
