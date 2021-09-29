package `in`.okcredit.merchant.collection.server

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.merchant.collection.CollectionTestData
import `in`.okcredit.merchant.collection.server.internal.ApiClientRiskV2
import `in`.okcredit.merchant.collection.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.collection.server.internal.CollectionApiClient
import `in`.okcredit.merchant.collection.utils.Utils
import com.nhaarman.mockitokotlin2.mock
import io.mockk.*
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.ApiError

class CollectionRemoteSourceImplTest {
    private val collectionApiClient: CollectionApiClient = mockk()
    private val apiClientRiskV2: ApiClientRiskV2 = mockk()
    private val collectionServer = CollectionRemoteSource(
        collectionApiClient = { collectionApiClient },
        apiClientRiskV2 = { apiClientRiskV2 },
    )
    private val businessId = "businessId"

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        mockkStatic(Error::class)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getCollectionProfile() when api call unsuccessful and error code is not 404 but error message is address_not_found then return error`() {
        runBlocking {
            val request = ApiMessages.MerchantCollectionProfileRequest("")
            val serverResponse: ResponseBody = mock()

            coEvery {
                (collectionApiClient.getMerchantCollectionProfile(request, true, businessId))
            } throws HttpException(Response.error<ApiMessages.MerchantCollectionProfileResponse>(400, serverResponse))

            try {
                collectionServer.getCollectionProfiles(businessId)
            } catch (exception: Exception) {
                assert(exception is ApiError)
            }
            coVerify { (collectionApiClient).getMerchantCollectionProfile(request, true, businessId) }
        }
    }

    @Test
    fun `getCollectionProfils() when api call successful and destination is null then return address_not_found`() {
        runBlocking {
            val request = ApiMessages.MerchantCollectionProfileRequest("")
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse =
                ApiMessages.MerchantCollectionProfileResponse(
                    customers = null,
                    suppliers = null,
                    destination = null,
                    merchantId = "merchant_id",
                    merchantVpa = null,
                    eta = 0
                )
            val businessId = "businessId"
            coEvery {
                (collectionApiClient.getMerchantCollectionProfile(request, true, businessId))
            } returns mockResponse

            try {
                collectionServer.getCollectionProfiles(businessId)
            } catch (exception: Exception) {
                assert(exception is CollectionServerErrors.AddressNotFound)
            }
            coVerify { (collectionApiClient).getMerchantCollectionProfile(request, true, businessId) }
        }
    }

    @Test
    fun `getCollectionCustomerProfile() when api call successful then return response body`() {
        runBlocking {
            val finalResponse = CollectionCustomerProfile(
                accountId = "abc",
                qr_intent = null,
                linkId = "link_id",
                paymentIntent = false
            )

            val request = ApiMessages.CustomerCollectionProfileRequest("", "customer_id")
            val serverResponse = CollectionTestData.API_COLLECTION_CUSTOMER_PROFILE

            coEvery {
                (collectionApiClient.getCustomerCollectionProfile(request, businessId))
            } returns (serverResponse)

            mockkObject(ApiEntityMapper.COLLECTION_CUSTOMER_PROFILE)
            every { ApiEntityMapper.COLLECTION_CUSTOMER_PROFILE.convert(serverResponse) } returns finalResponse

            val response = collectionServer.getCollectionCustomerProfile("customer_id", businessId)
            assert(response == finalResponse)
            coVerify { (collectionApiClient).getCustomerCollectionProfile(request, businessId) }
        }
    }

    @Test
    fun `getCollectionCustomerProfile() when api call unsuccessful then return error`() {
        runBlocking {
            val request = ApiMessages.CustomerCollectionProfileRequest("", "customer_id")
            coEvery {
                (collectionApiClient.getCustomerCollectionProfile(request, businessId))
            } throws HttpException(Response.error<ApiMessages.CustomerCollectionProfileResponse>(400, mock()))
            try {
                collectionServer.getCollectionCustomerProfile("customer_id", businessId)
            } catch (exception: Exception) {
                println(exception)
                assert(exception is ApiError)
            }
        }
    }

    @Test
    fun `setActiveDestination() when api call successful then return response body`() {
        runBlocking {
            val collectionMerchantProfile =
                CollectionMerchantProfile("merchant_id", "name", "paymentAddress", "type", null)
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request = ApiMessages.SetActiveDestinationRequest("merchant_id", apiMessages)
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)

            collectionServer.setActiveDestination(
                collectionMerchantProfile,
                referralMerchant = "",
                async = false,
                businessId = businessId
            )

            coVerify { (collectionApiClient).setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `setActiveDestination() when api call unsuccessful and error code is not 400 then return error`() {
        runBlocking {
            val collectionMerchantProfile =
                CollectionMerchantProfile("merchant_id", "name", "paymentAddress", "type", null)
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request = ApiMessages.SetActiveDestinationRequest("merchant_id", apiMessages)
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)

            collectionServer.setActiveDestination(
                collectionMerchantProfile,
                async = false,
                referralMerchant = "",
                businessId = businessId
            )

            coVerify { collectionApiClient.setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `setActiveDestination() when api call unsuccessful and error code is 400 and message is "invalid_account_number" then return InvalidAccountNumber error`() {
        runBlocking {
            val collectionMerchantProfile =
                CollectionMerchantProfile("merchant_id", "name", "paymentAddress", "type", null)
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request = ApiMessages.SetActiveDestinationRequest("merchant_id", apiMessages)
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)

            try {
                collectionServer.setActiveDestination(
                    collectionMerchantProfile,
                    referralMerchant = "",
                    async = false,
                    businessId = businessId
                )
            } catch (exception: Exception) {
                exception is CollectionServerErrors.InvalidAccountNumber
            }

            coVerify { collectionApiClient.setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `setActiveDestination() when api call unsuccessful and error code is 400 and message is "invalid_account_number" then return InvalidIFSCcode error`() {
        runBlocking {

            val collectionMerchantProfile = CollectionMerchantProfile(
                "merchant_id",
                "name",
                "paymentAddress",
                "type",
                null
            )
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request = ApiMessages.SetActiveDestinationRequest("merchant_id", apiMessages)
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)
            try {
                collectionServer.setActiveDestination(
                    collectionMerchantProfile,
                    referralMerchant = "",
                    async = false,
                    businessId = businessId
                )
            } catch (exception: Exception) {
                exception is CollectionServerErrors.InvalidIFSCcode
            }
            coVerify { collectionApiClient.setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `setActiveDestination() when api call unsuccessful and error code is 400 and message is empty then return InvalidIFSCcode error`() {
        runBlocking {

            val collectionMerchantProfile = CollectionMerchantProfile(
                "merchant_id",
                "name",
                "paymentAddress",
                "type",
                null
            )
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request = ApiMessages.SetActiveDestinationRequest("merchant_id", apiMessages)
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)

            collectionServer.setActiveDestination(
                collectionMerchantProfile,
                referralMerchant = "",
                async = false,
                businessId = businessId
            )

            coVerify { collectionApiClient.setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `setActiveDestination() when api call unsuccessful and error code is 400 and message is null then return InvalidIFSCcode error`() {
        runBlocking {

            val collectionMerchantProfile =
                CollectionMerchantProfile(
                    "merchant_id",
                    "name",
                    "paymentAddress",
                    "type",
                    null
                )
            val apiMessages = ApiMessages.DestinationRequest(
                "",
                collectionMerchantProfile.name,
                collectionMerchantProfile.payment_address,
                collectionMerchantProfile.type
            )
            val request =
                ApiMessages.SetActiveDestinationRequest(
                    "merchant_id",
                    apiMessages
                )
            val mockResponse: ApiMessages.MerchantCollectionProfileResponse = mock()

            coEvery { collectionApiClient.setActiveDestination(request, businessId) } returns (mockResponse)
            collectionServer.setActiveDestination(
                collectionMerchantProfile,
                referralMerchant = "",
                async = false,
                businessId = businessId
            )

            coVerify { collectionApiClient.setActiveDestination(request, businessId) }
        }
    }

    @Test
    fun `getSupplierCollectionProfile() when api call successful then return response body`() {
        runBlocking {

            val request = ApiMessages.GetSupplierCollectionProfileRequest("account_id")
            val serverResponse = CollectionTestData.API_SUPPLIER_COLLECTION_PROFILE

            val finalResponse: CollectionCustomerProfile = mock()

            coEvery {
                collectionApiClient.getSupplierCollectionProfile(request, businessId)
            } returns (serverResponse)

            mockkObject(ApiEntityMapper.COLLECTION_SUPPLIER_PROFILE)
            every {
                ApiEntityMapper.COLLECTION_SUPPLIER_PROFILE.convert(
                    serverResponse
                )
            } returns finalResponse

            collectionServer.getCollectionSupplierProfile(
                "account_id",
                businessId = businessId
            )
            coVerify {
                collectionApiClient.getSupplierCollectionProfile(request, businessId)
            }
        }
    }

    @Test
    fun `getSupplierCollectionProfile() when api call unsuccessful then return error`() {
        runBlocking {
            val request =
                ApiMessages.GetSupplierCollectionProfileRequest(
                    "account_id"
                )
            val serverResponse: ApiMessages.SupplierCollectionProfileResponse = mock()

            coEvery {
                collectionApiClient.getSupplierCollectionProfile(request, businessId)
            } returns (serverResponse)
            val testObserver =
                collectionServer.getCollectionSupplierProfile(
                    "account_id",
                    businessId = businessId
                )

            coVerify { collectionApiClient.getSupplierCollectionProfile(request, businessId) }
        }
    }

    @Test
    fun `getOnlinePaymentsList() when api call successful then return response body`() {
        runBlocking {
            val timestamp = 1593001740000L
            val request = ApiMessages.GetOnlinePaymentsRequest(timestamp)
            val onlinePayments: List<CollectionOnlinePayment> = listOf(mock(), mock())
            val onlinePaymentsAPI: List<ApiMessages.CollectionOnlinePaymentApi> = listOf(
                CollectionTestData.API_ONLINE_COLLECTION,
                CollectionTestData.API_ONLINE_COLLECTION,
                CollectionTestData.API_ONLINE_COLLECTION,
            )
            val mockResponse = ApiMessages.GetOnlinePaymentResponse(onlinePaymentsAPI)
            coEvery {
                collectionApiClient.getOnlinePaymentList(
                    ApiMessages.GetOnlinePaymentsRequest(timestamp),
                    businessId = businessId
                )
            } returns (mockResponse)

            mockkStatic(Utils::class)
            every {
                Utils.mapList(
                    onlinePaymentsAPI,
                    ApiEntityMapper.ONLINE_PAYMENT_MAPPER
                )
            } returns onlinePayments

            collectionServer.getOnlinePaymentsList(
                timestamp,
                businessId = businessId
            )
            coVerify { collectionApiClient.getOnlinePaymentList(request, businessId) }
        }
    }

    @Test
    fun `getOnlinePaymentsList() when api call unsuccessful then return error`() {
        runBlocking {
            val timestamp = 1593001740000L
            val request =
                ApiMessages.GetOnlinePaymentsRequest(
                    timestamp
                )

            coEvery {
                collectionApiClient.getOnlinePaymentList(
                    ApiMessages.GetOnlinePaymentsRequest(timestamp),
                    businessId = businessId
                )
            } throws HttpException(Response.error<ApiMessages.GetOnlinePaymentResponse>(400, mock()))
            try {
                collectionServer.getOnlinePaymentsList(
                    timestamp,
                    businessId = businessId
                )
            } catch (exception: Exception) {
                assert(exception is ApiError)
            }
            coVerify { collectionApiClient.getOnlinePaymentList(request, businessId) }
        }
    }

    @Test
    fun `tagMerchantPaymentWithCustomer() when api call successful return complete`() {
        runBlocking {
            val request =
                ApiMessages.TagMerchantPaymentRequest(
                    "customer_id",
                    "payment_id"
                )
            coJustRun {
                collectionApiClient.tagMerchantPaymentWithCustomer(request, businessId)
            }

            collectionServer.tagMerchantPaymentWithCustomer(
                "customer_id",
                "payment_id",
                businessId = businessId
            )
            coVerify {
                collectionApiClient.tagMerchantPaymentWithCustomer(request, businessId)
            }
        }
    }

    @Test
    fun `tagMerchantPaymentWithCustomer() when api call returns error`() {
        runBlocking {
            val request = ApiMessages.TagMerchantPaymentRequest(
                "customer_id",
                "payment_id"
            )
            coEvery {
                collectionApiClient.tagMerchantPaymentWithCustomer(request, businessId)
            } throws HttpException(Response.error<Unit>(400, mock()))

            try {
                collectionServer.tagMerchantPaymentWithCustomer(
                    "customer_id",
                    "payment_id",
                    businessId = businessId
                )
            } catch (exception: Exception) {
                assert(exception is ApiError)
            }

            coVerify { collectionApiClient.tagMerchantPaymentWithCustomer(request, businessId) }
        }
    }

    @Test
    fun `getKycRiskAttributes() when api call successful then return response body`() {
        runBlocking {
            val merchantId = "merchant_id"
            val riskCategory = "MEDIUM"
            val request = ApiMessages.GetRiskAttributesRequest(
                businessId
            )
            val mockResponse =
                ApiMessages.GetRiskAttributesResponse(
                    mock(),
                    riskCategory,
                    mock()
                )
            coEvery {
                apiClientRiskV2.getRiskAttributes(
                    request,
                    businessId
                )
            } returns (mockResponse)

            val testObserver =
                collectionServer.getKycRiskAttributes(businessId)
            assert(testObserver == mockResponse)
            coVerify { apiClientRiskV2.getRiskAttributes(request, businessId) }
        }
    }

    @Test
    fun `getKycRiskAttributes() when api call unsuccessful then return error`() {
        runBlocking {
            val request = ApiMessages.GetRiskAttributesRequest(businessId)
            val response: ApiMessages.GetRiskAttributesResponse = mock()
            coEvery {
                apiClientRiskV2.getRiskAttributes(
                    request,
                    businessId
                )
            } returns response
            try {
                collectionServer.getKycRiskAttributes(businessId)
            } catch (exception: Exception) {
            }

            coVerify { apiClientRiskV2.getRiskAttributes(request, businessId) }
        }
    }

    @Test
    fun `getBlindPayLinkId when api call return success`() {
        runBlocking {
            val accountId = "asdf12345fdad"
            val response: ApiMessages.BlindPayCreateLinkResponse = mock()
            val request = ApiMessages.BlindPayCreateLinkRequest(accountId)

            coEvery {
                collectionApiClient.getBlindPayLink(request, businessId)
            } returns (response)

            val testObservable = collectionServer.getBlindPayLinkId(accountId, businessId)
            assert(testObservable == response)
            coVerify {
                collectionApiClient.getBlindPayLink(request, businessId)
            }
        }
    }

    @Test
    fun `getBlindPayShareLink when api call return success`() {
        runBlocking {
            val response: ApiMessages.BlindPayShareLinkResponse = mock()

            coEvery {
                collectionApiClient.getBlindPayShareLink(
                    CollectionTestData.BLIND_PAY_SHARE_LINK_WITH_PAYMENT_ID_REQUEST, businessId
                )
            } returns (response)

            val testObservable = collectionServer.getBlindPayShareLink(CollectionTestData.PAYMENT_ID, businessId)
            assert(testObservable == response)
            coVerify {
                collectionApiClient.getBlindPayShareLink(
                    CollectionTestData.BLIND_PAY_SHARE_LINK_WITH_PAYMENT_ID_REQUEST,
                    businessId
                )
            }
        }
    }
}
