package tech.okcredit.home.ui.supplier_tab

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.home.usecase.GetActiveSuppliers

interface SupplierTabContract {

    object CONFIG {
        const val RESUME: String = "resume"
        const val PAUSE: String = "pause"
    }

    data class State(
        val supplier: GetActiveSuppliers.Response? = null,
        val unSyncSupplierIds: List<String> = arrayListOf(),
        val videoUrl1: String = "",
        val videoUrl2: String = "",
        val sourceScreen: String = "supplier_tab",
        val canShowSupplierTabVideo: Boolean = false,
        val nativeVideoState: String = CONFIG.PAUSE,
        val canShowSupplierKnowMore: Boolean = false,
        val isMerchantFromCollectionCampaign: Boolean = false,
        val chatCountMap: HashMap<String, Long>? = null,
        val canShowCarouselEducation: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class ShowSupplier(
            val suppliers: List<Supplier>,
            val tabCount: Int
        ) : PartialState()

        data class SetUnSyncSuppliers(val suppliers: List<String>) : PartialState()

        data class SetSupplierVideos(val video1: String, val video2: String) : PartialState()

        object NoChange : PartialState()

        data class CanShowSupplierTabVideo(val canShowSupplierTabVideo: Boolean) : PartialState()

        data class SetNativeVideoState(val nativeVideoState: String) : PartialState()

        data class IsMerchantFromCollectionCampaign(val isMerchantFromCollectionCampaign: Boolean) : PartialState()

        data class SetChatCountMap(val chatCountMap: HashMap<String, Long>) : PartialState()

        data class SetCanShowCarouselEducation(val canShow: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class OnProfileClick(val supplier: Supplier) : Intent()

        data class NativeVideoState(val state: String) : Intent()

        object OnVideoAttached : Intent()

        object SupplierLearnMore : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoLogin : ViewEvent()

        data class OpenSupplierProfileDialog(val supplier: Supplier) : ViewEvent()

        data class OpenSupplierPaymentDialog(val supplier: Supplier) : ViewEvent()

        data class GoToSupplierLearnMoreWebLink(val value: String) : ViewEvent()
    }

    interface Listeners {
        fun onSorted()
    }
}
