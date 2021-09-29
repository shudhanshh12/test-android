package `in`.okcredit.sales_ui.ui.billing_name

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.analytics.SalesAnalytics
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.*
import `in`.okcredit.sales_ui.databinding.DialogAddBillngBinding
import `in`.okcredit.sales_ui.ui.billing_name.views.ContactView
import `in`.okcredit.sales_ui.ui.billing_name.views.ContactsController
import `in`.okcredit.sales_ui.utils.Constants.Companion.PERMISSION_CONTACT_REQUEST_CODE
import `in`.okcredit.shared.base.BaseBottomDialogScreen
import `in`.okcredit.shared.base.UserIntent
import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.android.base.utils.KeyboardUtil.showKeyboardImplicit
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.contacts.contract.model.Contact
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BillingNameBottomSheetDialog :
    BaseBottomDialogScreen<BillingNameContract.State>("BillingNameBottomSheetDialog"),
    BillingNameContract.Navigator,
    ContactView.Listener {

    companion object {
        const val TAG = "ContactBottomSheetDialog"
        private const val BUYER_NAME = "buyer_name"
        private const val BUYER_MOBILE = "buyer_mobile"
        fun newInstance(buyerName: String?, buyerMobile: String?): BillingNameBottomSheetDialog {
            val fragment = BillingNameBottomSheetDialog()
            val bundle = Bundle()
            if (buyerName.isNullOrEmpty().not()) {
                bundle.putString(BUYER_NAME, buyerName)
            }
            if (buyerMobile.isNullOrEmpty().not()) {
                bundle.putString(BUYER_MOBILE, buyerMobile)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private var contactsDisposable: Disposable? = null
    lateinit var binding: DialogAddBillngBinding

    @Inject
    internal lateinit var salesAnalytics: SalesAnalytics

    private val controller = ContactsController(this)

    private val getContactsSubject: PublishSubject<String> = PublishSubject.create()
    private val setNameSubject: PublishSubject<String> = PublishSubject.create()
    private val setMobileSubject: PublishSubject<String> = PublishSubject.create()
    private val showMobileSubject: PublishSubject<Unit> = PublishSubject.create()
    private val setDataSubject: PublishSubject<Pair<String, String>> = PublishSubject.create()

    private var isImportEventTriggered: Boolean = false
    private var windowHeight = 0

    private var isSearched = false
    private var isContactSelected = false

    private val layoutManager = LinearLayoutManager(context)

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    interface Listener {
        fun onSubmit(name: String, mobile: String)
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddBillngBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setDialogHeight(isOpen: Boolean) {
        val layoutParams = binding.root.layoutParams as FrameLayout.LayoutParams
        var height = (windowHeight * 0.8f)
        if (isOpen || binding.importContacts.isVisible) {
            height = (windowHeight * 0.5f)
        }
        layoutParams.height = height.toInt()
        binding.root.postDelayed(
            {
                val lp = binding.rvContacts.layoutParams as ConstraintLayout.LayoutParams
                lp.height = 0
                binding.rvContacts.layoutParams = lp
                binding.root.layoutParams = layoutParams
                binding.root.invalidate()
            },
            100
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialog()
        initView()
        initContactList()
        setListeners()
    }

    private fun initDialog() {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val dm = DisplayMetrics()
        dialog?.window?.windowManager?.defaultDisplay?.getMetrics(dm)
        windowHeight = dm.heightPixels
        dialog?.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from<View>(bottomSheet!!)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initView() {
        binding.root.post {
            if (KeyboardVisibilityEvent.isKeyboardVisible(requireActivity())) {
                setDialogHeight(true)
            }
            binding.nameEditText.requestFocus()
            arguments?.let { arg ->
                arg.getString(BUYER_NAME)?.let {
                    binding.nameEditText.setText(it)
                    binding.nameEditText.setSelection(it.length)
                }
                arg.getString(BUYER_MOBILE)?.let {
                    binding.mobileEditText.setText(it)
                }
            }
            showKeyboardImplicit(requireContext(), binding.nameEditText)
            salesAnalytics.trackEvents(Event.SELECT_NAME, relation = PropertyValue.CASH_SALE)
        }
        binding.phoneField.visibility = View.GONE
        binding.nameField.setVisibility(binding.clearName, View.GONE)
        binding.importView.visibility = View.VISIBLE
        binding.rvContacts.visibility = View.GONE
        binding.selectContact.visibility = View.GONE
    }

    private fun initContactList() {
        binding.rvContacts.setController(controller)
        binding.rvContacts.adapter = controller.adapter
        if (binding.rvContacts.layoutManager == null) {
            binding.rvContacts.layoutManager = layoutManager
        }
    }

    private fun setListeners() {
        KeyboardVisibilityEvent.registerEventListener(requireActivity()) { isOpen ->
            setDialogHeight(isOpen)
        }
        binding.nameEditText.doOnTextChanged { text, start, count, after ->
            isSearched = text.isNullOrEmpty().not()
            setNameSubject.onNext(text.toString().trim())
        }
        binding.mobileEditText.doOnTextChanged { text, start, count, after ->
            isSearched = text.isNullOrEmpty().not()
            binding.addMobile.gone()
            binding.mobileLayout.visible()
            setMobileSubject.onNext(text.toString().trim())
        }
        binding.nameEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                showMobileSubject.onNext(Unit)
            }
            return@setOnEditorActionListener true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CONTACT_REQUEST_CODE) {
            if (permissions[0] == Manifest.permission.READ_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                salesAnalytics.trackEvents(
                    Event.GRANT_PERMISSION, screen = PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
                        .add(PropertyKey.TYPE, PropertyValue.CONTACT)
                )
                isImportEventTriggered = true
                loadContacts()
            } else {
                salesAnalytics.trackEvents(
                    Event.DENY_PERMISSION, screen = PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
                        .add(PropertyKey.TYPE, PropertyValue.CONTACT)
                )
            }
            showKeyboardImplicit(requireContext(), binding.nameEditText)
        }
    }

    private fun loadContacts() {
        contactsDisposable = Observable.timer(300, TimeUnit.MILLISECONDS, ThreadUtils.newThread())
            .subscribeOn(ThreadUtils.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (isStateInitialized()) {
                    getContactsSubject.onNext(getCurrentState().name)
                }
            }
            .filter { !isResumed || !isStateInitialized() }
            .delay(1, TimeUnit.SECONDS)
            .doOnNext {
                if (isStateInitialized()) {
                    getContactsSubject.onNext(getCurrentState().name)
                }
            }
            .subscribe()
    }

    override fun loadIntent(): UserIntent {
        return BillingNameContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            getContactsSubject
                .map {
                    BillingNameContract.Intent.GetContactsIntent(it)
                },
            binding.submitContact.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    if (isValidInput()) {
                        salesAnalytics.trackEvents(Event.CONFIRM_NAME, relation = PropertyValue.CASH_SALE)
                        if (getCurrentState().mobile.isNotEmpty()) {
                            salesAnalytics.trackEvents(
                                Event.CONFIRM_MOBILE,
                                relation = PropertyValue.CASH_SALE,
                                propertiesMap = PropertiesMap.create()
                            )
                        }
                        BillingNameContract.Intent.SubmitIntent(getCurrentState().name, getCurrentState().mobile)
                    } else {
                        BillingNameContract.Intent.ValidationErrorIntent
                    }
                },
            binding.addMobile.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillingNameContract.Intent.ShowMobileFieldIntent
                },
            binding.importContacts.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillingNameContract.Intent.GetContactPermissionIntent
                },
            binding.importImg.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillingNameContract.Intent.GetContactPermissionIntent
                },
            binding.clearName.clicks()
                .map {
                    BillingNameContract.Intent.SetNameIntent("")
                },
            binding.clearPhone.clicks()
                .map {
                    BillingNameContract.Intent.SetMobileIntent("")
                },
            setNameSubject
                .map {
                    BillingNameContract.Intent.SetNameIntent(it)
                },
            setMobileSubject
                .map {
                    BillingNameContract.Intent.SetMobileIntent(it)
                },
            setDataSubject
                .map {
                    BillingNameContract.Intent.SetDataIntent(it.first, it.second)
                },
            showMobileSubject
                .map {
                    BillingNameContract.Intent.ShowMobileFieldIntent
                }
        )
    }

    private fun isValidInput(): Boolean {
        val name = getCurrentState().name
        val mobile = getCurrentState().mobile
        return name.isNotEmpty() || (mobile.isNotEmpty() && mobile.length == 10)
    }

    override fun render(state: BillingNameContract.State) {
        controller.setState(state)
        if (isImportEventTriggered) {
            salesAnalytics.trackEvents(
                Event.IMPORT_CONTACT, screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
            )
            isImportEventTriggered = false
        }
        binding.rvContacts.postDelayed(
            {
                layoutManager.scrollToPosition(0)
            },
            300
        )
        if (state.isPermissionGranted) {
            binding.importView.visibility = View.INVISIBLE
            binding.rvContacts.visibility = View.VISIBLE
            binding.selectContact.visibility = View.VISIBLE
        }
        if (state.name.isEmpty() || (state.mobile.isNotEmpty() && state.mobile.length != 10)) {
            if (binding.nameEditText.text.isNullOrEmpty().not()) {
                binding.nameEditText.setText(state.name)
            }
            binding.submitContact.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey400)
            binding.submitContact.isEnabled = false
            binding.nameField.setVisibility(binding.clearName, View.GONE)
        } else {
            binding.nameField.setVisibility(binding.clearName, View.VISIBLE)
            binding.submitContact.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
            binding.submitContact.isEnabled = true
        }
        if (state.mobile.isEmpty()) {
            if (binding.mobileEditText.text.isNullOrEmpty().not()) {
                binding.mobileEditText.setText(state.mobile)
            }
            binding.phoneField.setVisibility(binding.clearPhone, View.GONE)
        } else {
            binding.phoneField.setVisibility(binding.clearPhone, View.VISIBLE)
        }
        if (state.canShowMobileField) {
            binding.phoneField.visibility = View.VISIBLE
            binding.addMobile.visibility = View.GONE
            binding.phoneField.setVisibility(binding.clearPhone, View.GONE)
            val lp = binding.nameLayout.layoutParams as ConstraintLayout.LayoutParams
            lp.setMargins(0, 0, 0, DimensionUtil.dp2px(requireContext(), 8f).toInt())
            binding.nameLayout.layoutParams = lp
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        hideSoftKeyboard()
        super.onDismiss(dialog)
    }

    override fun showMobileField() {
        activity?.runOnUiThread {
            salesAnalytics.trackEvents(Event.SELECT_MOBILE, relation = PropertyValue.CASH_SALE)
            binding.mobileEditText.requestFocus()
            binding.mobileEditText.setSelection(getCurrentState().mobile.length)
        }
    }

    override fun onSubmit(name: String, mobile: String) {
        salesAnalytics.trackEvents(
            Event.CONFIRM_BILLING_NAME, relation = PropertyValue.CASH_SALE,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.CONTACT, isContactSelected)
                .add(PropertyKey.SEARCH, isSearched)
        )
        listener?.onSubmit(name, mobile)
        dismiss()
    }

    override fun getContactPermission() {
        salesAnalytics.trackEvents(
            Event.IMPORT_CONTACT_CLICKED, screen = PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
        )
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_CONTACT_REQUEST_CODE)
            salesAnalytics.trackEvents(
                Event.VIEW_CONTACT_PERMISSION, screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
            )
        } else {
            getContactsSubject.onNext(getCurrentState().name)
        }
    }

    override fun onClick(contact: Contact) {
        isContactSelected = true
        salesAnalytics.trackEvents(
            Event.SELECT_CONTACT, screen = PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.FLOW, PropertyValue.CASH_SALE)
                .add(PropertyKey.SEARCH, isSearched)
        )
        setDataSubject.onNext(Pair(contact.name, contact.mobile))
    }

    override fun onDestroy() {
        contactsDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
        super.onDestroy()
    }
}
