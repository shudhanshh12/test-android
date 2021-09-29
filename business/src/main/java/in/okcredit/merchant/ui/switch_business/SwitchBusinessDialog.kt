package `in`.okcredit.merchant.ui.switch_business

import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.merchant.databinding.DialogSwitchBusinessBinding
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessContract.*
import `in`.okcredit.merchant.ui.switch_business.view.BusinessItemView
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference
import javax.inject.Inject

class SwitchBusinessDialog :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>("SwitchBusinessDialog"),
    BusinessItemView.Listener {

    private lateinit var binding: DialogSwitchBusinessBinding
    private lateinit var source: String

    private val controller = SwitchBusinessController(this)

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var businessNavigator: BusinessNavigator

    @Inject
    internal lateinit var analytics: SwitchBusinessAnalytics

    companion object {
        const val TAG = "SwitchBusinessDialog"
        const val SOURCE = "source"

        fun newInstance(source: String): SwitchBusinessDialog {
            val fragment = SwitchBusinessDialog()
            val bundle = Bundle()
            bundle.putString(SOURCE, source)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun loadIntent(): UserIntent? {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.createNewBusiness.clicks()
                .map {
                    Intent.CreateNewBusiness
                }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
        source = arguments?.getString(SOURCE) ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSwitchBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        analytics.trackPageViewed(source)
    }

    override fun render(state: State) {
        controller.setState(state, source)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ShowCreateBusinessDialog -> navigateToCreateBusinessDialog()
            is ViewEvent.ShowError -> {
                shortToast(event.msg)
                dismiss()
            }
        }
    }

    private fun navigateToCreateBusinessDialog() {
        analytics.trackCreateNewBusinessStarted(source)
        businessNavigator.showCreateBusinessDialog(parentFragmentManager)
        dismiss()
    }

    private fun initList() {
        binding.businessList.adapter = controller.adapter
        binding.businessList.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onSelect(businessId: String, businessName: String) {
        analytics.trackBusinessSelected(source, businessId)
        pushIntent(Intent.SetActiveBusiness(businessId, businessName, WeakReference(requireActivity())))
    }

    override fun onEdit(businessId: String) {
        analytics.trackViewProfile()
        legacyNavigator.goToMerchantProfile(requireContext())
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
    }
}
