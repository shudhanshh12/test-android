package `in`.okcredit.frontend.ui.merchant_profile.merchantinput

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.usecase.merchant.GetAddress
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.BusinessErrors
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.UpdateBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class MerchantInputViewModel @Inject constructor(
    initialState: MerchantInputContract.State,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TYPE) var inputType: Int,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_TITLE) var inputTitle: String,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_MERCHANT_INPUT_VALUE) var inputVaue: String,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_CATEGORY_ID) var selectedCategoryId: String,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_LATITUDE) var latitude: Double,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_LONGITUDE) var longitude: Double,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_GPS) var gpsEnabled: Boolean,
    @ViewModelParam(MainActivityTranslucentFullScreen.ARG_IS_SOURCE_IN_APP_NOTIFICATION) var isSourceInAppNotification: Boolean,
    private val getAddress: GetAddress,
    private val updateBusiness: UpdateBusiness,
    private val tracker: Tracker,
    private val context: Context,
) : BaseViewModel<MerchantInputContract.State, MerchantInputContract.PartialState, MerchantInputContract.ViewEvent>(
    initialState
) {

    lateinit var email: String
    lateinit var about: String
    private lateinit var personalName: String
    private lateinit var categoryName: String
    lateinit var address: String

    private var inputSubject: PublishSubject<Triple<Int, String, String>> = PublishSubject.create()
    private var geoCoderSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var isGpsPermissionShownOnce = false
    private var centerPosition = false

    override fun handle(): Observable<UiState.Partial<MerchantInputContract.State>> {
        return mergeArray(

            intent<MerchantInputContract.Intent.Load>()
                .take(1)
                .map {
                    inputSubject.onNext(Triple(inputType, inputTitle, inputVaue))
                    if (isSourceInAppNotification && isGpsPermissionShownOnce.not()) {
                        emitViewEvent(MerchantInputContract.ViewEvent.ShowGpsPermission)
                        isGpsPermissionShownOnce = true
                    }
                    MerchantInputContract.PartialState.NoChange
                },

            intent<MerchantInputContract.Intent.UpdateGpsStatus>()
                .map {
                    gpsEnabled = it.gpsStatus
                    inputSubject.onNext(Triple(inputType, inputTitle, inputVaue))
                    MerchantInputContract.PartialState.NoChange
                },

            inputSubject
                .map {
                    emitViewEvent(
                        MerchantInputContract.ViewEvent.SetInputSubject(
                            Triple(
                                it.first,
                                it.second.trim(),
                                it.third.trim()
                            )
                        )
                    )

                    // some extra use cases for address
                    if (it.first == BusinessConstants.ADDRESS) {
                        geoCoderSubject.onNext(false)

                        val permissionGranted = Permission.isLocationPermissionAlreadyGranted(context)
                        if (permissionGranted && gpsEnabled) {
                            MerchantInputContract.PartialState.ShowMap
                        } else {
                            MerchantInputContract.PartialState.ShowGivePermission
                        }
                    } else {
                        MerchantInputContract.PartialState.HideMap
                    }
                },

            intent<MerchantInputContract.Intent.FetchGeocoderAddress>()
                .map {
                    latitude = it.latlong.latitude
                    longitude = it.latlong.longitude
                    geoCoderSubject.onNext(it.centerPosition)
                    MerchantInputContract.PartialState.NoChange
                },

            geoCoderSubject
                .switchMap {
                    centerPosition = it
                    getAddress.execute(GetAddress.Request(inputVaue, LatLng(latitude, longitude), centerPosition))
                }
                .map {

                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(
                                MerchantInputContract.ViewEvent.SetMapData(
                                    it.value.address,
                                    it.value.latlong,
                                    centerPosition,
                                    it.value.correctedLocation
                                )
                            )
                            MerchantInputContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when (it.error) {
                                is BusinessErrors.NoInternet -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }
                                is BusinessErrors.ServiceNotAvailable -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                                is BusinessErrors.LatLongNotFound -> {
                                    tracker.trackError(
                                        PropertyValue.MERCHANT_SCREEN,
                                        PropertyValue.INVALID_ADDRESS,
                                        it.error
                                    )
                                    MerchantInputContract.PartialState.SetlatLongError
                                }
                                else -> {
                                    tracker.trackError(
                                        PropertyValue.MERCHANT_SCREEN,
                                        PropertyValue.MAP_SERVER_ERROR,
                                        it.error
                                    )
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdateBusinessName>()
                .switchMap {
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                updatedValue = it.businessName
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            tracker.trackUpdateProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.NAME)
                            emitViewEvent(MerchantInputContract.ViewEvent.UpdatedSuccessfully())
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdateEmail>()
                .switchMap {
                    email = it.email
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                updatedValue = email
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (email.isBlank()) {
                                tracker.trackUpdateProfileV7(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.EMAIL,
                                    removed = PropertyValue.TRUE
                                )
                            } else {
                                tracker.trackUpdateProfileV1(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.EMAIL
                                )
                            }
                            emitViewEvent(MerchantInputContract.ViewEvent.UpdatedSuccessfully())
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                it.error is BusinessErrors.InvalidEmail -> MerchantInputContract.PartialState.InvalidEmailError

                                it.error is BusinessErrors.EmailAlreadyExist -> MerchantInputContract.PartialState.EmailAlreadyExistError

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdateAbout>()
                .switchMap {
                    about = it.about
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                updatedValue = about
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (about.isBlank()) {
                                tracker.trackUpdateProfileV7(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.ABOUT,
                                    removed = PropertyValue.TRUE
                                )
                            } else {
                                tracker.trackUpdateProfileV1(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.ABOUT
                                )
                            }
                            emitViewEvent(MerchantInputContract.ViewEvent.UpdatedSuccessfully())
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                it.error is BusinessErrors.InvalidAbout -> MerchantInputContract.PartialState.AboutError

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdatePersonName>()
                .switchMap {
                    personalName = it.personName
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                updatedValue = personalName
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (personalName.isBlank()) {
                                tracker.trackUpdateProfileV7(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.PERSONAL_NAME,
                                    removed = PropertyValue.TRUE
                                )
                            } else {
                                tracker.trackUpdateProfileV1(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.PERSONAL_NAME
                                )
                            }
                            emitViewEvent(MerchantInputContract.ViewEvent.UpdatedSuccessfully())
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdateCategory>()
                .switchMap {
                    categoryName = it.categoryName
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                category = Pair(selectedCategoryId, it.categoryName)
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (categoryName.isBlank()) {
                                tracker.trackUpdateProfileV6(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.CATEGORY,
                                    method = PropertyValue.OTHER,
                                    removed = PropertyValue.TRUE
                                )
                            } else {
                                tracker.trackUpdateProfileV6(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.CATEGORY,
                                    method = PropertyValue.OTHER,
                                    setValue = categoryName,
                                    removed = PropertyValue.FALSE
                                )
                            }
                            emitViewEvent(MerchantInputContract.ViewEvent.UpdatedSuccessfully())
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<MerchantInputContract.Intent.UpdateAddress>()
                .switchMap {
                    address = it.address.first
                    wrap(
                        updateBusiness.execute(
                            Request(
                                inputType = inputType,
                                address = it.address
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> MerchantInputContract.PartialState.ShowLoading
                        is Result.Success -> {
                            if (address.isBlank()) {
                                tracker.trackUpdateProfileV7(
                                    relation = PropertyValue.MERCHANT,
                                    field = PropertyValue.ADDRESS,
                                    removed = PropertyValue.TRUE
                                )
                            } else {
                                if (gpsEnabled) {
                                    tracker.trackUpdateProfileV2(
                                        relation = PropertyValue.MERCHANT,
                                        field = PropertyValue.ADDRESS,
                                        gps = PropertyValue.TRUE
                                    )
                                } else {
                                    tracker.trackUpdateProfileV2(
                                        relation = PropertyValue.MERCHANT,
                                        field = PropertyValue.ADDRESS,
                                        gps = PropertyValue.FALSE
                                    )
                                }
                            }
                            emitViewEvent(
                                MerchantInputContract.ViewEvent.UpdatedSuccessfully(shouldShowSuccessBottomSheetDialog = isSourceInAppNotification)
                            )
                            MerchantInputContract.PartialState.HideLoading
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(MerchantInputContract.ViewEvent.GoToLogin)
                                    MerchantInputContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MerchantInputContract.PartialState.SetNetworkError
                                }

                                it.error is BusinessErrors.InvalidAddress -> MerchantInputContract.PartialState.AddressError

                                else -> {
                                    MerchantInputContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: MerchantInputContract.State,
        partialState: MerchantInputContract.PartialState
    ): MerchantInputContract.State {
        return when (partialState) {
            is MerchantInputContract.PartialState.ShowLoading -> currentState.copy(
                isUpdating = true,
                error = false,
                networkError = false,
                invalidEmailError = false,
                emailAlreadyExistError = false,
                addressError = false,
                aboutError = false,
                latLongError = false
            )
            is MerchantInputContract.PartialState.HideLoading -> currentState.copy(isUpdating = false)
            is MerchantInputContract.PartialState.ShowMap -> currentState.copy(
                showMap = true,
                showGivePermission = false
            )
            is MerchantInputContract.PartialState.ShowGivePermission -> currentState.copy(
                showMap = false,
                showGivePermission = true
            )
            is MerchantInputContract.PartialState.HideMap -> currentState.copy(
                showMap = false,
                showGivePermission = false
            )
            is MerchantInputContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is MerchantInputContract.PartialState.ErrorState -> currentState.copy(isUpdating = true, error = true)
            is MerchantInputContract.PartialState.SetlatLongError -> currentState.copy(
                isUpdating = false,
                error = false,
                networkError = false,
                latLongError = true
            )
            is MerchantInputContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = true,
                isUpdating = true
            )
            is MerchantInputContract.PartialState.InvalidEmailError -> currentState.copy(
                isUpdating = true,
                error = false,
                networkError = false,
                invalidEmailError = true,
                emailAlreadyExistError = false,
                addressError = false,
                aboutError = false,
                latLongError = false
            )
            is MerchantInputContract.PartialState.EmailAlreadyExistError -> currentState.copy(
                isUpdating = true,
                error = false,
                networkError = false,
                invalidEmailError = false,
                emailAlreadyExistError = true,
                addressError = false,
                aboutError = false,
                latLongError = false
            )
            is MerchantInputContract.PartialState.AddressError -> currentState.copy(
                isUpdating = true,
                error = false,
                networkError = false,
                invalidEmailError = false,
                emailAlreadyExistError = false,
                addressError = true,
                aboutError = false,
                latLongError = false
            )
            is MerchantInputContract.PartialState.AboutError -> currentState.copy(
                isUpdating = true,
                error = false,
                networkError = false,
                invalidEmailError = false,
                emailAlreadyExistError = false,
                addressError = false,
                aboutError = true,
                latLongError = false
            )
            is MerchantInputContract.PartialState.NoChange -> currentState
        }
    }
}
