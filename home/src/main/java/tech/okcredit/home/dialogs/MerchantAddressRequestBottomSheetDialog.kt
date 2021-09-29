package tech.okcredit.home.dialogs

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.BACKPRESSED
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.GpsUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.home.databinding.MerchantAddressRequestBottomSheetBinding
import javax.inject.Inject

class MerchantAddressRequestBottomSheetDialog : ExpandedBottomSheetDialogFragment() {

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var activeBusiness: Lazy<GetActiveBusiness>

    private var business: Business? = null

    companion object {
        const val TAG = "MerchantAddressRequestBottomSheetDialog"
        fun show(fragmentManager: FragmentManager) {
            MerchantAddressRequestBottomSheetDialog().show(fragmentManager, TAG)
        }
    }

    private val binding: MerchantAddressRequestBottomSheetBinding by viewLifecycleScoped(
        MerchantAddressRequestBottomSheetBinding::bind
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeBusiness.get().execute().subscribe({ business }, {})
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = MerchantAddressRequestBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mbProceed.setOnClickListener {
            tracker.get().trackInAppClickedV2(
                type = "inapp_merchant_address",
                screen = PropertyValue.HOME_PAGE,
                focalArea = PropertyValue.FALSE,
                value = "proceed"
            )
            if (Permission.isLocationPermissionAlreadyGranted(requireContext())) {
                goToAddressScreen()
                dialog?.dismiss()
            } else {
                requestLocationPermission()
            }
        }

        binding.mbAskLater.setOnClickListener {
            tracker.get().trackInAppClickedV2(
                type = "inapp_merchant_address",
                screen = PropertyValue.HOME_PAGE,
                focalArea = PropertyValue.FALSE,
                value = "ask_later"
            )
            dialog?.dismiss()
        }
    }

    private fun requestLocationPermission() {
        Permission.requestLocationPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    goToAddressScreen()
                    dialog?.dismiss()
                }

                override fun onPermissionDenied() {
                    dialog?.dismiss()
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        tracker.get()
            .trackInAppClearedV1(type = "inapp_merchant_address", method = BACKPRESSED)
    }

    internal fun goToAddressScreen() {
        val isGpsOn = try {
            activity?.let { GpsUtils(requireActivity()).isGPSOn() } ?: false
        } catch (_: Exception) {
            false
        }
        legacyNavigator.get().goToMerchantInputScreen(
            context = requireActivity(),
            inputType = BusinessConstants.ADDRESS,
            inputTitle = "Address",
            inputValue = business?.address,
            latitude = business?.addressLatitude ?: 0.0,
            longitude = business?.addressLongitude ?: 0.0,
            gps = isGpsOn,
            isSourceInAppNotification = true
        )
    }
}
