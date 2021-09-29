package `in`.okcredit.collection_ui.ui.add_merchant_destination

import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationContract.*
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationViewModel
import `in`.okcredit.collection_ui.usecase.SetActiveDestination
import `in`.okcredit.collection_ui.usecase.ValidatePaymentAddress
import `in`.okcredit.merchant.contract.GetActiveBusiness
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import org.junit.Test

class AddMerchantDestinationViewModelTest {

    private lateinit var addMerchantDestinationPresenter: AddMerchantDestinationViewModel
    private val tracker: CollectionTracker = mock()
    private val setActiveDestination: SetActiveDestination = mock()
    private val getActiveBusiness: GetActiveBusiness = mock() {
        mock()
    }
    private val getCollectionMerchantProfile: GetCollectionMerchantProfile = mock()
    private val validatePaymentAddress: ValidatePaymentAddress = mock()

    fun createViewModel(initialState: State, paymentMethodType: String? = null) {
        addMerchantDestinationPresenter = AddMerchantDestinationViewModel(
            initialState = initialState,
            isUpdateCollection = false,
            paymentMethodType = if (paymentMethodType.isNullOrBlank().not()) paymentMethodType else "",
            validatePaymentAddress = { validatePaymentAddress },
            getCollectionMerchantProfile = { getCollectionMerchantProfile },
            getActiveBusiness = { getActiveBusiness },
            setActiveDestination = { setActiveDestination },
            tracker = tracker
        )
    }

//    @Test
//    fun `on Load set AdoptionMode, CollectionMerchantProfile and adoptionMode`() {
//        //Given
//        val initialState = State()
//        createViewModel(initialState, paymentMethodType = "Bali")
//        whenever(
//            getCollectionMerchantProfile.execute()
//        ).thenReturn(Observable.just(CollectionMerchantProfile.empty()))
//
//        whenever(getActiveMerchant.execute()).thenReturn(Observable.just(merchant))
//
//        //when
//        val testObserver = addMerchantDestinationPresenter.state().test()
//        addMerchantDestinationPresenter.attachIntents(Observable.just(Intent.Load))
//
//        //then
//        testObserver.values().contains(initialState)
//        testObserver.values().contains(
//            State(
//                adoptionMode = "Bali",
//                collectionMerchantProfile = CollectionMerchantProfile.empty(),
//                merchant = merchant
//            )
//        )
//
//        verify(getCollectionMerchantProfile).execute()
//        verify(getActiveMerchant).execute()
//
//        testObserver.dispose()
//    }

    @Test
    fun `should set Adoption Mode`() {
        // Given
        val initialState = State()
        createViewModel(initialState)

        // When
        val testObserver = addMerchantDestinationPresenter.state().test()
        addMerchantDestinationPresenter.attachIntents(Observable.just(Intent.SetAdoptionMode("Bali")))

        // Then
        testObserver.values().contains(initialState)
        testObserver.values().contains(
            State(adoptionMode = "Bali")
        )

        testObserver.dispose()
    }

    @Test
    fun `should set Account Number`() {
        // Given
        val initialState = State()
        createViewModel(initialState)

        // When
        val testObserver = addMerchantDestinationPresenter.state().test()
        addMerchantDestinationPresenter.attachIntents(Observable.just(Intent.EnteredAccountNumber("054101507917")))

        // Then
        testObserver.values().contains(initialState)
        testObserver.values().contains(
            State(enteredAccountNumber = "054101507917")
        )

        testObserver.dispose()
    }

    @Test
    fun `should set Ifsc Code`() {
        // Given
        val initialState = State()
        createViewModel(initialState)

        // When
        val testObserver = addMerchantDestinationPresenter.state().test()
        addMerchantDestinationPresenter.attachIntents(Observable.just(Intent.EnteredIfsc("ICIC0000541")))

        // Then
        testObserver.values().contains(initialState)
        testObserver.values().contains(
            State(enteredAccountNumber = "ICIC0000541")
        )

        testObserver.dispose()
    }

    @Test
    fun `should set UPI ID`() {
        // Given
        val initialState = State()
        createViewModel(initialState)

        // When
        val testObserver = addMerchantDestinationPresenter.state().test()
        addMerchantDestinationPresenter.attachIntents(Observable.just(Intent.EnteredUPI("8882946897@ybl")))

        // Then
        testObserver.values().contains(initialState)
        testObserver.values().contains(
            State(enteredAccountNumber = "8882946897@ybl")
        )

        testObserver.dispose()
    }

/*
 TO be fixed
 @Test
    fun `should set and return CollectionMerchant Profile`() {
        //Given
        val collectionMerchantProfile =
            CollectionMerchantProfile(payment_address = "8882946897@ybl", merchant_id = "hhhh")
        val initialState = State()
        createViewModel(initialState)

        whenever(setCollectionDestination.execute(collectionMerchantProfile)).thenReturn(
            Observable.just(
                collectionMerchantProfile
            )
        )

        //When
        val testObserver = addMerchantDestinationPresenter.state().test()
        addMerchantDestinationPresenter.attachIntents(
            Observable.just(
                Intent.ConfirmBankAccount(
                    paymentAddress = "8882946897@ybl",
                    merchantId = "hhhh"
                )
            )
        )

        testObserver.values().contains(initialState)
        testObserver.values().contains(
            State(
                collectionMerchantProfile = collectionMerchantProfile
            )
        )

        testObserver.dispose()
    }*/
}
