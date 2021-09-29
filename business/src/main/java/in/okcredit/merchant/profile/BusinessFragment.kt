package `in`.okcredit.merchant.profile

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.BusinessEvents
import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.BusinessTypeListener
import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.merchant.databinding.MerchantProfileFragmentBinding
import `in`.okcredit.merchant.utils.CommonUtils
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import com.yalantis.ucrop.UCrop
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.media_select_sheet.*
import kotlinx.android.synthetic.main.merchant_profile_fragment.*
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.GpsUtils
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.userSupport.SupportRepository
import zendesk.belvedere.Belvedere
import zendesk.belvedere.Callback
import zendesk.belvedere.MediaResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BusinessFragment :
    BaseScreen<BusinessContract.State>(
        "MerchantScreen",
        R.layout.merchant_profile_fragment
    ),
    BusinessContract.Navigator,
    BusinessShareDialog.Listener {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val binding by viewLifecycleScoped(MerchantProfileFragmentBinding::bind)

    internal var businessTypeSubject = PublishSubject.create<BusinessType>() // Pair<categoryId,otherCategoryName>

    private var profileImageSubject = PublishSubject.create<Pair<Boolean, String>>()
    private var profileImageBottomSheetSubject = PublishSubject.create<Boolean>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>

    private var cameraImage = false

    @Inject
    internal lateinit var businessNavigator: Lazy<BusinessNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var imageLoader: Lazy<IImageLoader>

    private var disposable: Disposable? = null

    private var showFullScreenImage: Boolean = false

    @Inject
    lateinit var legacyNavigator: LegacyNavigator

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    companion object {
        const val ARG_SETUP_PROFILE = "set up profile"
        const val ARG_SHARE_BUSINESS_CARD = "share business card"
        const val ARG_SHOW_MERCHANT_PROFILE = "show merchant profile image"
        const val ARG_SHOW_MERCHANT_LOCATION = "show merchant location"
        const val ARG_SHOW_BUSINESS_TYPE_BOTTOM_SHEET = "show business type bottom sheet"
        const val ARG_SHOW_CATEGORY_SCREEN = "show category screen"

        const val RESULT_LOAD_IMG_CROP = 100
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        rootView.setTracker(performanceTracker)
    }

    private fun initViews() {

        contextual_help.initDependencies(
            screenName = ScreenName.MerchantScreen.value,
            tracker = tracker.get(),
            legacyNavigator = legacyNavigator
        )

        bottomSheetBehavior = BottomSheetBehavior.from(rlMediaSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        initClickListener()
    }

    private fun initClickListener() {
        binding.imgToolbarBusiness.setOnClickListener {
            businessNavigator.get().showSwitchBusinessDialog(childFragmentManager, BusinessEvents.Value.POST_PROFILE_SECTION)
        }

        clBusinessName.setOnClickListener {
            tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.NAME)
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.BUSINESS_NAME,
                inputTitle = tvTitleBusinessName.text.toString(),
                inputValue = tvValueBusinessName.text.toString()
            )
        }

        clMobile.setOnClickListener {
            tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.MOBILE)
            startActivity(legacyNavigator.getNumberChangeScreenIntent())
        }

        clAddress.setOnClickListener {
            tracker.get().trackViewProfile(PropertyValue.MERCHANT, PropertyValue.ADDRESS)
            Permission.requestLocationPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.ADDRESS, true)
                    }

                    override fun onPermissionGranted() {
                        GpsUtils(requireActivity()).turnGPSOn(object : GpsUtils.onGpsListener {
                            override fun gpsStatus(isGPSEnable: Boolean) {
                                legacyNavigator.goToMerchantInputScreen(
                                    context = requireContext(),
                                    inputType = BusinessConstants.ADDRESS,
                                    inputTitle = tvTitleAddress.text.toString(),
                                    inputValue = tvValueAddress.text.toString(),
                                    latitude = latitude,
                                    longitude = longitude,
                                    gps = isGPSEnable
                                )
                            }
                        })
                    }

                    override fun onPermissionDenied() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.ADDRESS, false)
                        legacyNavigator.goToMerchantInputScreen(
                            context = requireContext(),
                            inputType = BusinessConstants.ADDRESS,
                            inputTitle = tvTitleAddress.text.toString(),
                            inputValue = tvValueAddress.text.toString(),
                            latitude = latitude,
                            longitude = longitude,
                            gps = false
                        )
                    }
                }
            )
        }

        clEmail.setOnClickListener {
            tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.EMAIL)
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.EMAIL,
                inputTitle = tvTitleEmail.text.toString(),
                inputValue = tvValueEmail.text.toString()
            )
        }

        clAbout.setOnClickListener {
            tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.ABOUT)
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.ABOUT,
                inputTitle = tvTitleAbout.text.toString(),
                inputValue = tvValueAbout.text.toString()
            )
        }

        clContactPersonName.setOnClickListener {
            tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.PERSONAL_NAME)
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.PERSON_NAME,
                inputTitle = tvTitleContactPersonName.text.toString(),
                inputValue = tvValueContactPersonName.text.toString()
            )
        }

        clOtherInfoHeader
            .setOnClickListener {
                if (clOtherInfoContent.visibility == View.VISIBLE) {
                    AnimationUtils.collapse(ivOthersExpandCollapse)
                    clOtherInfoContent.visibility = View.GONE
                } else {
                    AnimationUtils.expand(ivOthersExpandCollapse)
                    clOtherInfoContent.visibility = View.VISIBLE
                }
            }

        clShareBusinessCard.setOnClickListener {
            tracker.get().track(Event.SHARE_BUSINESS_CARD)
            getCurrentState().business?.let {
                BusinessShareDialog.showBusinessCard(context as Activity, it, this)
            }
        }

        llImageContainer.setOnClickListener {
            profileImageBottomSheetSubject.onNext(true)
        }

        ivAddPhoto.setOnClickListener {
            profileImageBottomSheetSubject.onNext(false)
        }

        rlMediaSheet.setOnClickListener {
            hideBottomSheet()
        }

        fbGallery.setOnClickListener {
            cameraImage = false
            Permission.requestStoragePermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.STORAGE, true)
                    }

                    override fun onPermissionGranted() {

                        Belvedere.from(requireActivity())
                            .document()
                            .contentType("image/*")
                            .allowMultiple(false)
                            .open(context as Activity)
                    }

                    override fun onPermissionDenied() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.STORAGE, false)
                    }
                }
            )
        }

        fbCamera.setOnClickListener {
            cameraImage = true
            Permission.requestStorageAndCameraPermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.STORAGE, true)
                    }

                    override fun onPermissionGranted() {

                        if (CommonUtils.hasCamera(requireContext())) {
                            Belvedere.from(requireActivity())
                                .camera()
                                .open(context as Activity)
                        } else {
                            // TODO we should replace Belvedere with stable Camera X
                            longToast(R.string.camera_not_available)
                            RecordException.recordException(Exception("Merchant Screen: No Camera available"))
                        }
                    }

                    override fun onPermissionDenied() {
                        tracker.get().trackRuntimePermission(PropertyValue.MERCHANT, PropertyValue.STORAGE, false)
                    }
                }
            )
        }

        fbDelete.setOnClickListener {
            DeleteProfilePhotoConfirmDialog.show(
                context as Activity,
                object : DeleteProfilePhotoConfirmDialog.Listener {
                    override fun onDeletePhoto() {
                        profileImageSubject.onNext(cameraImage to "") // deleting image
                        slideBottomSheetDown()
                    }
                }
            )
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    override fun loadIntent(): UserIntent {
        return BusinessContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {

        return Observable.mergeArray(

            businessTypeSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BusinessContract.Intent.UpdateBusiness(it)
                },

            profileImageSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BusinessContract.Intent.UpdateProfileImage(it)
                },

            profileImageBottomSheetSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BusinessContract.Intent.ShowProfileBottomSheet(it)
                },

            clCategoryHeader.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    tracker.get()
                        .trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.CATEGORY)
                    BusinessContract.Intent.GoToCategoryScreen
                }
        )
    }

    @AddTrace(name = Traces.RENDER_MERCHANT_PROFILE)
    override fun render(state: BusinessContract.State) {
        when {
            state.networkError -> {
                view?.snackbar(getString(R.string.no_internet_msg), Snackbar.LENGTH_LONG)?.show()
            }
            state.error -> {
                view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)?.show()
            }
        }

        if (state.business == null) return

        if (state.businessTypes.isNotEmpty()) {
            businessTypeContainer.visibility = View.VISIBLE
        } else {
            businessTypeContainer.visibility = View.GONE
        }

        if (state.business.businessType?.name.isNullOrBlank()) {
            descBusinessType.text = getString(R.string.select_your_business_type)
        } else {
            descBusinessType.text = state.business.businessType?.name
        }

        businessTypeContainer.setOnClickListener {
            showBusinessTypeBottomSheet(state)
        }

        if (state.business.category?.name.isNullOrBlank()) {
            tvValueCategory.text = getString(R.string.enter_category)
        } else {
            tvValueCategory.text = state.business.category?.name
        }

        contextual_help.setContextualHelpIds(state.contextualHelpIds)

        if (state.business.name == state.business.mobile) {
            tvValueBusinessName.text = getString(R.string.enter_business_name)
        } else {
            tvValueBusinessName.text = state.business.name
        }

        tvValueMobileNumber.text = state.business.mobile
        tvValueAddress.text = state.business.address
        tvValueEmail.text = state.business.email
        tvValueAbout.text = state.business.about
        tvValueContactPersonName.text = state.business.contactName

        imageLoader.get().context(activity as AppCompatActivity)
            .load(state.business.profileImage)
            .placeHolder(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account_125dp)!!)
            .scaleType(IImageLoader.CIRCLE_CROP)
            .into(photo_image_view)
            .build()

        if (state.business.profileImage.isNullOrBlank()) {
            hideEditPhotoOptions()
        } else {
            showEditPhotoOptions()
        }

        if (state.business.profileImage.isNullOrEmpty()) {
            ivAddPhoto.setImageResource(R.drawable.ic_camera_round)
        } else {
            ivAddPhoto.setImageResource(R.drawable.ic_photo_edit)
        }

        state.business.addressLatitude?.let { latitude = it }
        state.business.addressLongitude?.let { longitude = it }
        multipleAccountsEntry(state)
    }

    private fun showBusinessTypeBottomSheet(state: BusinessContract.State) {
        tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.BUSINESS_TYPE)
        val businessTypeBottomSheet = BusinessTypeBottomSheetDialog.newInstance()
        businessTypeBottomSheet.initialise(
            object : BusinessTypeListener {
                override fun onSelectBusinessType(type: BusinessType) {
                    businessTypeSubject.onNext(type)
                }
            },
            state.business?.businessType?.id ?: "", state.businessTypes
        )

        businessTypeBottomSheet.show(
            requireActivity().supportFragmentManager,
            BusinessTypeBottomSheetDialog.TAG
        )
    }

    /****************** Navigation *****************/

    override fun gotoLogin() {
        startActivity(legacyNavigator.getWelcomeLanguageScreenIntent())
        activity?.finishAffinity()
    }

    override fun goToCategoryScreen() {
        startActivity(legacyNavigator.getCategoryScreenIntent())
    }

    override fun gotoSetupProfile() {
        activity?.runOnUiThread {
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.BUSINESS_NAME,
                inputTitle = tvTitleBusinessName.text.toString(),
                inputValue = tvValueBusinessName.text.toString()
            )
        }
    }

    override fun shareBusinessCard() {
        activity?.runOnUiThread {
            BusinessShareDialog.showBusinessCard(context as Activity, getCurrentState().business!!, this)
        }
    }

    override fun showProfileImageBottomSheet(showFullScreenImage: Boolean) {
        this.showFullScreenImage = showFullScreenImage
        activity?.runOnUiThread {
            if (showFullScreenImage) {
                tracker.get().trackSelectProfileV1(relation = PropertyValue.MERCHANT, field = PropertyValue.PHOTO)
                showFullScreenPhoto(true)
                slideBottomSheetUp()
            } else {
                showFullScreenPhoto(false)
                slideBottomSheetUp()
            }
        }
    }

    override fun showLocationDialog() {
        activity?.runOnUiThread {
            clAddress.performClick()
        }
    }

    override fun openBusinessTypeBottomSheet() {
        activity?.runOnUiThread {
            if (isStateInitialized()) {
                showBusinessTypeBottomSheet(getCurrentState())
            } else {
                longToast(R.string.err_default)
            }
        }
    }

    /****************** Callbacks *****************/

    override fun onBusinessCardShare(bitmap: Bitmap) {

        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        val storageDir = File(
            context?.getExternalFilesDir(null),
            "reminder_images"
        )

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val fileName = UUID.randomUUID().toString() + "image.jpg"

        val reminderImage = File(context?.getExternalFilesDir(null), "reminder_images/$fileName")

        try {
            val fo = FileOutputStream(reminderImage)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: IOException) {
            tracker.get().trackError(Event.SELECT_PROFILE, PropertyValue.IMAGE_WRITE_FAILED, "", PropertyValue.MERCHANT)
            shortToast(getString(R.string.err_default))
            e.printStackTrace()
            return
        }

        try {
            val reminderUri =
                FileProvider.getUriForFile(requireContext(), context?.packageName + ".provider", reminderImage)

            val sendIntent = Intent("android.intent.action.MAIN")
            sendIntent.putExtra(Intent.EXTRA_STREAM, reminderUri)
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "${getString(R.string.business_card_share_text)} \n ${getString(R.string.share_msg)} ${getCurrentState().referralId}"
            )

            sendIntent.action = Intent.ACTION_SEND
            sendIntent.type = "image/png"
            startActivity(sendIntent)
        } catch (e: Exception) {
            tracker.get().trackError(Event.SELECT_PROFILE, PropertyValue.WHATS_APP_ERROR, "", PropertyValue.MERCHANT)
            shortToast(R.string.err_default)
            e.printStackTrace()
            return
        }
    }

    private fun slideBottomSheetUp() {
        AnimationUtils.fadeInV1(flDim)
        KeyboardVisibilityEvent.hideKeyboard(context as Activity)
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.peekHeight = 100
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun slideBottomSheetDown(): Boolean {
        AnimationUtils.fadeOutV1(flDim)
        var wasExpanded = false
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            wasExpanded = true
            bottomSheetBehavior.peekHeight = 0
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return wasExpanded
    }

    private fun showEditPhotoOptions() {
        fbDelete.show()
        tvDelete.visibility = View.VISIBLE
        viewBlank.visibility = View.VISIBLE
    }

    private fun hideEditPhotoOptions() {
        fbDelete.hide()
        tvDelete.visibility = View.GONE
        viewBlank.visibility = View.GONE
    }

    // If bottom sheet is not in full screen mode, then we hide sheet
    private fun hideBottomSheet() {
        if (showFullScreenImage) {
            val profileImageAvailable =
                isStateInitialized() && getCurrentState().business!!.profileImage.isNullOrBlank().not()
            if (profileImageAvailable.not()) {
                slideBottomSheetDown()
            }
        } else {
            slideBottomSheetDown()
        }
    }

    private fun showFullScreenPhoto(show: Boolean) {
        if (show) {
            val profileImageAvailable = getCurrentState().business?.profileImage.isNullOrBlank().not()
            if (profileImageAvailable && ivProfilePhotoFull != null) {
                GlideApp.with(requireActivity())
                    .load(getCurrentState().business?.profileImage)
                    .placeholder(getDrawableCompact(R.drawable.ic_account_125dp))
                    .centerInside()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            e?.let { RecordException.recordException(it) }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            return false
                        }
                    })
                    .into(ivProfilePhotoFull)
                ivProfilePhotoFull.visible()
            } else {
                ivProfilePhotoFull.gone()
            }
        } else {
            ivProfilePhotoFull.gone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GpsUtils.GPS_REQUEST) {
            legacyNavigator.goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.ADDRESS,
                inputTitle = tvTitleAddress.text.toString(),
                inputValue = tvValueAddress.text.toString(),
                latitude = latitude,
                longitude = longitude,
                gps = resultCode == Activity.RESULT_OK
            )
        } else if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMG_CROP) {
            if (data != null) {
                val resultUri = UCrop.getOutput(data)
                if (resultUri != null && resultUri.path != null) {
                    slideBottomSheetDown()
                    lifecycleScope.launchWhenResumed {
                        profileImageSubject.onNext(cameraImage to (resultUri.path ?: ""))
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            Belvedere.from(requireActivity())
                .getFilesFromActivityOnResult(
                    requestCode, resultCode, data,
                    object : Callback<List<MediaResult>>() {
                        override fun success(result: List<MediaResult>?) {
                            if (result != null && result.isNotEmpty()) {
                                val uri = Uri.fromFile(result[0].file)
                                val file = File(
                                    context?.cacheDir,
                                    String.format("profileImage_%s.jpg", System.currentTimeMillis().toString())
                                )
                                val destinationUri = Uri.fromFile(file)

                                val options = UCrop.Options()
                                options.setToolbarTitle(getString(R.string.crop_image))
                                options.setStatusBarColor(resources.getColor(R.color.primary))
                                options.setToolbarColor(resources.getColor(R.color.primary))

                                UCrop.of(Uri.parse(uri.toString()), destinationUri)
                                    .withAspectRatio(1f, 1f)
                                    .withOptions(options)
                                    .start(context as Activity, RESULT_LOAD_IMG_CROP)
                            }
                        }
                    }
                )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onBackPressed(): Boolean {
        return slideBottomSheetDown() // if bottom sheet is opened , it will close it , or else will go back
    }

    private fun loadBusiness(business: Business?) {
        if (business == null) return
        val defaultPic = TextDrawableUtils.getRoundTextDrawable(business.name)
        Glide.with(this)
            .load(business.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .into(binding.imgToolbarBusiness)
    }

    private fun multipleAccountsEntry(state: BusinessContract.State) {
        if (state.canShowMultipleAccountEntry) {
            loadBusiness(state.business)
        }
        binding.imgToolbarBusiness.isVisible = state.canShowMultipleAccountEntry
    }
}
