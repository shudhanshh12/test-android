package `in`.okcredit.frontend.ui.add_supplier_transaction

import `in`.okcredit.analytics.*
import `in`.okcredit.analytics.PropertyKey.COMMON_LEDGER
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.AddSupplierTransactionFragmentBinding
import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTransactionContract.Companion.INPUT_MODE_MEDIA
import `in`.okcredit.frontend.ui.add_supplier_transaction.views.MediaView
import `in`.okcredit.frontend.ui.add_supplier_transaction.views.PermissionView
import `in`.okcredit.frontend.ui.add_supplier_transaction.views.mediaView
import `in`.okcredit.frontend.ui.add_supplier_transaction.views.permissionView
import `in`.okcredit.frontend.ui.supplier.SupplierContract.Companion.KEY_TRANSACTION_CREATE_TIME
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.calculator.CalculatorView
import `in`.okcredit.shared.calculator.calculatorView
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.addTo
import android.Manifest
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import tech.okcredit.contract.OnUpdatePinClickListener
import zendesk.belvedere.Belvedere
import zendesk.belvedere.Callback
import zendesk.belvedere.MediaResult
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddSupplierTransactionFragment :
    BaseScreen<AddSupplierTransactionContract.State>(
        contentLayoutId = R.layout.add_supplier_transaction_fragment,
        label = "AddSupplierTxnScreen"
    ),
    AddSupplierTransactionContract.Navigator,
    CalculatorView.CalcListener,
    DatePickerDialog.OnDateSetListener,
    DialogInterface.OnCancelListener,
    PermissionView.Listener,
    MediaView.Listener,
    BottomSheetLoaderScreen.Listener,
    OnUpdatePinClickListener {

    private var mMobile: String? = null

    // intents
    private val onDigitClicked: PublishSubject<Int> = PublishSubject.create()
    private val onOperatorClicked: PublishSubject<String> = PublishSubject.create()
    private val onEqualsClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onDotClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onLongBackPress: PublishSubject<Unit> = PublishSubject.create()
    private val onBackPressClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeInputMode: PublishSubject<Int> = PublishSubject.create()
    private val onChangeDate: PublishSubject<DateTime> = PublishSubject.create()
    private val onChangeImage: PublishSubject<File?> = PublishSubject.create()
    private val onDeleteImage: PublishSubject<Unit> = PublishSubject.create()
    private val hideDateDialogueVisibility: PublishSubject<Unit> = PublishSubject.create()
    private val showAlert: PublishSubject<String> = PublishSubject.create()
    private val onTransactionSubmit: PublishSubject<UserIntent> = PublishSubject.create()
    private val onCheckPinStatus: PublishSubject<UserIntent> = PublishSubject.create()

    private var alert: Snackbar? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var imageEditDialog: AlertDialog? = null
    private var activeInputMode: Int = -1
    private var disposable: Disposable? = null

    private val storageAndCameraPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    )

    companion object {
        const val STORAGE_AND_CAMERA_REQUEST_CODE = 5467
        const val ADD_SUPPLIER_TXN = 3100
        const val ADD_SUPPLIER_TXN_UPDATE_PIN = 3150
    }

    private var bottomSheetLoader: BottomSheetLoaderScreen? = null
    private var isBottomSheetShown: Boolean = false

    @Inject
    internal lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var appLock: Lazy<AppLock>

    private val binding: AddSupplierTransactionFragmentBinding by viewLifecycleScoped(
        AddSupplierTransactionFragmentBinding::bind
    )

    override fun loadIntent(): UserIntent {
        return AddSupplierTransactionContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            binding.amountBox.clicks()
                .throttleFirst(50, TimeUnit.MILLISECONDS)
                .map { hideSoftKeyboard() }
                .delay(200, TimeUnit.MILLISECONDS)
                .map {
                    AddSupplierTransactionContract.Intent.OnChangeInputMode(AddSupplierTransactionContract.INPUT_MODE_AMOUNT)
                },

            onTransactionSubmit,
            onCheckPinStatus,
            binding.profileImage.clicks()
                .throttleFirst(50, TimeUnit.MILLISECONDS)
                .map {
                    AddSupplierTransactionContract.Intent.GoToCustomerProfile
                },

            binding.profileName.clicks()
                .throttleFirst(50, TimeUnit.MILLISECONDS)
                .map {
                    AddSupplierTransactionContract.Intent.GoToCustomerProfile
                },

            binding.btnSubmit.clicks()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .map {
                    val receiptFile =
                        if (getCurrentState().imageLocal.isNullOrEmpty()) null else File(getCurrentState().imageLocal)

                    val isPayment = getCurrentState().txType == Transaction.PAYMENT

                    if (getCurrentState().isPassWordEnable && getCurrentState().isPasswordSet && getCurrentState().activeInputMode != AddSupplierTransactionContract.INPUT_MODE_PASSWORD &&
                        getCurrentState().txType == Transaction.PAYMENT
                    ) {
                        callAddTransaction()
                    } else {
                        initBottomLoader()
                        AddSupplierTransactionContract.Intent.SubmitPassword(
                            amount = getCurrentState().amount,
                            password = "",
                            image = receiptFile?.absolutePath,
                            billDate = getCurrentState().date,
                            payment = isPayment,
                            note = binding.bottomContainerText.text.toString(),
                            isPasswordVerifyRequired = false
                        )
                    }
                },

            onDigitClicked
                .map { AddSupplierTransactionContract.Intent.OnDigitClicked(it) },

            onOperatorClicked
                .map { AddSupplierTransactionContract.Intent.OnOperatorClicked(it) },

            onEqualsClicked
                .map { AddSupplierTransactionContract.Intent.OnEqualClicked },

            onDotClicked
                .map { AddSupplierTransactionContract.Intent.OnDotClicked },

            onLongBackPress
                .map { AddSupplierTransactionContract.Intent.OnLongPressBackSpace },

            onBackPressClicked
                .map { AddSupplierTransactionContract.Intent.OnBackSpaceClicked },

            onChangeInputMode
                .map { AddSupplierTransactionContract.Intent.OnChangeInputMode(it) },

            onChangeDate
                .map { AddSupplierTransactionContract.Intent.OnChangeDate(it) },

            onChangeImage
                .map { AddSupplierTransactionContract.Intent.OnChangeImage(it) },

            onDeleteImage
                .map { AddSupplierTransactionContract.Intent.OnDeleteImage },

            showAlert
                .map { AddSupplierTransactionContract.Intent.ShowAlert(it) }

        )
    }

    private fun callAddTransaction(): AddSupplierTransactionContract.Intent {
        return if (getCurrentState().isMerchantPrefSynced) {
            if (getCurrentState().isFourDigitPin) {
                AddSupplierTransactionContract.Intent.AddTransaction(binding.bottomContainerText.text.toString())
            } else {
                AddSupplierTransactionContract.Intent.CheckIsFourDigitPinSet
            }
        } else {
            AddSupplierTransactionContract.Intent.SyncMerchantPref
        }
    }

    private fun initBottomLoader() {
        bottomSheetLoader =
            childFragmentManager.findFragmentByTag(BottomSheetLoaderScreen.TAG) as? BottomSheetLoaderScreen
        if (bottomSheetLoader == null) {
            bottomSheetLoader = BottomSheetLoaderScreen()
            bottomSheetLoader?.setListener(this)
            bottomSheetLoader?.isCancelable = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cameraContainer.setOnClickListener {
            redirectToCamera()
        }
        binding.dateContainer.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.SELECT_BILL_DATE,
                EventProperties
                    .create()
                    .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
            )

            if (activity != null) {
                if (!requireActivity().isFinishing) {
                    datePickerDialog?.show()
                }
            }
        }

        binding.imageContainer.setOnClickListener {
            if (activity != null) {
                if (!requireActivity().isFinishing) {
                    imageEditDialog?.show()
                }
            }
        }

        binding.bottomContainerRightIcon.setOnClickListener {
            if (getCurrentState().activeInputMode == AddSupplierTransactionContract.INPUT_MODE_PERMISSION ||
                getCurrentState().activeInputMode == INPUT_MODE_MEDIA
            ) {
                onChangeInputMode.onNext(AddSupplierTransactionContract.INPUT_MODE_AMOUNT)
            } else {
                redirectToCamera()
            }
        }

        binding.bottomContainerLeftIcon.setOnClickListener {
            if (activeInputMode != AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
                showSoftKeyboard(binding.bottomContainerText)
                onChangeInputMode.onNext(AddSupplierTransactionContract.INPUT_MODE_NOTE)
            }
        }

        binding.bottomContainerText.setOnFocusChangeListener { _, hasFocus ->
            // activeInputMode != -1 prevents notes from getting focus on launch of the screen
            // Refer: https://github.com/okcredit/merchant-android/pull/2190
            if (hasFocus && activeInputMode != AddSupplierTransactionContract.INPUT_MODE_PASSWORD && activeInputMode != -1) {
                showSoftKeyboard(binding.bottomContainerText)
                onChangeInputMode.onNext(AddSupplierTransactionContract.INPUT_MODE_NOTE)
            }
        }

        binding.rootView.setTracker(performanceTracker)
    }

    @AddTrace(name = Traces.RENDER_ADD_SUPPLIER_Transaction)
    override fun render(state: AddSupplierTransactionContract.State) {

        bottomSheetLoader?.let {
            if (state.isSubmitLoading.not() && state.isSubmitSuccess) {
                it.success()
            } else if (state.isSubmitLoading) {
                it.load()
            }
            if (it.isAdded.not() && isBottomSheetShown.not()) {
                isBottomSheetShown = true
                it.show(childFragmentManager, BottomSheetLoaderScreen.TAG)
                hideSoftKeyboard()
            }
            if (it.isAdded && isBottomSheetShown.not()) {
                dismissBottomLoader()
            }
        }

        if (state.isIncorrectPassword) {
            return
        }
        mMobile = state.supplier?.mobile

        // Default
        binding.amountBox.visibility = View.VISIBLE
        binding.amountDivider.visibility = View.VISIBLE
        binding.textAmountCalculation.visibility = View.VISIBLE
        binding.dateContainer.visibility = View.VISIBLE
        binding.txContainer.root.visibility = View.GONE
        binding.bottomContainerText.error = null
        binding.bottomContainerLeftIcon.setImageResource(R.drawable.ic_note_add)

        binding.bottomContainerText.apply {
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
            maxLines = 3
            hint = getString(R.string.add_note_optional)
        }

        binding.textAmountCalculation.text = state.amountCalculation?.replace("*", "x")

        // Set Add TRANSACTION info
        if (state.amount == 0L) {
            binding.bottomTextContainer.visibility = View.GONE
            binding.textAmount.visibility = View.GONE
            binding.amountDivider.visibility = View.GONE
            binding.textAmount.visibility = View.GONE
            binding.dateContainer.visibility = View.GONE
            binding.addCreditText.visibility = View.VISIBLE
            binding.cameraContainer.gone()
        } else {
            val autoTransition = AutoTransition()
            autoTransition.duration = 150
            if (binding.textAmount.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(binding.amountContainer, autoTransition)
            }

            if (binding.bottomTextContainer.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(binding.bottomContainer)
            }

            binding.bottomTextContainer.visibility = View.VISIBLE
            binding.amountDivider.visibility = View.VISIBLE
            binding.dateContainer.visibility = View.VISIBLE

            binding.textAmount.apply {
                visibility = View.VISIBLE
                text = CurrencyUtil.formatV2(state.amount)
            }

            binding.addCreditText.visibility = View.GONE

            state.amountCalculation?.let {
                if (state.amountCalculation.contains("*") ||
                    state.amountCalculation.contains("+") ||
                    state.amountCalculation.contains("-") ||
                    state.amountCalculation.contains("/")
                ) {
                    binding.textAmount.visibility = View.VISIBLE
                } else {
                    binding.textAmount.visibility = View.GONE
                }
            }
            if (state.canShowMidCamera) {
                binding.cameraContainer.visible()
            } else {
                binding.cameraContainer.gone()
                binding.bottomContainerRightIcon.setImageResource(R.drawable.ic_camera_56)
            }
        }

        if (activeInputMode != state.activeInputMode || state.activeInputMode == AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
            binding.recyclerView.withModels {
                when (state.activeInputMode) {
                    AddSupplierTransactionContract.INPUT_MODE_AMOUNT -> {
                        cancelPendingModelBuild()
                        binding.bottomContainerText.clearFocus()
                        hideSoftKeyboard()
                        calculatorView {
                            id("calculatorView")
                            listener(this@AddSupplierTransactionFragment)
                        }

                        if (activeInputMode == AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
                            binding.bottomContainerText.setText(state.note)
                        }
                    }

                    AddSupplierTransactionContract.INPUT_MODE_PERMISSION -> {
                        binding.bottomContainerText.clearFocus()
                        hideSoftKeyboard()
                        permissionView {
                            id("permissionView")
                            listener(this@AddSupplierTransactionFragment)
                        }
                    }

                    INPUT_MODE_MEDIA -> {
                        binding.bottomContainerText.clearFocus()
                        hideSoftKeyboard()
                        mediaView {
                            id("mediaView")
                            listener(this@AddSupplierTransactionFragment)
                        }
                    }
                }
            }
            activeInputMode = state.activeInputMode
        }

        // Set Date
        binding.dateText.text = DateTimeUtils.formatDateOnly(state.date)

        // Set color based on tx type
        if ((state.txType == Transaction.PAYMENT || state.txType == Transaction.RETURN) && context != null) {
            binding.textAmountCalculation.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_payment))
            binding.rupeeSymbol.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_payment))
            binding.addCreditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tx_payment))
            binding.imageDivider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_payment))
            binding.txContainer.txImageDivider.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.tx_payment
                )
            )
            binding.cursor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_payment))
            binding.addCreditText.text = getString(R.string.give_payment)
        } else {
            binding.cursor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_credit))
            binding.addCreditText.text = getString(R.string.take_credit)
        }

        // Set Amount Error Visibility
        if (state.amountError) {
            binding.errorAmount.visibility = View.VISIBLE
            binding.errorAmount.text = getString(R.string.txn_invalid_amount)
            binding.amountDivider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tx_credit))

            TransitionManager.beginDelayedTransition(binding.amountContainer)
            AnimationUtils.shake(binding.amountContainer)
        } else {
            TransitionManager.beginDelayedTransition(binding.amountContainer)

            binding.errorAmount.visibility = View.GONE
            binding.amountDivider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey800))
        }

        if (state.activeInputMode != AddSupplierTransactionContract.INPUT_MODE_AMOUNT) {
            binding.cursor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (localeManager.get().getLanguage() == LocaleManager.LANGUAGE_HINGLISH) {
            Locale.setDefault(LocaleManager.englishLocale)
        }

        // Set Date Picker dialog
        datePickerDialog = DatePickerDialog(
            requireContext(),
            this@AddSupplierTransactionFragment,
            state.date.year,
            state.date.monthOfYear.minus(1),
            state.date.dayOfMonth
        )
        if (datePickerDialog?.datePicker != null) {
            datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
        }

        datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
        datePickerDialog?.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), datePickerDialog)
        datePickerDialog?.setOnCancelListener(this@AddSupplierTransactionFragment)

        // Set Image
        if (!state.imageLocal.isNullOrEmpty()) {
            context?.let {
                if (state.activeInputMode != AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
                    binding.imageContainer.visibility = View.VISIBLE
                    setTxImageDialogueInfo(state.imageLocal)

                    GlideApp
                        .with(it)
                        .load(Uri.fromFile(File(state.imageLocal)))
                        .centerCrop()
                        .into(binding.imageView)
                }

                if (state.activeInputMode == AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
                    binding.bottomContainerRightIcon.visibility = View.VISIBLE
                } else {
                    binding.bottomContainerRightIcon.visibility = View.GONE
                }
            }
        } else {
            imageEditDialog?.dismiss()
            binding.bottomContainerRightIcon.visibility = View.VISIBLE
            binding.imageContainer.visibility = View.GONE
        }

        // Set Customer info
        setToolbarInfo(state)

        if (state.isAlertVisible) {
            alert = view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
            alert?.show()
        } else {
            alert?.dismiss()
        }

        // show/hide alert
        if (state.error) {
            alert = view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun redirectToCamera() {
        if (isAllRequiredPermissionsGranted(storageAndCameraPermission).not() &&
            isPermissionGranted(storageAndCameraPermission[0])
        ) {
            askCameraAndGalleryPermission()
        } else if (isAllRequiredPermissionsGranted(storageAndCameraPermission)) {
            hideSoftKeyboard()
            Observable
                .timer(50, TimeUnit.MILLISECONDS)
                .subscribe {
                    onChangeInputMode.onNext(INPUT_MODE_MEDIA)
                }.addTo(autoDisposable)
        } else {
            hideSoftKeyboard()
            Observable
                .timer(50, TimeUnit.MILLISECONDS)
                .subscribe {
                    onChangeInputMode.onNext(AddSupplierTransactionContract.INPUT_MODE_PERMISSION)
                }.addTo(autoDisposable)
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            KeyboardVisibilityEvent.setEventListener(requireActivity()) { isOpen ->
                if (isOpen) {
                    if (activeInputMode != AddSupplierTransactionContract.INPUT_MODE_PASSWORD) {
                        binding.bottomContainerText.requestFocus()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }

        val animation = AlphaAnimation(0.5f, 0f)
        animation.duration = 500
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        binding.cursor.startAnimation(animation)
    }

    override fun onStop() {
        super.onStop()
        if (disposable != null) {
            if (!disposable!!.isDisposed) {
                disposable!!.dispose()
            }
        }
        hideSoftKeyboard()
    }

    override fun onBackPressed(): Boolean {
        var originScreen = AddSupplierTransactionContract.ORIGIN_CUSTOMER_SCREEN
        try {
            originScreen = getCurrentState().originScreen
        } catch (e: Exception) {
        }
        if (originScreen == AddSupplierTransactionContract.ORIGIN_DELETE_SCREEN) {
            activity?.finish()
        } else {
            findNavController(this).popBackStack()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_SUPPLIER_TXN || requestCode == ADD_SUPPLIER_TXN_UPDATE_PIN) {
            data?.let {
                if (data.getBooleanExtra(IS_AUTHENTICATED, false)) {
                    val receiptFile =
                        if (getCurrentState().imageLocal.isNullOrEmpty()) null else File(getCurrentState().imageLocal)

                    val isPayment = getCurrentState().txType == Transaction.PAYMENT

                    initBottomLoader()
                    Completable
                        .timer(100, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            onTransactionSubmit.onNext(
                                AddSupplierTransactionContract.Intent.SubmitPassword(
                                    amount = getCurrentState().amount,
                                    password = "",
                                    image = receiptFile?.absolutePath,
                                    billDate = getCurrentState().date,
                                    payment = isPayment,
                                    note = binding.bottomContainerText.text.toString(),
                                    isPasswordVerifyRequired = false
                                )
                            )
                        }.addTo(autoDisposable)
                }
            }
        }
        context?.let {
            Belvedere.from(it)
                .getFilesFromActivityOnResult(
                    requestCode, resultCode, data,
                    object : Callback<List<MediaResult>>() {
                        override fun success(result: List<MediaResult>) {
                            if (result.isNotEmpty()) {
                                GlideApp
                                    .with(it)
                                    .load(Uri.fromFile(result[0].file))
                                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                    .into(binding.profileImage)

                                result[0].file?.let { file ->
                                    onChangeImage.onNext(file)
                                    onChangeInputMode.onNext(AddSupplierTransactionContract.INPUT_MODE_AMOUNT)
                                }
                            }
                        }
                    }
                )
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun askCameraAndGalleryPermission() {
        askPermissions(storageAndCameraPermission, STORAGE_AND_CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_AND_CAMERA_REQUEST_CODE) {
            if (isAllRequiredPermissionsGranted(storageAndCameraPermission)) {
                tracker.get().trackRuntimePermission(PropertyValue.SUPPLIER, PropertyValue.STORAGE, true)
                hideSoftKeyboard()
                Observable
                    .timer(300, TimeUnit.MILLISECONDS)
                    .observeOn(ThreadUtils.newThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        onChangeInputMode.onNext(INPUT_MODE_MEDIA)
                    }.addTo(autoDisposable)
            } else {
                hideSoftKeyboard()

                Observable
                    .timer(300, TimeUnit.MILLISECONDS)
                    .observeOn(ThreadUtils.newThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        tracker.get().trackRuntimePermission(PropertyValue.SUPPLIER, PropertyValue.STORAGE, false)
                        showAlert.onNext(getString(R.string.txn_camera_gallery_permission_msg))
                    }.addTo(autoDisposable)
            }
        }
    }

    private fun setToolbarInfo(state: AddSupplierTransactionContract.State) {
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        state.supplier?.let {
            binding.profileName.text = state.supplier.name

            CurrencyUtil.renderV2(state.supplier.balance, binding.due, 0)

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.supplier.name.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(state.supplier.name)
                )

            if (it.profileImage.isNullOrBlank()) {
                binding.profileImage.setImageDrawable(defaultPic)
            } else {
                disposable = imageLoader.get().context(this)
                    .load(state.supplier.profileImage)
                    .placeHolder(defaultPic)
                    .scaleType(IImageLoader.CIRCLE_CROP)
                    .into(binding.profileImage)
                    .build()
            }
        }
    }

    private fun setTxImageDialogueInfo(receiptUrl: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_txn_image_dialog, null)
        builder.setView(dialogView)
        dialogView.setPadding(0, 0, 0, 0)

        imageEditDialog = builder.create()

        val dialogLoading = dialogView.findViewById<ProgressBar>(R.id.dialog_loading)
        val dialogReceipt = dialogView.findViewById<PhotoView>(R.id.dialog_receipt)
        val dialogCameraBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_camera)
        val dialogGalleryBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_gallery)
        val dialogDeleteBtn = dialogView.findViewById<LinearLayout>(R.id.dialog_delete)

        dialogLoading.visibility = View.VISIBLE
        dialogReceipt.visibility = View.VISIBLE
        GlideApp.with(requireContext())
            .load(Uri.fromFile(File(receiptUrl)))
            .error(R.drawable.ic_no_recipt)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    dialogLoading.visibility = View.GONE
                    dialogReceipt.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    dialogLoading.visibility = View.GONE
                    dialogReceipt.visibility = View.VISIBLE
                    return false
                }
            })
            .into(dialogReceipt)

        dialogDeleteBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.DELETE_RECEIPT,
                EventProperties
                    .create()
                    .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
            )
            onDeleteImage.onNext(Unit)
        }

        dialogCameraBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.ADD_RECEIPT,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "camera")
                    .with(PropertyKey.SOURCE, "dialog")
                    .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
            )
            onCameraClickedFromBottomSheet()
        }

        dialogGalleryBtn.setOnClickListener {
            Analytics.track(
                AnalyticsEvents.ADD_RECEIPT,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "gallery")
                    .with(PropertyKey.SOURCE, "dialog")
                    .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
            )
            onGalleryClickedFromBottomSheet()
        }
    }

    override fun onDigitClicked(d: Int) {
        onDigitClicked.onNext(d)
    }

    override fun onOperatorClicked(d: String) {
        onOperatorClicked.onNext(d)
    }

    override fun onEqualsClicked() {
        onEqualsClicked.onNext(Unit)
    }

    override fun onDotClicked() {
        onDotClicked.onNext(Unit)
    }

    override fun onBackspaceLongPress() {
        onLongBackPress.onNext(Unit)
    }

    override fun onBackspaceClicked() {
        onBackPressClicked.onNext(Unit)
    }

    // On Date Changed
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val newDate = DateTime(calendar.timeInMillis)

        Analytics.track(
            AnalyticsEvents.UPDATE_BILL_DATE,
            EventProperties
                .create()
                .with("default", getCurrentState().date.withTimeAtStartOfDay() == newDate.withTimeAtStartOfDay())
                .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
        )

        onChangeDate.onNext(newDate)
        hideDateDialogueVisibility.onNext(Unit)
    }

    // Dismissing bill_date dialog
    override fun onCancel(p0: DialogInterface?) {
        hideDateDialogueVisibility.onNext(Unit)
    }

    // Clicking camera allow on bottom sheet
    override fun onCameraClickedFromBottomSheet() {
        imageEditDialog?.dismiss()
        Belvedere.from(requireContext())
            .camera()
            .open(this)
    }

    // Clicking gallery allow on bottom sheet
    override fun onGalleryClickedFromBottomSheet() {
        imageEditDialog?.dismiss()
        Belvedere.from(requireContext())
            .document()
            .contentType("image/*")
            .allowMultiple(false)
            .open(this)
    }

    // Clicking permission allow on bottom sheet
    override fun onAllowPermissionClicked() {
        askCameraAndGalleryPermission()
    }

    override fun goToHomeClearStack() {
        activity?.runOnUiThread {
            activity?.let {
                legacyNavigator.get().goToHome(it)
                it.finishAffinity()
            }
        }
    }

    override fun handleFourDigitPin(isFourDigitPinSet: Boolean) {
        if (isFourDigitPinSet)
            goToAuthScreen()
        else
            appLock.get().showUpdatePin(requireActivity().supportFragmentManager, this, ADD_SUPPLIER_TXN_UPDATE_PIN, "AddSupplierTxnScreen")
    }

    override fun onMerchantPrefSynced() {
        if (getCurrentState().isFourDigitPin) {
            goToAuthScreen()
        } else {
            onCheckPinStatus.onNext(AddSupplierTransactionContract.Intent.CheckIsFourDigitPinSet)
        }
    }

    override fun showUpdatePinScreen() {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get()
                    .appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), "AddSupplierTxnScreen"),
                ADD_SUPPLIER_TXN
            )
        }
    }

    override fun goToAuthScreen() {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get()
                    .appLock(getString(R.string.enterpin_screen_deeplink), requireActivity(), "AddSupplierTxnScreen"),
                ADD_SUPPLIER_TXN
            )
        }
    }

    override fun showFailedMsg() {
        bottomSheetLoader?.let {
            it.failed()
            if (it.isAdded.not() && isBottomSheetShown.not()) {
                isBottomSheetShown = true
                it.show(childFragmentManager, BottomSheetLoaderScreen.TAG)
                hideSoftKeyboard()
            }
            if (it.isAdded && isBottomSheetShown.not()) {
                dismissBottomLoader()
            }
        }
    }

    @UiThread
    override fun gotoTxSuccessScreen() {
        Analytics.track(
            AnalyticsEvents.ADD_TXN_CONFIRM,
            EventProperties
                .create()
                .with(
                    PropertyKey.TYPE,
                    if (getCurrentState().txType == Transaction.CREDIT) "credit" else "payment"
                )
                .with("customer_id", getCurrentState().supplier?.id)
                .with("amount", getCurrentState().amount.toString())
                .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
                .with(COMMON_LEDGER, getCurrentState().supplier?.registered ?: false)
                .with(PropertyKey.SOURCE, getSource())
        )

        if (getCurrentState().originScreen == AddSupplierTransactionContract.ORIGIN_DELETE_SCREEN) {
            activity?.finish()
        } else {
            activity?.runOnUiThread {
                findNavController(this).popBackStack()
            }
        }
    }

    private fun getSource(): String {
        return if (getCurrentState().originScreen == AddSupplierTransactionContract.ORIGIN_DELETE_SCREEN) {
            Screen.DELETE_SUPPLIER
        } else {
            Screen.SUPPLIER_SCREEN
        }
    }

    @UiThread
    override fun gotoForgotPasswordScreen(mobile: String) {
        activity?.runOnUiThread {
            legacyNavigator.get().goToForgotPasswordScreen(requireActivity(), mobile)
        }
    }

    override fun gotoCustomerProfile(customerId: String?) {
        tracker.get().trackViewProfile(
            PropertyValue.ADD_TXN,
            PropertyValue.SUPPLIER,
            PropertyValue.CUSTOMER,
            customerId
        )
        activity?.runOnUiThread {
            legacyNavigator.get().gotoSupplierProfile(requireActivity(), customerId!!)
        }
    }

    override fun showNetworkError() {
        shortToast(getString(R.string.no_internet_msg))
    }

    override fun onRetry() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        binding.btnSubmit.performClick()
    }

    override fun onCancel() {
        bottomSheetLoader?.dismiss()
        isBottomSheetShown = false
        bottomSheetLoader = null
    }

    override fun onSuccess() {
        bottomSheetLoader?.dismiss()
        bottomSheetLoader = null
        gotoTxSuccessScreen()
    }

    private fun dismissBottomLoader() {
        bottomSheetLoader?.let {
            if (isStateInitialized() && it.isAdded) {
                it.dismiss()
            }
        }
        childFragmentManager.findFragmentByTag(BottomSheetLoaderScreen.TAG)?.let {
            removeFragment(it)
        }
        isBottomSheetShown = false
        bottomSheetLoader = null
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        startActivityForResult(
            appLock.get()
                .appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), "AddSupplierTxnScreen"),
            requestCode
        )
    }

    override fun onUpdateDialogDismissed() {}

    override fun onPause() {
        super.onPause()
        binding.bottomContainerText.clearFocus()
        hideSoftKeyboard()
    }

    override fun setNewTransactionIdAsNavResult(transactionCreateTime: Long) {
        activity?.runOnUiThread {
            findNavController(this).previousBackStackEntry?.savedStateHandle
                ?.set(KEY_TRANSACTION_CREATE_TIME, transactionCreateTime)
        }
    }
}
