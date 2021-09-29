package `in`.okcredit.frontend.ui.merchant_profile.merchantinput

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import com.google.android.gms.maps.model.LatLng

interface MerchantInputContract {

    data class State(
        val isUpdating: Boolean = false,
        val showMap: Boolean = false,
        val showGivePermission: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val latLongError: Boolean = false,
        val invalidEmailError: Boolean = false,
        val emailAlreadyExistError: Boolean = false,
        val addressError: Boolean = false,
        val aboutError: Boolean = false,
        val gps: Boolean = false,
        val latlong: LatLng? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ShowMap : PartialState()

        object ShowGivePermission : PartialState()

        object HideLoading : PartialState()

        object HideMap : PartialState()

        object ErrorState : PartialState()

        object NoChange : PartialState()

        object SetNetworkError : PartialState()

        object SetlatLongError : PartialState()

        object ClearNetworkError : PartialState()

        object InvalidEmailError : PartialState()

        object EmailAlreadyExistError : PartialState()

        object AddressError : PartialState()

        object AboutError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        data class UpdateGpsStatus(val gpsStatus: Boolean) : Intent()

        data class UpdateBusinessName(val businessName: String) : Intent()

        data class UpdateEmail(val email: String) : Intent()

        data class UpdateAbout(val about: String) : Intent()

        data class UpdatePersonName(val personName: String) : Intent()

        data class UpdateCategory(val categoryName: String) : Intent()

        data class UpdateAddress(val address: Triple<String, Double, Double>) : Intent()

        data class FetchGeocoderAddress(val latlong: LatLng, val centerPosition: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToLogin : ViewEvent()
        data class UpdatedSuccessfully(val shouldShowSuccessBottomSheetDialog: Boolean = false) : ViewEvent()
        data class SetInputSubject(val input: Triple<Int, String, String>) : ViewEvent()

        /**
         * @param address  : address of the merchant
         *
         * @param latlong : latitude & longitude
         *
         * @param centerPosition : when user drags the map to the marker , we get center location
         *
         * @param correctedLocation : when user entered invalid address , we show lat long not found and ask to to get proper location
         *
         */
        data class SetMapData(
            val address: String?,
            val latLong: LatLng,
            val centerPosition: Boolean = false,
            val correctedLocation: Boolean
        ) : ViewEvent()

        object ShowGpsPermission : ViewEvent()
    }
}
