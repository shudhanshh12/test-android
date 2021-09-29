package `in`.okcredit.frontend.ui.merchant_profile.merchantinput

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputContract.State
import `in`.okcredit.frontend.ui.merchant_profile.merchantinput.MerchantInputContract.ViewEvent
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.merchant_profile_input.*
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.setStatusBarColor
import tech.okcredit.android.base.utils.GpsUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MerchantInputFragment :
    BaseFragment<State, ViewEvent, MerchantInputContract.Intent>("MerchantInputScreen"),
    OnMapReadyCallback {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    private var inputType = BusinessConstants.NONE

    private var gpsSubject = PublishSubject.create<Boolean>()
    private var businessNameSubject = PublishSubject.create<String>()
    private var emailSubject = PublishSubject.create<String>()
    private var aboutSubject = PublishSubject.create<String>()
    private var personNameSubject = PublishSubject.create<String>()
    private var otherCategorySubject = PublishSubject.create<String>()
    private var geocoderCenterPositionSubject = PublishSubject.create<Pair<LatLng, Boolean>>()
    private var addressSubject = PublishSubject.create<Triple<String, Double, Double>>() // Triple<address, lat, long>

    @Inject
    lateinit var tracker: Tracker

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.merchant_profile_input, null, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initKeyboardListener()
        setTextChangedListener()
        setClickListeners()
        initLocation()
        clPopup.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return MerchantInputContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            gpsSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateGpsStatus(it)
                },

            businessNameSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateBusinessName(it)
                },

            emailSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateEmail(it)
                },

            aboutSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateAbout(it)
                },

            personNameSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdatePersonName(it)
                },

            otherCategorySubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateCategory(it)
                },

            addressSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.UpdateAddress(it)
                },

            geocoderCenterPositionSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    MerchantInputContract.Intent.FetchGeocoderAddress(it.first, it.second)
                }
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: MerchantInputContract.State) {

        if (state.isUpdating) {
            showUpdateLoader()
        }

        when {
            state.showMap -> showMapLayout()
            state.showGivePermission -> showGivePermissionLayout()
            else -> hideMapLayout()
        }

        when {
            state.networkError -> {
                enableError(true, getString(`in`.okcredit.frontend.R.string.err_network))
            }
            state.error -> {
                enableError(true, getString(`in`.okcredit.frontend.R.string.err_default))
            }
            state.invalidEmailError -> {
                enableError(true, getString(`in`.okcredit.frontend.R.string.invalid_email))
            }

            state.emailAlreadyExistError -> {
                enableError(true, getString(`in`.okcredit.frontend.R.string.email_already_exit))
            }

            state.addressError -> {
                enableError(true, getString(`in`.okcredit.frontend.R.string.invalid_address))
            }

            state.aboutError -> {
                enableError(true, "${getString(`in`.okcredit.frontend.R.string.about_should_not)}")
            }

            state.latLongError -> {
                showLatLongError()
            }

            else -> enableError(false)
        }
    }

    /****************************************************************
     * Navigation
     ****************************************************************/

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToLogin -> gotoLogin()
            is ViewEvent.ShowGpsPermission -> showGpsPermission()
            is ViewEvent.SetInputSubject -> setInputSubject(event.input)
            is ViewEvent.SetMapData -> setMapData(
                address = event.address,
                latlong = event.latLong,
                centerPosition = event.centerPosition,
                correctedLocation = event.correctedLocation
            )
            is ViewEvent.UpdatedSuccessfully -> updatedSuccessfully(event.shouldShowSuccessBottomSheetDialog)
        }
    }

    private fun setInputSubject(input: Triple<Int, String, String>) {
        inputType = input.first
        setInputConstraints(input.first, input.second, input.third)
    }

    @UiThread
    private fun gotoLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
    }

    @SuppressLint("CheckResult")
    @UiThread
    private fun updatedSuccessfully(shouldShowSuccessBottomSheetDialog: Boolean) {
        if (requireActivity().callingActivity != null) {
            val intent = Intent().apply {
                putExtra(INPUT_TYPE, inputType)
            }
            requireActivity().setResult(Activity.RESULT_OK, intent)
        }
        if (shouldShowSuccessBottomSheetDialog) {
            findNavController(this).navigate(R.id.merchant_address_confirmation_in_app_popup)
            Completable.timer(2, TimeUnit.SECONDS).subscribe {
                finishActivity()
            }
        } else {
            finishActivity()
        }
    }

    private fun finishActivity() {
        activity?.runOnUiThread {
            activity?.finish()
            activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun showGpsPermission() {
        activity?.let { it1 ->
            GpsUtils(it1)
                .turnGPSOn(object : GpsUtils.onGpsListener {
                    override fun gpsStatus(isGPSEnable: Boolean) {

                        disposable = Completable.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                // if clicked on rlGiveLocationPermission when 'lat long not found' is shown , then we get current location
                                if (getCurrentState().latLongError) {
                                    geocoderCenterPositionSubject.onNext(LatLng(0.0, 0.0) to true)
                                } else {
                                    gpsSubject.onNext(isGPSEnable)
                                }
                            }
                    }
                })
        }
    }

    /****************************************************************
     * UI
     ****************************************************************/

    private fun initKeyboardListener() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        KeyboardVisibilityEvent.setEventListener(activity as Activity) { isOpen ->
            when {
                isOpen -> {
                    if (inputType == BusinessConstants.ADDRESS) {

                        val params = ivGiveLocationPermission.layoutParams as RelativeLayout.LayoutParams
                        params.height = resources.getDimension(R.dimen.view_116dp).toInt()
                        ivGiveLocationPermission.layoutParams = params
                    }
                }
                else -> {
                    if (inputType != BusinessConstants.ADDRESS) {
                        onBackPressed()
                    } else {
                        val params = ivGiveLocationPermission.layoutParams as RelativeLayout.LayoutParams
                        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
                        ivGiveLocationPermission.layoutParams = params
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        fbSumit
            .setOnClickListener {
                when (inputType) {
                    BusinessConstants.BUSINESS_NAME -> {
                        businessNameSubject.onNext(etInput.text.toString().trim())
                    }

                    BusinessConstants.EMAIL -> {
                        emailSubject.onNext(etInput.text.toString().trim())
                    }

                    BusinessConstants.ABOUT -> {
                        aboutSubject.onNext(etInput.text.toString().trim())
                    }

                    BusinessConstants.PERSON_NAME -> {
                        personNameSubject.onNext(etInput.text.toString().trim())
                    }

                    BusinessConstants.OTHER_CATEGORY -> {
                        otherCategorySubject.onNext(etInput.text.toString().trim())
                    }

                    BusinessConstants.ADDRESS -> {
                        addressSubject.onNext(Triple(etInput.text.toString().trim(), latitude, longitude))
                    }
                }
            }

        val permissionClickListener = View.OnClickListener { view ->
            Permission.requestLocationPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.ADDRESS, true)
                    }

                    override fun onPermissionGranted() {
                        showGpsPermission()
                    }

                    override fun onPermissionDenied() {
                        tracker.trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.ADDRESS, false)
                    }
                }
            )
        }

        rlGiveLocationPermission.setOnClickListener(permissionClickListener)
        tvAllow.setOnClickListener(permissionClickListener)

        clPopupContainer.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setTextChangedListener() {
        etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                TransitionManager.beginDelayedTransition(clInput as ViewGroup)

                val text = editable.toString()

                if (text.isEmpty()) {
                    enableError(false)
                }

                when (inputType) {

                    BusinessConstants.BUSINESS_NAME -> {
                        showCharacterCount(text.length, BUSINESS_NAME_MAX_LIMIT)
                        if (text.isEmpty()) {
                            hideSubmit()
                        } else {
                            showSubmit()
                        }
                        if (text.length >= BUSINESS_NAME_MAX_LIMIT) {
                            enableError(true, "${getString(`in`.okcredit.frontend.R.string.name_should_not)}")
                            hideSubmit()
                        } else {
                            enableError(false)
                            showSubmit()
                        }
                    }

                    BusinessConstants.ADDRESS -> {
                        showCharacterCount(text.length, ADDRESS_MAX_LIMIT)
                        if (text.isNotEmpty() && text.length >= ADDRESS_MAX_LIMIT) {
                            enableError(true, "${getString(`in`.okcredit.frontend.R.string.address_should_not)}")
                            hideSubmit()
                        } else {
                            enableError(false)
                            showSubmit()
                        }
                    }
                    BusinessConstants.EMAIL -> {
                        showCharacterCount(text.length, EMAIL_MAX_LIMIT)
                        if (text.isNotEmpty() && text.length >= EMAIL_MAX_LIMIT) {
                            enableError(true, "${getString(`in`.okcredit.frontend.R.string.email_should_not)}")
                            hideSubmit()
                        } else {
                            enableError(false)
                            showSubmit()
                        }
                    }

                    BusinessConstants.ABOUT -> {
                        showCharacterCount(text.length, ABOUT_MAX_LIMIT)
                        if (text.isNotEmpty() && text.length >= ABOUT_MAX_LIMIT) {
                            enableError(true, "${getString(`in`.okcredit.frontend.R.string.about_should_not)}")
                            hideSubmit()
                        } else {
                            enableError(false)
                            showSubmit()
                        }
                    }

                    BusinessConstants.OTHER_CATEGORY -> {
                        showCharacterCount(text.length, CATEGORY_MAX_LIMIT)
                        when {
                            text.length >= CATEGORY_MAX_LIMIT -> {
                                editable?.delete(CATEGORY_MAX_LIMIT, editable?.length)
                                enableError(true, "${getString(`in`.okcredit.frontend.R.string.category_name_limit)}")
                                hideSubmit()
                            }

                            else -> {
                                enableError(false)
                                showSubmit()
                            }
                        }
                    }

                    BusinessConstants.PERSON_NAME -> {
                        showCharacterCount(text.length, PERSONAL_NAME_MAX_LIMIT)
                        if (text.isNotEmpty() && text.length >= PERSONAL_NAME_MAX_LIMIT) {
                            enableError(true, "${getString(`in`.okcredit.frontend.R.string.name_should_not)}")
                            hideSubmit()
                        } else {
                            enableError(false)
                            showSubmit()
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setInputConstraints(inputType: Int, intputTitle: String, intputValue: String) {

        when (inputType) {

            BusinessConstants.BUSINESS_NAME -> {
                if (isAdded) setStatusBarColor(R.color.trasparent_black)

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(`in`.okcredit.frontend.R.string.enter_business_name_box)

                if (intputValue == getString(`in`.okcredit.frontend.R.string.enter_business_name)) { // This is not business name , so setting null text
                    etInput.text = null
                } else {
                    etInput.setText(intputValue)
                }

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(BUSINESS_NAME_MAX_LIMIT))
                etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(1)
                etInput.setSelection(etInput.length()) // cursor position
            }

            BusinessConstants.ADDRESS -> {
                if (isAdded) setStatusBarColor(R.color.indigo_lite)

                etInput.clearFocus() // not showing keyboard , so that full size map is displayed

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(R.string.enter_location_box)
                etInput.setText(intputValue)

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(ADDRESS_MAX_LIMIT))
                etInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(3)
                etInput.maxLines = 3
                etInput.setSelection(0) // cursor position

                showSubmit()
            }

            BusinessConstants.EMAIL -> {
                if (isAdded) setStatusBarColor(R.color.trasparent_black)

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(`in`.okcredit.frontend.R.string.enter_email_box)
                etInput.setText(intputValue)

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(EMAIL_MAX_LIMIT))
                etInput.inputType =
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(1)
                etInput.setSelection(etInput.length()) // cursor position

                showSubmit()
            }

            BusinessConstants.ABOUT -> {
                if (isAdded) setStatusBarColor(R.color.trasparent_black)

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(`in`.okcredit.frontend.R.string.enter_about_business_box)
                etInput.setText(intputValue)

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(ABOUT_MAX_LIMIT))
                etInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(1)
                etInput.maxLines = 4
                etInput.setSelection(0) // cursor position

                showSubmit()
            }

            BusinessConstants.PERSON_NAME -> {
                if (isAdded) setStatusBarColor(R.color.trasparent_black)

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(`in`.okcredit.frontend.R.string.enter_person_name_box)
                etInput.setText(intputValue)

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(PERSONAL_NAME_MAX_LIMIT))
                etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(1)
                etInput.setSelection(etInput.length()) // cursor position

                showSubmit()
            }

            BusinessConstants.OTHER_CATEGORY -> {
                if (isAdded) setStatusBarColor(R.color.trasparent_black)

                // Text
                tvTitle.text = intputTitle
                etInput.setHint(`in`.okcredit.frontend.R.string.enter_other_category_name_box)
                etInput.setText(intputValue)

                // Input filters
                etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(CATEGORY_MAX_LIMIT))
                etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                etInput.setLines(1)
                etInput.setSelection(etInput.length()) // cursor position
            }
        }
    }

    private fun initLocation() {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapSupportFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    internal fun showSubmit() {
        clSubmit.visibility = View.VISIBLE
        fbSumit.show()
    }

    internal fun hideSubmit() {
        clSubmit.visibility = View.GONE
        fbSumit.hide()
    }

    internal fun showCharacterCount(userInputCount: Int, maxCount: Int) {
        tvCount.visibility = View.VISIBLE
        tvCount.text = "$userInputCount/${maxCount - 1}"
        when (userInputCount) {
            maxCount -> tvCount.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
            else -> tvCount.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        }
    }

    internal fun enableError(enable: Boolean, errorMessage: String? = null) {
        if (enable) {
            tvErrorMessage.text = errorMessage
            tvErrorMessage.visibility = View.VISIBLE
            hideUpdateLoader()
        } else {
            tvErrorMessage.visibility = View.GONE
        }
    }

    private fun showLatLongError() {
        showGivePermissionLayout()
        rlGiveLocationPermission.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivGiveLocationPermission.setImageResource(R.drawable.im_no_lat_long)
        tvMapBusiness.text = getString(R.string.title_location_not_found)
        tvAllowLocation.text = getString(R.string.location_not_found_map)
        tvAllow.text = getString(R.string.set_map_location)
    }

    private fun showUpdateLoader() {
        progressBar.visibility = View.VISIBLE
        fbSumit.hide()
    }

    private fun hideUpdateLoader() {
        progressBar.visibility = View.GONE
        fbSumit.show()
    }

    private fun showMapLayout() {
        clMap.visibility = View.VISIBLE
        rlGiveLocationPermission.visibility = View.GONE
    }

    private fun showGivePermissionLayout() {
        clMap.visibility = View.GONE
        rlGiveLocationPermission.visibility = View.VISIBLE

        rlGiveLocationPermission.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.indigo_lite))
        ivGiveLocationPermission.setImageResource(R.drawable.img_location)
        tvMapBusiness.text = getString(R.string.map_business)
        tvAllowLocation.text = getString(R.string.allow_map_location)
        tvAllow.text = getString(R.string.allow)
    }

    private fun hideMapLayout() {
        clMap.visibility = View.GONE
        rlGiveLocationPermission.visibility = View.GONE
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        enableMyLocation()

        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.uiSettings?.isZoomGesturesEnabled = true
        googleMap?.uiSettings?.isRotateGesturesEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        mapListener()

        changeMyLocationButtonToRightBottom()
    }

    private fun changeMyLocationButtonToRightBottom() {
        val locationButton = mapFragment.view?.findViewWithTag<View>("GoogleMapMyLocationButton")
        val rlp = locationButton?.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)
    }

    // when ,map is dragged by user , we get address and lat long from map center point
    private fun mapListener() {
        googleMap?.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    KeyboardVisibilityEvent.hideKeyboard(context as AppCompatActivity)
                    getMapCenterLanLong()
                }
                GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                    getMapCenterLanLong()
                }
                GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                    // googleMap?.setOnCameraIdleListener(null)
                }
            }
        }
    }

    private fun getMapCenterLanLong() {
        googleMap?.setOnCameraIdleListener {
            geocoderCenterPositionSubject.onNext(
                LatLng(
                    googleMap?.cameraPosition?.target?.latitude!!,
                    googleMap?.cameraPosition?.target?.longitude!!
                ) to true
            )
        }
    }

    private fun setMapData(address: String?, latlong: LatLng, centerPosition: Boolean, correctedLocation: Boolean) {
        this.latitude = latlong.latitude
        this.longitude = latlong.longitude
        etInput?.setText(address)

        AnimationUtils.bounce(ivLocation)

        // centerPosition ->if map was dragged by user to center of the 'Marker' , then we don't call moveMapCamera(latlng)
        // because map is already displaying the location , as user dragged it

        if (correctedLocation) {
            showMapLayout()
        } else if (centerPosition) {
            return
        }

        moveMapCamera(latlong)

        //  This hack , some device doesn't moves to lat long
        // so , calling moveMapCamera(latlng) once again
        disposable = Completable.timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.i(it) }
            .subscribe {
                moveMapCamera(latlong)
            }
    }

    @UiThread
    private fun moveMapCamera(latlong: LatLng) {
        try {
            enableMyLocation()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latlong))
            googleMap?.animateCamera(CameraUpdateFactory.zoomTo(15F), 500, null)
        } catch (e: Exception) {
            Timber.i(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (Permission.isLocationPermissionAlreadyGranted(activity as AppCompatActivity)) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    //    private fun addMarker(latlong: LatLng) {
//        val markerOptions = MarkerOptions()
//            .position(latlong)
//            .icon(Utils.generateBitmapDescriptorFromRes(requireContext(), R.drawable.ic_location))
//        googleMap?.clear()
//        googleMap?.addMarker(markerOptions)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GpsUtils.GPS_REQUEST) {
            disposable = Completable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (resultCode == Activity.RESULT_OK) {
                        gpsSubject.onNext(true)
                    } else {
                        gpsSubject.onNext(false)
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    companion object {
        const val BUSINESS_NAME_MAX_LIMIT = 501
        const val PERSONAL_NAME_MAX_LIMIT = 501
        const val CATEGORY_MAX_LIMIT = 51
        const val ABOUT_MAX_LIMIT = 51
        const val ADDRESS_MAX_LIMIT = 151
        const val EMAIL_MAX_LIMIT = 41

        const val INPUT_TYPE = "input_type"
    }

    override fun onBackPressed(): Boolean {
        if (requireActivity().callingActivity != null) {
            requireActivity().setResult(Activity.RESULT_CANCELED)
        }
        finishActivity()
        return true
    }
}
