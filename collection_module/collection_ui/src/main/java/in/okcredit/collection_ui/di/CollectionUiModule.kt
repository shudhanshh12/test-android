package `in`.okcredit.collection_ui.di

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.dialogs.SuccessDialog
import `in`.okcredit.collection_ui.navigation.CollectionNavigatorImpl
import `in`.okcredit.collection_ui.ui.benefits.CollectionBenefitsActivity
import `in`.okcredit.collection_ui.ui.benefits.CollectionBenefitsModule
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListActivity
import `in`.okcredit.collection_ui.ui.defaulters._di.DefaulterListModule
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivity
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivityModule
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.home.add._di.AddMerchantDestinationModule
import `in`.okcredit.collection_ui.ui.home_menu.HomePaymentsContainerFragment
import `in`.okcredit.collection_ui.ui.home_menu.HomePaymentsContainerModule
import `in`.okcredit.collection_ui.ui.insights.CollectionInsightsActivity
import `in`.okcredit.collection_ui.ui.insights.CollectionInsightsModule
import `in`.okcredit.collection_ui.ui.inventory.InventoryActivity
import `in`.okcredit.collection_ui.ui.inventory.InventoryActivityModule
import `in`.okcredit.collection_ui.ui.passbook.PassbookActivity
import `in`.okcredit.collection_ui.ui.passbook.PassbookActivityModule
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataDialog
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.di.AddToKhataModule
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailFragment
import `in`.okcredit.collection_ui.ui.passbook.detail.di.PaymentDetailModule
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment
import `in`.okcredit.collection_ui.ui.passbook.payments._di.OnlinePaymentsModule
import `in`.okcredit.collection_ui.ui.passbook.refund.RefundConsentBottomSheet
import `in`.okcredit.collection_ui.ui.passbook.refund.di.RefundConsentModule
import `in`.okcredit.collection_ui.ui.qr_scanner.QrScannerActivity
import `in`.okcredit.collection_ui.ui.qr_scanner.QrScannerModule
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivityModule
import `in`.okcredit.collection_ui.usecase.*
import `in`.okcredit.merchant.collection.usecase.GetCollectionActivationStatusImpl
import dagger.Binds
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.di.AppScope

@dagger.Module
abstract class CollectionUiModule {

    @Binds
    @Reusable
    abstract fun dataProvider(getCreditGraphicalData: GetCreditGraphicalData): CreditGraphicalDataProvider

    @Binds
    @Reusable
    abstract fun collectionNavigator(collectionNavigator: CollectionNavigatorImpl): CollectionNavigator

    @Binds
    @Reusable
    abstract fun getCollectionActivationStatus(getCollectionActivationStatus: GetCollectionActivationStatusImpl): GetCollectionActivationStatus

    @Binds
    @Reusable
    abstract fun checkLiveSalesActive(checkLiveSalesActive: CheckLiveSalesActiveImpl): CheckLiveSalesActive

    @Binds
    @Reusable
    abstract fun canShowAddBankDetailsPopUp(canShowAddBankDetailsPopUp: CanShowAddBankDetailsPopUpImpl): CanShowAddBankDetailsPopUp

    @Binds
    @AppScope
    abstract fun getCollectionEventTracker(collectionTracker: CollectionTracker): CollectionEventTracker

    @ContributesAndroidInjector(modules = [CollectionsHomeActivityModule::class])
    abstract fun collectionsHomeActivity(): CollectionsHomeActivity

    @ContributesAndroidInjector(modules = [CollectionBenefitsModule::class])
    abstract fun collectionBenefitsActivity(): CollectionBenefitsActivity

    @ContributesAndroidInjector(modules = [AddMerchantDestinationModule::class])
    abstract fun addMerchantDestinationDialog(): AddMerchantDestinationDialog

    @ContributesAndroidInjector(modules = [CollectionInsightsModule::class])
    abstract fun collectionInsightsActivity(): CollectionInsightsActivity

    @ContributesAndroidInjector(modules = [PassbookActivityModule::class])
    abstract fun passbookActivity(): PassbookActivity

    @ContributesAndroidInjector(modules = [TargetedReferralActivityModule::class])
    abstract fun referralActivity(): TargetedReferralActivity

