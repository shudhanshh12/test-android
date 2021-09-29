package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.collection.server.CollectionRemoteSource
import `in`.okcredit.merchant.collection.usecase.IsCollectionActivated
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class CollectionRepositoryImplTest {

    private val localSource: CollectionLocalSource = mock()
    private val remoteSource: CollectionRemoteSource = mockk()
    private val syncer: CollectionSyncer = mockk()
    private val isCollectionActivated: IsCollectionActivated = mock()
    private val repositoryImpl: CollectionRepositoryImpl = CollectionRepositoryImpl(
        localSource = { localSource },
        remoteSource = { remoteSource },
        syncer = { syncer },
        isCollectionActivated = { isCollectionActivated },
    )
    private val businessId = CollectionTestData.BUSINESS_ID

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getCollectionMerchantProfile() should fetch CollectionMerchantProfile from store`() {
        val profile = CollectionMerchantProfile(businessId)
        whenever(localSource.getCollectionMerchantProfile(businessId)).thenReturn(Observable.just(profile))
        val testObserver = repositoryImpl.getCollectionMerchantProfile(businessId).test()

        verify(localSource) {
            2 * { getCollectionMerchantProfile(businessId) }
        }

        testObserver.assertValue(profile)

        testObserver.dispose()
    }

    @Test
    fun `getPredictedCollectionMerchantProfile() should fetch CollectionMerchantProfile from server`() {
        runBlocking {
            val profile = CollectionMerchantProfile("121")
            coEvery {
                remoteSource.getPredictedCollectionMerchantProfile(CollectionTestData.BUSINESS_ID)
            } returns profile

            val testObserver =
                repositoryImpl.getPredictedCollectionMerchantProfile(CollectionTestData.BUSINESS_ID).test()

            coVerify { remoteSource.getPredictedCollectionMerchantProfile(CollectionTestData.BUSINESS_ID) }
            testObserver.assertValue(profile)

            testObserver.dispose()
        }
    }

    @Test
    fun `getCollectionCustomerProfile() should return CollectionCustomerProfile after fetching from server when qr_intent is empty`() {
        runBlocking {
            val collectionCustomerProfile = CollectionCustomerProfile(
                accountId = "account_id"
            )
            coJustRun { syncer.executeSyncCollectionProfileForCustomer(collectionCustomerProfile.accountId) }
            whenever(
                localSource.getCustomerCollectionProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Observable.just(collectionCustomerProfile))

            val testObserver = repositoryImpl.getCollectionCustomerProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            ).test()

            coVerify { (syncer).executeSyncCollectionProfileForCustomer(collectionCustomerProfile.accountId) }
            verify(localSource, times(2)).getCustomerCollectionProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            )
            testObserver.assertValue(collectionCustomerProfile)

            testObserver.dispose()
        }
    }

    @Test
    fun `getCollectionCustomerProfile() should return CollectionCustomerProfile after from store when qr_intent is not empty`() {
        runBlocking {

            val collectionCustomerProfile = CollectionCustomerProfile(
                accountId = "account_id", qr_intent = "qr_intent"
            )
            coEvery {
                (
                    remoteSource.getCollectionCustomerProfile(
                        collectionCustomerProfile.accountId,
                        CollectionTestData.BUSINESS_ID
                    )
                    )
            } returns collectionCustomerProfile
            whenever(
                localSource.getCustomerCollectionProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Observable.just(collectionCustomerProfile))
            whenever(
                localSource.putCustomerCollectionProfile(
                    collectionCustomerProfile,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Completable.complete())

            val testObserver =
                repositoryImpl.getCollectionCustomerProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                ).test()

            coVerify(exactly = 0) {
                (remoteSource).getCollectionCustomerProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            }
            verify(localSource, times(2)).getCustomerCollectionProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            )
            verify(localSource, times(0)).putCustomerCollectionProfile(
                collectionCustomerProfile,
                CollectionTestData.BUSINESS_ID
            )
            testObserver.assertValue(collectionCustomerProfile)

            testObserver.dispose()
        }
    }

    @Test
    fun `listCollectionCustomerProfiles() should fetch CollectionCustomerProfiles from store`() {
        val customerProfiles: List<CollectionCustomerProfile> = listOf(mock(), mock(), mock())
        whenever(localSource.listCustomerCollectionProfiles(CollectionTestData.BUSINESS_ID)).thenReturn(
            Observable.just(
                customerProfiles
            )
        )

        val testObserver = repositoryImpl.listCollectionCustomerProfiles(CollectionTestData.BUSINESS_ID).test()

        verify(localSource).listCustomerCollectionProfiles(CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(customerProfiles)

        testObserver.dispose()
    }

    @Test
    fun `listCollections() should fetch Collections from store when sync is done`() {
        val collections: List<Collection> = listOf(mock(), mock(), mock())
        whenever(localSource.listCollections(CollectionTestData.BUSINESS_ID)).thenReturn(Observable.just(collections))
        val testObserver = repositoryImpl.listCollections(CollectionTestData.BUSINESS_ID).test()

        verify(localSource).listCollections(CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(collections)

        testObserver.dispose()
    }

    @Test
    fun `getCollectionsOfCustomer() should fetch Collection from store when sync is done`() {
        val collections: List<Collection> = listOf(mock(), mock(), mock())

        whenever(localSource.listCollectionsOfCustomer("customer_id", CollectionTestData.BUSINESS_ID))
            .thenReturn(Observable.just(collections))

        val testObserver =
            repositoryImpl.getCollectionsOfCustomerOrSupplier("customer_id", CollectionTestData.BUSINESS_ID).test()

        verify(localSource).listCollectionsOfCustomer("customer_id", CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(collections)

        testObserver.dispose()
    }

    @Test
    fun `getCollection() should fetch Collection from server`() {
        val collection: Collection = mock()
        whenever(localSource.getCollection("collection_id", CollectionTestData.BUSINESS_ID)).thenReturn(
            Observable.just(
                collection
            )
        )

        val testObserver = repositoryImpl.getCollection("collection_id", CollectionTestData.BUSINESS_ID).test()

        verify(localSource).getCollection("collection_id", CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(collection)

        testObserver.dispose()
    }

    @Test
    fun `clearLocalData() should return Completable from store`() {
        whenever(localSource.clearCollectionSDK()).thenReturn(Completable.complete())

        val testObserver = repositoryImpl.clearLocalData().test()

        verify(localSource).clearCollectionSDK()
        testObserver.assertComplete()

        testObserver.dispose()
    }

    @Test
    fun `validatePaymentAddress() should return validation value from server`() {
        val pair: Pair<Boolean, String> = mock()
        coEvery {
            (remoteSource.validatePaymentAddress("address_type", "8882946897@ybl", CollectionTestData.BUSINESS_ID))
        } returns (pair)

        val testObserver =
            repositoryImpl.validatePaymentAddress("address_type", "8882946897@ybl", CollectionTestData.BUSINESS_ID)
                .test()

        coVerify {
            (remoteSource).validatePaymentAddress(
                "address_type",
                "8882946897@ybl",
                CollectionTestData.BUSINESS_ID
            )
        }
        testObserver.assertValue(pair)

        testObserver.dispose()
    }

    @Test
    fun `listCollectionShareInfos() should return Completable from store`() {
        runBlocking {
            val shareInfo: List<CollectionShareInfo> = mock()
            whenever(localSource.listCollectionShareInfos(CollectionTestData.BUSINESS_ID)).thenReturn(
                Observable.just(
                    shareInfo
                )
            )

            val testObserver = repositoryImpl.listCollectionShareInfos(CollectionTestData.BUSINESS_ID).test()

            verify(localSource).listCollectionShareInfos(CollectionTestData.BUSINESS_ID)
            testObserver.assertValue(shareInfo)

            testObserver.dispose()
        }
    }

    @Test
    fun `insertCollectionShareInfo() should return Completable from store`() {
        val shareInfo: CollectionShareInfo = mock()
        whenever(localSource.insertCollectionShareInfo(shareInfo, CollectionTestData.BUSINESS_ID)).thenReturn(
            Completable.complete()
        )

        val testObserver = repositoryImpl.insertCollectionShareInfo(shareInfo, CollectionTestData.BUSINESS_ID).test()

        verify(localSource).insertCollectionShareInfo(shareInfo, CollectionTestData.BUSINESS_ID)
        testObserver.assertComplete()

        testObserver.dispose()
    }

    @Test
    fun `deleteCollectionShareInfoOfCustomer() should return Completable from store`() {
        whenever(localSource.deleteCollectionShareInfoOfCustomer("customer_id")).thenReturn(Completable.complete())

        val testObserver = repositoryImpl.deleteCollectionShareInfoOfCustomer("customer_id").test()

        verify(localSource).deleteCollectionShareInfoOfCustomer("customer_id")
        testObserver.assertComplete()

        testObserver.dispose()
    }

    @Test
    fun `isCollectionActivated() should return true when merchant payment address is set`() {
        whenever(isCollectionActivated.execute()).thenReturn(Observable.just(true))

        val testObserver = repositoryImpl.isCollectionActivated().test()

        verify(isCollectionActivated).execute()
        testObserver.assertValue(true)

        testObserver.dispose()
    }

    @Test
    fun `isCollectionActivated() should return false when merchant payment address is empty`() {
        whenever(isCollectionActivated.execute()).thenReturn(Observable.just(false))

        val testObserver = repositoryImpl.isCollectionActivated().test()

        verify(isCollectionActivated).execute()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    @Test
    fun `createBatchCollection() should return Completable`() {
        runBlocking {
            val customerIds = listOf("customer_id1", "customer_id2", "customer_id3")
            coEvery {
                (
                    remoteSource.createBatchCollection(
                        CollectionTestData.BUSINESS_ID,
                        customerIds,
                        CollectionTestData.BUSINESS_ID
                    )
                    )
            } returns (listOf(CollectionTestData.COLLECTION1, CollectionTestData.COLLECTION2))
            whenever(
                localSource.putCollections(
                    listOf(
                        CollectionTestData.COLLECTION1,
                        CollectionTestData.COLLECTION2
                    ),
                    CollectionTestData.BUSINESS_ID
                )
            ).thenReturn(Completable.complete())

            val testObserver = repositoryImpl.createBatchCollection(customerIds, CollectionTestData.BUSINESS_ID).test()

            coVerify {
                (remoteSource).createBatchCollection(
                    CollectionTestData.BUSINESS_ID,
                    customerIds,
                    CollectionTestData.BUSINESS_ID
                )
            }
            verify(localSource).putCollections(
                listOf(
                    CollectionTestData.COLLECTION1,
                    CollectionTestData.COLLECTION2
                ),
                CollectionTestData.BUSINESS_ID
            )
            testObserver.assertComplete()

            testObserver.dispose()
        }
    }

    @Test
    fun `getSupplierCollectionProfile() should return CollectionCustomerProfile after from store`() {
        val collectionCustomerProfile = CollectionCustomerProfile(
            accountId = "account_id", message_link = "message_link"
        )
        whenever(
            localSource.getSupplierCollectionProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            )
        )
            .thenReturn(Observable.just(collectionCustomerProfile))
        val testObserver = repositoryImpl.getSupplierCollectionProfile(
            collectionCustomerProfile.accountId,
            CollectionTestData.BUSINESS_ID
        ).test()
        verify(localSource, times(1)).getSupplierCollectionProfile(
            collectionCustomerProfile.accountId,
            CollectionTestData.BUSINESS_ID
        )
        testObserver.assertValue(collectionCustomerProfile)
        testObserver.dispose()
    }

    @Test
    fun `getSupplierPaymentDestination() should return CollectionCustomerProfile after fetching from server when paymentAddress is empty`() {
        runBlocking {

            val collectionCustomerProfile = CollectionCustomerProfile(
                accountId = "account_id"
            )
            coEvery {
                (
                    remoteSource.getCollectionSupplierProfile(
                        collectionCustomerProfile.accountId,
                        CollectionTestData.BUSINESS_ID
                    )
                    )
            } returns collectionCustomerProfile

            whenever(
                localSource.getSupplierCollectionProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Observable.just(collectionCustomerProfile))
            whenever(
                localSource.putSupplierCollectionProfile(
                    collectionCustomerProfile,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Completable.complete())

            val testObserver = repositoryImpl.getSupplierPaymentDestination(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            ).test()

            coVerify {
                (remoteSource).getCollectionSupplierProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            }
            verify(localSource, times(2)).getSupplierCollectionProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            )
            verify(localSource).putSupplierCollectionProfile(collectionCustomerProfile, CollectionTestData.BUSINESS_ID)
            testObserver.assertValue(collectionCustomerProfile)

            testObserver.dispose()
        }
    }

    @Test
    fun `getSupplierPaymentDestination() should return CollectionCustomerProfile after from store when paymentAddress is not empty`() {
        runBlocking {
            val collectionCustomerProfile = CollectionCustomerProfile(
                accountId = "account_id", paymentAddress = "paymentAddress"
            )
            coEvery {
                (
                    remoteSource.getCollectionSupplierProfile(
                        collectionCustomerProfile.accountId,
                        CollectionTestData.BUSINESS_ID
                    )
                    )
            } returns (collectionCustomerProfile)

            whenever(
                localSource.getSupplierCollectionProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Observable.just(collectionCustomerProfile))
            whenever(
                localSource.putSupplierCollectionProfile(
                    collectionCustomerProfile,
                    CollectionTestData.BUSINESS_ID
                )
            )
                .thenReturn(Completable.complete())

            val testObserver = repositoryImpl.getSupplierPaymentDestination(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            ).test()

            coVerify(exactly = 0) {
                (remoteSource).getCollectionSupplierProfile(
                    collectionCustomerProfile.accountId,
                    CollectionTestData.BUSINESS_ID
                )
            }
            verify(localSource, times(2)).getSupplierCollectionProfile(
                collectionCustomerProfile.accountId,
                CollectionTestData.BUSINESS_ID
            )
            verify(localSource, times(0)).putSupplierCollectionProfile(
                collectionCustomerProfile,
                CollectionTestData.BUSINESS_ID
            )
            testObserver.assertValue(collectionCustomerProfile)

            testObserver.dispose()
        }
    }

    @Test
    fun `enableCustomerPayment() should return Completable`() {
        runBlocking {
            coJustRun { (remoteSource.enableCustomerPayment(CollectionTestData.BUSINESS_ID)) }

            val testObserver = repositoryImpl.enableCustomerPayment(CollectionTestData.BUSINESS_ID).test()

            coVerify { (remoteSource).enableCustomerPayment(CollectionTestData.BUSINESS_ID) }
            testObserver.assertComplete()

            testObserver.dispose()
        }
    }

    @Test
    fun `listOnlinePayments() should return list of collectionOnlinePayments`() {
        val onlinePayments = listOf<CollectionOnlinePayment>(mock(), mock())
        whenever(localSource.listOnlinePayments(CollectionTestData.BUSINESS_ID)).thenReturn(
            Observable.just(
                onlinePayments
            )
        )

        val testObserver = repositoryImpl.listOnlinePayments(CollectionTestData.BUSINESS_ID).test()
        verify(localSource).listOnlinePayments(CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(onlinePayments)

        testObserver.dispose()
    }

    @Test
    fun `setOnlinePaymentsDataRead() should return Completable`() {
        whenever(localSource.setOnlinePaymentsDataRead(CollectionTestData.BUSINESS_ID)).thenReturn(Completable.complete())

        val testObserver = repositoryImpl.setOnlinePaymentsDataRead(CollectionTestData.BUSINESS_ID).test()

        verify(localSource).setOnlinePaymentsDataRead(CollectionTestData.BUSINESS_ID)
        testObserver.assertComplete()

        testObserver.dispose()
    }

    @Test
    fun `listOfNewOnlinePayments() should return list of new collectionOnlinePayments`() {
        val onlinePayments = listOf<CollectionOnlinePayment>(mock(), mock())
        whenever(localSource.listOfNewOnlinePayments(CollectionTestData.BUSINESS_ID)).thenReturn(
            Observable.just(
                onlinePayments
            )
        )

        val testObserver = repositoryImpl.listOfNewOnlinePayments(CollectionTestData.BUSINESS_ID).test()
        verify(localSource).listOfNewOnlinePayments(CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(onlinePayments)

        testObserver.dispose()
    }

    @Test
    fun `getOnlinePaymentsTotalAmount() should return double`() {
        val total = 100.0
        whenever(localSource.getOnlinePaymentsTotalAmount(CollectionTestData.BUSINESS_ID)).thenReturn(
            Observable.create {
                it.onNext(total)
            }
        )

        val result = repositoryImpl.getOnlinePaymentsTotalAmount(CollectionTestData.BUSINESS_ID).test()
        verify(localSource).getOnlinePaymentsTotalAmount(CollectionTestData.BUSINESS_ID)

        result.assertValue { it == total }
    }

    @Test
    fun `getOnlinePayment() should return collectionOnlinePayment`() {
        val onlinePayment: CollectionOnlinePayment = mock()
        whenever(
            localSource.getCollectionOnlinePayment(
                "id",
                CollectionTestData.BUSINESS_ID
            )
        ).thenReturn(Observable.just(onlinePayment))

        val testObserver = repositoryImpl.getOnlinePayment("id", CollectionTestData.BUSINESS_ID).test()
        verify(localSource).getCollectionOnlinePayment("id", CollectionTestData.BUSINESS_ID)
        testObserver.assertValue(onlinePayment)

        testObserver.dispose()
    }

    @Test
    fun `tagMerchantPaymentWithCustomer() should return Completable`() {
        runBlocking {
            val paymentId = "paymentId"
            val customerId = "customerId"
            coJustRun {
                (
                    remoteSource.tagMerchantPaymentWithCustomer(
                        customerId,
                        paymentId,
                        CollectionTestData.BUSINESS_ID
                    )
                    )
            }
            whenever(
                localSource.tagCustomerToPayment(
                    paymentId,
                    customerId,
                    CollectionTestData.BUSINESS_ID
                )
            ).thenReturn(
                Completable.complete()
            )

            val testObserver =
                repositoryImpl.tagMerchantPaymentWithCustomer(customerId, paymentId, CollectionTestData.BUSINESS_ID)
                    .test()
            testObserver.awaitCount(1)

            verify(localSource).tagCustomerToPayment(paymentId, customerId, CollectionTestData.BUSINESS_ID)
            testObserver.assertComplete()

            testObserver.dispose()
        }
    }

    @Test
    fun `setActiveDestination when merchant is empty`() {
        runBlocking {
            val request = CollectionMerchantProfile("123123")
            coEvery {
                (
                    remoteSource.setActiveDestination(
                        request,
                        referralMerchant = "",
                        async = false,
                        businessId = CollectionTestData.BUSINESS_ID
                    )
                    )
            } returns ApiMessages.MerchantCollectionProfileResponse(
                customers = emptyList(),
                suppliers = emptyList(),
                destination = null,
                merchantId = "123123",
                merchantVpa = null,
                eta = 0L
            )
            whenever(localSource.clearCollectionSDK()).thenReturn(Completable.complete())
            coJustRun { (syncer.executeSyncCollectionProfile()) }
            whenever(
                localSource.updatePaymentIntent(
                    false,
                    CollectionTestData.BUSINESS_ID
                )
            ).thenReturn(Completable.complete())

            val testObserver =
                repositoryImpl.setActiveDestination(request, businessId = CollectionTestData.BUSINESS_ID).test()
            testObserver.await()
            testObserver.assertValue {
                it.merchantId == "123123"
            }
        }
    }

    @Test
    fun `getKycStatus test`() {
        whenever(localSource.getKycStatus(businessId)).thenReturn(Observable.just(("kyc_status")))
        whenever(localSource.getCollectionMerchantProfile(businessId))
            .thenReturn(Observable.just(CollectionMerchantProfile(businessId)))

        val testObserver = repositoryImpl.getKycStatus(businessId).test()
        testObserver.assertValue { it == "kyc_status" }
    }

    @Test
    fun `canShowQrEducation test`() {
        whenever(localSource.canShowQrEducation()).thenReturn(Single.just(false))
        val testObserver = repositoryImpl.canShowQrEducation().test()
        testObserver.assertValue { it == false }
        testObserver.dispose()
    }

    @Test
    fun `resetQrEducation test`() {
        whenever(localSource.resetQrEducation()).thenReturn(Completable.complete())
        repositoryImpl.resetQrEducation()
        verify(localSource).resetQrEducation()
    }

    @Test
    fun `setQrSaveSendEducationShown test`() {
        whenever(localSource.resetQrEducation()).thenReturn(Completable.complete())
        repositoryImpl.setQrSaveSendEducationShown()
        verify(localSource).setQrSaveSendEducationShown()
    }

    @Test
    fun `isQrSaveSendEducationShown test`() {
        whenever(localSource.isQrSaveSendEducationShown()).thenReturn(false)

        Assert.assertTrue(!repositoryImpl.isQrSaveSendEducationShown())
        verify(localSource).isQrSaveSendEducationShown()
    }

    @Test
    fun `getBlindPayLinkId test`() {
        runBlocking {

            val accountId = "asdf123456321"
            val result = ApiMessages.BlindPayCreateLinkResponse("test_link_id")

            coEvery { (remoteSource.getBlindPayLinkId(accountId, CollectionTestData.BUSINESS_ID)) } returns (
                result
                )

            repositoryImpl.getBlindPayLinkId(accountId, CollectionTestData.BUSINESS_ID).test().apply {
                awaitCount(1)
                assertValue(result)
            }
            coVerify { (remoteSource).getBlindPayLinkId(accountId, CollectionTestData.BUSINESS_ID) }
        }
    }
}
