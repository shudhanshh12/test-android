package `in`.okcredit.merchant.profile

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.SyncBusinessData
import `in`.okcredit.merchant.usecase.UpdateBusinessImpl
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BusinessViewModel @Inject constructor(
    initialState: BusinessContract.State,
    private val navigator: BusinessContract.Navigator,
    private val businessApi: BusinessRepository,
    private val getActiveBusiness: GetActiveBusiness,
    private val updateMerchant: UpdateBusinessImpl,
    private val tracker: Tracker,
    private val getReferralLink: GetReferralLink,
    private val checkNetworkHealth: CheckNetworkHealth,
    @ViewModelParam(BusinessFragment.ARG_SETUP_PROFILE) var setupProfile: Boolean,
    @ViewModelParam(BusinessFragment.ARG_SHARE_BUSINESS_CARD) var shareBusinessCard: Boolean,
    @ViewModelParam(BusinessFragment.ARG_SHOW_MERCHANT_PROFILE) var showMerchantProfile: Boolean,
    @ViewModelParam(BusinessFragment.ARG_SHOW_MERCHANT_LOCATION) var showMerchantLocation: Boolean,
    @ViewModelParam(BusinessFragment.ARG_SHOW_CATEGORY_SCREEN) var showCategoryScreen: Boolean,
    @ViewModelParam(BusinessFragment.ARG_SHOW_BUSINESS_TYPE_BOTTOM_SHEET) var showBusinessTypeBottomSheet: Boolean,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val syncBusinessData: Lazy<SyncBusinessData>,
    private val isMultipleAccountEnabled: Lazy<IsMultipleAccountEnabled>
) : BaseViewModel<BusinessContract.State, BusinessContract.PartialState, BusinessContract.ViewEvent>(
    initialState,
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var profileImageUrl: String
    private var businessTypeId: String = ""
    private var cameraImage: Boolean = false

    override fun handle(): Observable<UiState.Partial<BusinessContract.State>> {
        return Observable.mergeArray(
            observableLoadForMerchantData(),

            observeContextualHelpIdsOnLoad(),

            checkNetworkHealth
                .execute(Unit)
                .filter { it is Result.Success }
                .map {
                    // network connected
                    reload.onNext(Unit)
                    BusinessContract.PartialState.NoChange
                },

            intent<BusinessContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getReferralLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> BusinessContract.PartialState.NoChange
                        is Result.Success -> {
                            BusinessContract.PartialState.SetReferralData(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    BusinessContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> BusinessContract.PartialState.NoChange
                                else -> BusinessContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<BusinessContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(businessApi.getBusinessTypes()) }
                .map {
                    when (it) {
                        is Result.Progress -> BusinessContract.PartialState.NoChange
                        is Result.Success -> {
                            BusinessContract.PartialState.SetBusinessTypes(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    BusinessContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> BusinessContract.PartialState.NoChange
                                else -> BusinessContract.PartialState.NoChange
                            }
                        }
                    }
                },

            /***********************   MerchantResponse data  ***********************/
            intent<BusinessContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> BusinessContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (setupProfile) {
                                Completable.timer(1, TimeUnit.SECONDS)
                                    .subscribe {
                                        navigator.gotoSetupProfile()
                                        setupProfile = false
                                    }
                            }
                            if (shareBusinessCard) {
                                Completable.timer(1, TimeUnit.SECONDS)
                                    .subscribe {
                                        navigator.shareBusinessCard()
                                        shareBusinessCard = false
                                    }
                            }

                            if (showMerchantProfile) {
                                Completable.timer(1, TimeUnit.SECONDS)
                                    .subscribe {
                                        navigator.showProfileImageBottomSheet(false)
                                        showMerchantProfile = false
                                    }
                            }

                            if (showMerchantLocation) {
                                Completable.timer(200, TimeUnit.MILLISECONDS)
                                    .subscribe {
                                        navigator.showLocationDialog()
                                        showMerchantLocation = false
                                    }
                            }

                            if (showBusinessTypeBottomSheet) {
                                Completable.timer(200, TimeUnit.MILLISECONDS)
                                    .subscribe {
                                        navigator.openBusinessTypeBottomSheet()
                                        showBusinessTypeBottomSheet = false
                                    }
                            }

                            if (showCategoryScreen) {
                                Completable.timer(200, TimeUnit.MILLISECONDS)
                                    .subscribe {
                                        navigator.goToCategoryScreen()
                                        showCategoryScreen = false
                                    }
                            }

                            BusinessContract.PartialState.ShowBusiness(it.value)
                        }

                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    BusinessContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> BusinessContract.PartialState.SetNetworkError

                                else -> BusinessContract.PartialState.ErrorState
                            }
                        }
                        else -> BusinessContract.PartialState.NoChange
                    }
                },

            intent<BusinessContract.Intent.UpdateProfileImage>()
                .switchMap {
                    cameraImage = it.profileImage.first
                    profileImageUrl = it.profileImage.second
                    wrap(
                        updateMerchant.execute(
                            Request(
                                inputType = BusinessConstants.PROFILE_IMAGE,
                                updatedValue = profileImageUrl
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> BusinessContract.PartialState.ShowLoading
                        is Result.Success -> {
                            when {
                                profileImageUrl.isBlank() -> tracker.trackUpdateProfileV7(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.PHOTO,
                                    removed = PropertyValue.TRUE
                                )
                                cameraImage -> tracker.trackUpdateProfileV5(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.PHOTO,
                                    method = PropertyValue.CAMERA
                                )
                                else -> tracker.trackUpdateProfileV5(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.PHOTO,
                                    method = PropertyValue.GALLERY
                                )
                            }

                            BusinessContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    BusinessContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    BusinessContract.PartialState.SetNetworkError
                                }

                                else -> {
                                    BusinessContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<BusinessContract.Intent.UpdateBusiness>()
                .switchMap {
                    businessTypeId = it.business.id
                    wrap(
                        updateMerchant.execute(
                            Request(
                                inputType = BusinessConstants.BUSINESS_TYPE,
                                businessType = it.business
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> BusinessContract.PartialState.NoChange
                        is Result.Success -> {
                            tracker.trackUpdateProfileLegacy(
                                relation = PropertyValue.MERCHANT,
                                field = PropertyValue.BUSINESS_TYPE,
                                businessId = businessTypeId
                            )
                            BusinessContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    BusinessContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    BusinessContract.PartialState.NoChange
                                }

                                else -> {
                                    BusinessContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            // Fetching categories
            intent<BusinessContract.Intent.ShowProfileBottomSheet>()
                .map {
                    navigator.showProfileImageBottomSheet(it.showFullScreenImage)
                    BusinessContract.PartialState.NoChange
                },

            intent<BusinessContract.Intent.GoToCategoryScreen>()
                .map {
                    navigator.goToCategoryScreen()
                    BusinessContract.PartialState.NoChange
                },

            canShowMultipleAccountEntry()
        )
    }

    private fun observableLoadForMerchantData() = intent<BusinessContract.Intent.Load>()
        .switchMap { UseCase.wrapCompletable(syncBusinessData.get().execute()) }
        .map { BusinessContract.PartialState.NoChange }

    private fun observeContextualHelpIdsOnLoad() = intent<BusinessContract.Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.MerchantScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map BusinessContract.PartialState.SetContextualHelpIds(it.value)
        }
        BusinessContract.PartialState.NoChange
    }

    private fun canShowMultipleAccountEntry() = intent<BusinessContract.Intent.Load>()
        .switchMap { wrap(isMultipleAccountEnabled.get().execute()) }
        .map {
            if (it is Result.Success) {
                return@map BusinessContract.PartialState.ShowMultipleAccountsEntry(it.value)
            }
            BusinessContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: BusinessContract.State,
        partialState: BusinessContract.PartialState,
    ): BusinessContract.State {
        return when (partialState) {
            is BusinessContract.PartialState.ErrorState -> currentState.copy(
                loading = false,
                error = true,
                networkError = false
            )
            is BusinessContract.PartialState.SetNetworkError -> currentState.copy(
                loading = false,
                error = false,
                networkError = true
            )
            is BusinessContract.PartialState.ShowBusiness -> currentState.copy(
                loading = false,
                error = false,
                networkError = false,
                business = partialState.business
            )
            is BusinessContract.PartialState.SetReferralData -> currentState.copy(referralId = partialState.referralId)
            is BusinessContract.PartialState.SetBusinessTypes ->
                currentState.copy(businessTypes = partialState.businessTypes)
            is BusinessContract.PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
            is BusinessContract.PartialState.ShowMultipleAccountsEntry -> currentState.copy(
                canShowMultipleAccountEntry = partialState.canShow
            )
            else -> currentState
        }
    }
}