    @ContributesAndroidInjector(modules = [QrScannerModule::class])
    abstract fun qrScannerScreen(): QrScannerActivity

    @ContributesAndroidInjector(modules = [DefaulterListModule::class])
    abstract fun defaulterListScreen(): DefaulterListActivity

    @ContributesAndroidInjector(modules = [InventoryActivityModule::class])
    abstract fun billingActivity(): InventoryActivity

    @ContributesAndroidInjector
    abstract fun successDialog(): SuccessDialog

    @ContributesAndroidInjector(modules = [OnlinePaymentsModule::class])
    abstract fun onlinePaymentsScreen(): OnlinePaymentsFragment

    @ContributesAndroidInjector(modules = [PaymentDetailModule::class])
    abstract fun onlinePaymentDetailScreen(): PaymentDetailFragment

    @ContributesAndroidInjector(modules = [AddToKhataModule::class])
    abstract fun addToKhataDialog(): AddToKhataDialog

    @ContributesAndroidInjector(modules = [RefundConsentModule::class])
    abstract fun refundConsentBottomSheet(): RefundConsentBottomSheet

    @ContributesAndroidInjector(modules = [HomePaymentsContainerModule::class])
    abstract fun homePaymentsContainerFragment(): HomePaymentsContainerFragment

    @Binds
    @Reusable
    abstract fun isMerchantFromCollectionCampaign(
        isMerchantFromCollectionCampaignImpl: IsMerchantFromCollectionCampaignImpl,
    ): IsCollectionCampaignMerchant

    @Binds
    @Reusable
    abstract fun enablePaymentAddress(
        enablePaymentAddressImpl: EnablePaymentAddressImpl,
    ): EnablePaymentAddress

    @Binds
    @Reusable
    abstract fun getCollectionMerchantProfile(
        getCollectionMerchantProfileImpl: GetCollectionMerchantProfileImpl,
    ): GetCollectionMerchantProfile

    @Binds
    @Reusable
    abstract fun getCustomerCollectionProfile(
        getCustomerCollectionProfileImpl: GetCustomerCollectionProfileImpl,
    ): GetCustomerCollectionProfile

    @Binds
    @Reusable
    abstract fun setCollectionDestination(
        setCollectionDestinationImpl: SetCollectionDestinationImpl,
    ): SetCollectionDestination

    @Binds
    @Reusable
    abstract fun isUpiVpaValid(
        isUpiVpaValidImpl: IsUpiVpaValidImpl,
    ): IsUpiVpaValid

    @Binds
    @Reusable
    abstract fun getSupplierCollectionProfileWithSync(
        getSupplierCollectionProfileWithSyncImpl: GetSupplierCollectionProfileWithSyncImpl,
    ): GetSupplierCollectionProfileWithSync

    @Binds
    @Reusable
    abstract fun isKycRiskReached(
        isKycRiskReachedImpl: GetKycRiskCategoryImpl,
    ): GetKycRiskCategory

    @Binds
    @Reusable
    abstract fun isKycCompleted(
        isKycCompletedImpl: IsKycCompletedImpl,
    ): IsKycCompleted

    @Binds
    @Reusable
    abstract fun getKycStatus(
        getKycStatus: GetKycStatusImpl,
    ): GetKycStatus

    @Binds
    @Reusable
    abstract fun shouldShowCreditCardInfoForKyc(
        shouldShowCreditCardInfoForKyc: ShouldShowCreditCardInfoForKycImpl,
    ): ShouldShowCreditCardInfoForKyc

    @Binds
    @Reusable
    abstract fun sendCollectionEvent(
        sendCollectionEvent: SendCollectionEventImpl,
    ): SendCollectionEvent

    @Binds
    @Reusable
    abstract fun triggerMerchantPayout(
        triggerMerchantPayoutImpl: TriggerMerchantPayoutImpl,
    ): TriggerMerchantPayout

    @Binds
    @Reusable
    abstract fun setOnlinePaymentStatusLocally(
        setOnlinePaymentStatusLocallyImpl: SetOnlinePaymentStatusLocallyImpl,
    ): SetOnlinePaymentStatusLocally
}
