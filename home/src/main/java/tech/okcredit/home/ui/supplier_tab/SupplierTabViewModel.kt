package tech.okcredit.home.ui.supplier_tab

import `in`.okcredit.collection.contract.IsCollectionCampaignMerchant
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.ui.payables_onboarding.GetCarouselVisibility
import tech.okcredit.home.ui.supplier_tab.SupplierTabContract.*
import tech.okcredit.home.usecase.GetActiveSuppliers
import tech.okcredit.home.usecase.GetCanShowSupplierTabVideo
import tech.okcredit.home.usecase.GetShouldAutoPlaySupplierVideo
import tech.okcredit.home.usecase.GetSupplierEducationVideoIds
import tech.okcredit.home.usecase.GetSupplierKnowMoreWebLink
import tech.okcredit.home.usecase.GetUnSyncSupplier
import javax.inject.Inject

class SupplierTabViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("notification_url") val notificationUrl: Lazy<String>,
    private val getActiveSuppliers: Lazy<GetActiveSuppliers>,
    private val getUnSyncedSuppliers: Lazy<GetUnSyncSupplier>,
    private val getSupplierEducationVideoIds: Lazy<GetSupplierEducationVideoIds>,
    private val getCanShowSupplierTabVideo: Lazy<GetCanShowSupplierTabVideo>,
    private val getShouldAutoPlaySupplierVideo: Lazy<GetShouldAutoPlaySupplierVideo>,
    private val getSupplierKnowMoreWebLink: GetSupplierKnowMoreWebLink,
    private val isMerchantFromCollectionCampaign: Lazy<IsCollectionCampaignMerchant>,
    private val getCarouselVisibility: Lazy<GetCarouselVisibility>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            canShowCarouselEducation(),

            intent<Intent.Load>()
                .switchMap { wrap(getActiveSuppliers.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.ShowSupplier(
                                suppliers = it.value.suppliers,
                                tabCount = it.value.tabCount
                            )
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            /***********************   Load UnSynced Suppliers  ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(getUnSyncedSuppliers.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetUnSyncSuppliers(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            /*********************** Load tutorial video ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(getSupplierEducationVideoIds.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetSupplierVideos(it.value.first, it.value.second)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            getUtmCampaignForNewUsersObservable(),

            profileClickObservable(),

            intent<Intent.Load>()
                .switchMap { getCanShowSupplierTabVideo.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.CanShowSupplierTabVideo(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.NativeVideoState>()
                .map { PartialState.SetNativeVideoState(it.state) },

            intent<Intent.OnVideoAttached>()
                .switchMap { wrap(getShouldAutoPlaySupplierVideo.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value) {
                                PartialState.SetNativeVideoState(CONFIG.RESUME)
                            } else PartialState.NoChange
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.SupplierLearnMore>()
                .switchMap { wrap(getSupplierKnowMoreWebLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress ->
                            PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GoToSupplierLearnMoreWebLink(it.value))
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },
        )
    }

    private fun canShowCarouselEducation() = intent<Intent.Load>()
        .switchMap { wrap(getCarouselVisibility.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> PartialState.SetCanShowCarouselEducation(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun getUtmCampaignForNewUsersObservable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(isMerchantFromCollectionCampaign.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.IsMerchantFromCollectionCampaign(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun profileClickObservable(): Observable<PartialState> {
        return intent<Intent.OnProfileClick>()
            .map {
                emitViewEvent(ViewEvent.OpenSupplierProfileDialog(it.supplier))
                PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState

            is PartialState.ShowSupplier -> currentState.copy(
                supplier = GetActiveSuppliers.Response(
                    suppliers = partialState.suppliers,
                    tabCount = partialState.tabCount
                )
            )
            is PartialState.SetUnSyncSuppliers -> currentState.copy(
                unSyncSupplierIds = partialState.suppliers
            )
            is PartialState.SetSupplierVideos -> currentState.copy(
                videoUrl1 = partialState.video1,
                videoUrl2 = partialState.video2
            )
            is PartialState.CanShowSupplierTabVideo -> currentState.copy(
                canShowSupplierTabVideo = partialState.canShowSupplierTabVideo
            )
            is PartialState.SetNativeVideoState -> currentState.copy(
                nativeVideoState = partialState.nativeVideoState
            )
            is PartialState.IsMerchantFromCollectionCampaign -> currentState.copy(
                isMerchantFromCollectionCampaign = partialState.isMerchantFromCollectionCampaign
            )
            is PartialState.SetChatCountMap -> currentState.copy(
                chatCountMap = partialState.chatCountMap
            )
            is PartialState.SetCanShowCarouselEducation -> currentState.copy(
                canShowCarouselEducation = partialState.canShow
            )
        }
    }
}
