package `in`.okcredit.collection_ui.ui.qrcode

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.ui.TestViewModel
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeViewModel
import `in`.okcredit.collection_ui.usecase.GetMerchantQRIntent
import `in`.okcredit.collection_ui.usecase.GetNewOnlinePayments
import `in`.okcredit.collection_ui.usecase.GetTotalOnlinePaymentCount
import `in`.okcredit.collection_ui.usecase.GetUnSettledAmountDueToInvalidBankDetails
import `in`.okcredit.collection_ui.usecase.SaveMerchantQROnDevice
import `in`.okcredit.collection_ui.usecase.ShouldShowReferralBanner
import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.UseCase
import android.os.Build
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.contract.MerchantPrefSyncStatus

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class QrCodeViewModelTest :
    TestViewModel<QrCodeContract.State, QrCodeContract.PartialState, QrCodeContract.ViewEvent>() {

    private val getCollectionMerchantProfile: GetCollectionMerchantProfile = mock()
    private val tracker: Tracker = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val getMerchantPreference: Lazy<IndividualRepository> = mock()
    private val getMerchantQRIntentLazy: Lazy<GetMerchantQRIntent> = mock()
    private val getMerchantQRIntent: GetMerchantQRIntent = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()
    private val setCollectionDestination: SetCollectionDestination = mock()
    private val saveMerchantQROnDeviceLazy: Lazy<SaveMerchantQROnDevice> = mock()
    private val saveMerchantQROnDevice: SaveMerchantQROnDevice = mock()
    private val isKycCompletedLazy: Lazy<IsKycCompleted> = mock()
    private val isKycCompleted: IsKycCompleted = mock()
    private val getNewOnlinePayments: GetNewOnlinePayments = mock()
    private val isPasswordSet: Lazy<IsPasswordSet> = mock()
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus> = mock()
    private val getKycStatus: GetKycStatus = mock()
    private val getUnSettledAmountDueToInvalidBankDetails: GetUnSettledAmountDueToInvalidBankDetails = mock()
    private val collectionEvent: SendCollectionEvent = mock()
    private val collectionTracker: CollectionTracker = mock()
    private val triggerMerchantPayout: TriggerMerchantPayout = mock()
    private val getTotalOnlinePaymentCount: GetTotalOnlinePaymentCount = mock()
    private val shouldShowReferralBanner: ShouldShowReferralBanner = mock()
    private val referralEducationPreference: ReferralEducationPreference = mock()
    private val isCollectionActivatedOrOnlinePaymentExist: IsCollectionActivatedOrOnlinePaymentExist = mock()
    private val collectionSyncer: CollectionSyncer = mock()

    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        private var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val merchant = Business(
            id = "id",
            name = "name",
            mobile = "mobile",
            createdAt = dt,
            updateCategory = true,
            updateMobile = true
        )

        val collectionOnlinePayment = CollectionOnlinePayment(
            id = "id",
            createdTime = dt,
            updatedTime = dt,
            status = 101,
            merchantId = "merchantId",
            accountId = "accountId",
            amount = 101.toDouble(),
            paymentId = "paymentId",
            payoutId = "payoutId",
            paymentSource = "paymentSource",
            paymentMode = "paymentMode",
            type = "type",
            read = true,
            errorCode = "",
            errorDescription = ""
        )
    }

    override fun initDependencies() {
        whenever(getMerchantQRIntentLazy.get()).thenReturn(getMerchantQRIntent)
        whenever(saveMerchantQROnDeviceLazy.get()).thenReturn(saveMerchantQROnDevice)
        whenever(isKycCompletedLazy.get()).thenReturn(isKycCompleted)
        super.initDependencies()
    }

    @Test
    fun getCollectionProfileMerchantTest() {
        val collectionMerchantProfile = CollectionMerchantProfile(
            merchant_id = "merchant_id",
            name = "name",
            payment_address = "payment_address",
            type = "type",
            merchant_vpa = "merchant_vpa"
        )

        whenever(getCollectionMerchantProfile.execute()).thenReturn(Observable.just(collectionMerchantProfile))
        pushIntent((QrCodeContract.Intent.LoadCollectionProfileMerchant))
        verify(getCollectionMerchantProfile).execute()
        assertLastValue {
            it.responseData.collectionMerchantProfile == collectionMerchantProfile &&
                it.merchantCollectionState.paymentAddress == collectionMerchantProfile.payment_address &&
                it.merchantCollectionState.qrIntent == "upi://pay?pa=merchant_vpa&pn=name"
        }
    }

    @Test
    fun getMerchant() {
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))
        pushIntent((QrCodeContract.Intent.LoadMerchant))
        assertLastValue {
            it.responseData.business == merchant
        }
    }

    @Test
    fun `getKycRiskCategory() should return no risk `() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.NO_RISK, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == response.kycRiskCategory
        }
    }

    @Test
    fun `getKycRiskCategory() should return low risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == response.kycRiskCategory
        }
    }

    @Test
    fun `getKycRiskCategory() should return high risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == response.kycRiskCategory
        }
    }

    @Test
    fun `getKycRiskCategory() should return low risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == response.kycRiskCategory
        }
    }

    @Test
    fun `getKycRiskCategory() should return high risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == response.kycRiskCategory
        }
    }

    @Test
    fun getNewOnlinePaymentsCount() {
        whenever(getNewOnlinePayments.execute(Unit)).thenReturn(
            UseCase.wrapObservable(
                Observable.just(
                    listOf(
                        collectionOnlinePayment
                    )
                )
            )
        )
        pushIntent((QrCodeContract.Intent.LoadNewOnlinePaymentsCount))
        assertLastValue {
            it.onlinePaymentState.newCount == 1
        }
    }

    @Test
    fun `getKycStatus() should return kyc status`() {
        val status = KycStatus.COMPLETE
        val kycRisk = KycRisk(KycRiskCategory.LOW, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(kycRisk))
        pushIntent((QrCodeContract.Intent.LoadKycDetails))
        assertLastValue {
            it.responseData.kycStatus == status &&
                it.responseData.kycRiskCategory == kycRisk.kycRiskCategory
        }
    }

    override fun createViewModel() = QrCodeViewModel(
        initialState = QrCodeContract.State(),
        getCollectionMerchantProfile = { getCollectionMerchantProfile },
        tracker = { tracker },
        getActiveBusiness = { getActiveBusiness },
        getMerchantQRIntent = getMerchantQRIntentLazy,
        getKycRiskCategory = { getKycRiskCategory },
        setCollectionDestination = { setCollectionDestination },
        saveMerchantQROnDevice = saveMerchantQROnDeviceLazy,
        getNewOnlinePayments = { getNewOnlinePayments },
        getMerchantPreference = getMerchantPreference,
        isPasswordSet = isPasswordSet,
        merchantPrefSyncStatus = merchantPrefSyncStatus,
        context = { mock() },
        getKycStatus = { getKycStatus },
        getUnSettledAmountDueToInvalidBankDetails = { getUnSettledAmountDueToInvalidBankDetails },
        sendCollectionEvent = { collectionEvent },
        collectionTracker = { collectionTracker },
        triggerMerchantPayout = { triggerMerchantPayout },
        getTotalOnlinePaymentCount = { getTotalOnlinePaymentCount },
        shouldShowReferralBanner = { shouldShowReferralBanner },
        referralEducationPreference = { referralEducationPreference },
        isCollectionActivatedOrOnlinePaymentExist = { isCollectionActivatedOrOnlinePaymentExist },
        collectionSyncer = { collectionSyncer },
        getLastOnlinePayment = mock(),
        shouldShowOrderQr = mock(),
        getPaymentReminderIntent = mock(),
    )
}
