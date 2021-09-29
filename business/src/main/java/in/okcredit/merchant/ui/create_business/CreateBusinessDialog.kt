package `in`.okcredit.merchant.ui.create_business

import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.merchant.databinding.DialogCreateBusinessBinding
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CreateBusinessDialog :
    BaseBottomSheetWithViewEvents<CreateBusinessContract.State, CreateBusinessContract.ViewEvent, CreateBusinessContract.Intent>(
        "CreateBusinessDialog"
    ) {

    companion object {
        const val TAG = "CreateBusinessDialog"
    }

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var analytics: CreateBusinessAnalytics

    private lateinit var binding: DialogCreateBusinessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val dialog = dialog as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogCreateBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showSoftKeyboard(binding.etBusinessName)

        binding.etBusinessName.onChange { text ->
            val color = if (text.isBlank()) R.color.grey400 else R.color.primary
            binding.fbAddBusiness.backgroundTintList = getColorStateListCompat(color)
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.fbAddBusiness.clicks()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter { binding.etBusinessName.text.toString().isNotBlank() }
                .map {
                    analytics.trackNameEntered()
                    binding.fbAddBusiness.isEnabled = false
                    val businessName = binding.etBusinessName.text.toString()
                    CreateBusinessContract.Intent.CreateBusiness(businessName, WeakReference(requireActivity()))
                }
        )
    }

    override fun render(state: CreateBusinessContract.State) {
        binding.fbAddBusiness.isEnabled = state.loading.not()
        val iconColor = if (state.loading) getColorCompat(R.color.primary) else getColorCompat(R.color.white)
        binding.fbAddBusiness.setColorFilter(iconColor)
        binding.loader.isVisible = state.loading
        isCancelable = state.loading.not() && state.successful.not()
    }

    override fun handleViewEvent(event: CreateBusinessContract.ViewEvent) {
        when (event) {
            is CreateBusinessContract.ViewEvent.ShowError -> shortToast(event.msg)
            CreateBusinessContract.ViewEvent.CreateSuccessful -> {
                hideSoftKeyboard(binding.etBusinessName)
                binding.grpContent.invisible()
                binding.grpSuccess.visible()
                isCancelable = false
                analytics.trackNameEnteredSuccessful()
                pushIntent(CreateBusinessContract.Intent.AutoDismissAndGoToHome)
            }
            CreateBusinessContract.ViewEvent.DismissAndGoHome -> {
                legacyNavigator.get().goToHome(requireActivity())
                activity?.finishAffinity()
            }
        }
    }
}
