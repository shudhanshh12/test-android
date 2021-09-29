package tech.okcredit.home.ui.menu

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.ShouldShowCreditCardInfoForKyc
import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import `in`.okcredit.merchant.contract.SyncBusinessData
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.home.models.KycMenuItem
import tech.okcredit.home.ui.menu.HomeMenuContract.*
import tech.okcredit.home.ui.sidemenu.usecacse.ShouldShowCallCustomerCare
import tech.okcredit.home.usecase.GetCustomization
import tech.okcredit.home.usecase.GetUnClaimedRewards
import tech.okcredit.home.usecase.ShowFeedback
import java.util.*
import javax.inject.Inject

class HomeMenuViewModel @Inject constructor(
    initialState: Lazy<HomeMenuState>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getCustomization: Lazy<GetCustomization>,
    private val getUnClaimedRewards: Lazy<GetUnClaimedRewards>,
    private val submitFeedback: Lazy<SubmitFeedbackImpl>,
    private val ab: Lazy<AbRepository>,
    private val showFeedback: Lazy<ShowFeedback>,
    private val tracker: Lazy<Tracker>,
    private val shouldShowCreditCardInfoForKyc: Lazy<ShouldShowCreditCardInfoForKyc>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val syncBusinessData: Lazy<SyncBusinessData>,
    private val isMultipleAccountEnabled: Lazy<IsMultipleAccountEnabled>,
    private val shouldShowCallCustomerCare: Lazy<ShouldShowCallCustomerCare>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
) : BaseViewModel<HomeMenuState, HomeMenuPartialState, HomeMenuViewEvent>(initialState.get()) {
    override fun handle(): Observable<out UiState.Partial<HomeMenuState>> {
        return Observable.mergeArray(
            observableForCustomization(),
            observableLoadForMerchantData(),
            observableForMerchant(),
            moveToSettingsScreen(),
            observableForUnclaimedRewardsCount(),
            observableForFeedback(),
            showFeedbackItemVisibility(),
            observableForCollectionClick(),
            shouldShowCreditCardInfoForKyc(),
            showKycStatusDialog(),
            showKycMenuItem(),
            handleCanShowCreateBusiness(),
            showCallCustomerCare(),
            callCustomerCare(),
            observableForInventory(),
        )
    }

    private fun observableForFeedback() = intent<HomeMenuIntent.SubmitFeedback>()
        .switchMap { wrap(submitFeedback.get().schedule(it.feedback, it.rating)) }
        .map {
            HomeMenuPartialState.NoChange
        }

    private fun showFeedbackItemVisibility() = showFeedback.get().execute()
        .map {
            if (it is Result.Success) {
                HomeMenuPartialState.SetFeedbackEnabled(it.value)
            } else {
                HomeMenuPartialState.NoChange
            }
        }

    private fun observableForCollectionClick() = intent<HomeMenuIntent.CollectionClicked>()
        .map {
            emitViewEvent(HomeMenuViewEvent.GoToCollectionScreen)
            HomeMenuPartialState.NoChange
        }

    private fun getKycDetails() = Observable.combineLatest(
        getKycStatus.get().execute(shouldFetchWhenCollectionNotAdopted = true),
        getKycRiskCategory.get().execute(shouldFetchWhenCollectionNotAdopted = true),
        { kycStatus, kycRisk ->
            Pair(kycStatus, kycRisk)
        }
    )

    private fun shouldShowCreditCardInfoForKyc() = intent<HomeMenuIntent.Load>()
        .switchMap { shouldShowCreditCardInfoForKyc.get().execute() }
        .map {
            HomeMenuPartialState.SetShouldShowCreditCardInfoForKyc(it)
        }

    private fun showKycStatusDialog() = intent<HomeMenuIntent.ShowKycStatusDialog>()
        .map {
            emitViewEvent(HomeMenuViewEvent.ShowKycStatusDialog)

            val kycMenuItemAvailable = (getCurrentState().kycMenuItem as KycMenuItem.Available)
            val kycStatus = kycMenuItemAvailable.kycStatus
            val kycRiskCategory = kycMenuItemAvailable.kycRisk.kycRiskCategory

            tracker.get().trackEntryPointClicked(
                source = PropertyValue.MENU_ITEM,
                type = if (kycStatus == KycStatus.NOT_SET) PropertyValue.COMPLETE_KYC else PropertyValue.KYC_STATUS,
                riskType = kycRiskCategory.value.lowercase(Locale.getDefault()),
            )

            HomeMenuPartialState.NoChange
        }

    private fun showKycMenuItem() = intent<HomeMenuIntent.Load>()
        .switchMap { getKycDetails() }
        .map { (kycStatus, kycRisk) ->
            HomeMenuPartialState.SetKycMenuItem(KycMenuItem.Available(kycStatus, kycRisk))
        }

    private fun moveToSettingsScreen() = intent<HomeMenuIntent.SettingsClicked>()
        .map {
            emitViewEvent(HomeMenuViewEvent.GoToSettingsScreen)
            HomeMenuPartialState.NoChange
        }

    private fun observableForCustomization() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(getCustomization.get().execute(Targets.SIDE_MENU)) }
        .map {
            when (it) {
                is Result.Progress -> HomeMenuPartialState.NoChange
                is Result.Success -> HomeMenuPartialState.Customization(it.value)
                is Result.Failure -> HomeMenuPartialState.NoChange
            }
        }

    private fun observableForUnclaimedRewardsCount() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(getUnClaimedRewards.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> HomeMenuPartialState.NoChange
                is Result.Success -> HomeMenuPartialState.UnclaimedRewardsCount(it.value.size)
                is Result.Failure -> HomeMenuPartialState.NoChange
            }
        }

    private fun observableForMerchant() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(getActiveBusiness.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> HomeMenuPartialState.NoChange
                is Result.Success -> HomeMenuPartialState.ProfileLoad(it.value)
                is Result.Failure -> HomeMenuPartialState.NoChange
            }
        }

    private fun observableLoadForMerchantData() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(syncBusinessData.get().execute()) }
        .map { HomeMenuPartialState.NoChange }

    private fun handleCanShowCreateBusiness() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(isMultipleAccountEnabled.get().execute()) }
        .map {
            if (it is Result.Success) {
                return@map HomeMenuPartialState.ShowCreateBusiness(it.value)
            }
            HomeMenuPartialState.NoChange
        }

    private fun showCallCustomerCare() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(shouldShowCallCustomerCare.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> HomeMenuPartialState.NoChange
                is Result.Success -> {
                    if (it.value) {
                        // No need to delegate to different thread
                        HomeMenuPartialState.ShowCallCustomerCare(getSupportNumber.get().supportNumber)
                    } else {
                        HomeMenuPartialState.ShowCallCustomerCare("")
                    }
                }
                is Result.Failure -> HomeMenuPartialState.NoChange
            }
        }

    private fun callCustomerCare() = intent<HomeMenuIntent.CallHelp>()
        .map {
            emitViewEvent(HomeMenuViewEvent.CallHelp)
            HomeMenuPartialState.NoChange
        }

    private fun observableForInventory() = intent<HomeMenuIntent.Load>()
        .switchMap { wrap(ab.get().isFeatureEnabled(Features.FEATURE_BILLING_AND_INVENTORY).firstOrError()) }
        .map {
            when (it) {
                is Result.Progress -> HomeMenuPartialState.NoChange
                is Result.Success -> HomeMenuPartialState.SetShowInventoryAndBilling(it.value)
                is Result.Failure -> HomeMenuPartialState.NoChange
            }
        }

    override fun reduce(currentState: HomeMenuState, partialState: HomeMenuPartialState): HomeMenuState {
        return when (partialState) {
            is HomeMenuPartialState.Customization -> currentState.copy(customization = partialState.target)
            is HomeMenuPartialState.NoChange -> currentState
            is HomeMenuPartialState.ProfileLoad -> currentState.copy(business = partialState.business)
            is HomeMenuPartialState.UnclaimedRewardsCount -> currentState.copy(showNewLabel = partialState.count > 0)
            is HomeMenuPartialState.SetFeedbackEnabled -> currentState.copy(isFeedbackEnabled = partialState.isFeedbackEnabled)
            is HomeMenuPartialState.SetKycMenuItem -> currentState.copy(
                kycMenuItem = partialState.kycMenuItem
            )
            is HomeMenuPartialState.SetShouldShowCreditCardInfoForKyc -> currentState.copy(
                shouldShowCreditCardInfoForKyc = partialState.shouldShowCreditCardInfoForKyc
            )
            is HomeMenuPartialState.ShowCreateBusiness -> currentState.copy(
                canShowCreateBusiness = partialState.canShow
            )
            is HomeMenuPartialState.ShowCallCustomerCare -> currentState.copy(helpNumber = partialState.number)
            is HomeMenuPartialState.SetShowInventoryAndBilling -> currentState.copy(
                showInventoryAndBilling = partialState.shouldShow
            )
        }
    }
}
