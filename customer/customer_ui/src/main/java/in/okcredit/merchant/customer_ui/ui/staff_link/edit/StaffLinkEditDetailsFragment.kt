package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.FragmentStaffLinkEditDetailBinding
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.delete.DeleteItemBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.staff_link.NavigationListener
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsContract.*
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class StaffLinkEditDetailsFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "StaffLinkEditCustomer",
        R.layout.fragment_staff_link_edit_detail
    ),
    StaffLinkEditDetailsCustomerView.SelectCustomerListener {
    private lateinit var controller: StaffLinkEditDetailsController

    private var navigationListener: NavigationListener? = null

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var communicationRepository: Lazy<CommunicationRepository>

    private val binding: FragmentStaffLinkEditDetailBinding by viewLifecycleScoped(
        FragmentStaffLinkEditDetailBinding::bind
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener) {
            navigationListener = context
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                pushIntent(Intent.GoBack)
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            pushIntent(Intent.GoBack)
            requireActivity().finish()
        }
        binding.buttonAddDetails.setOnClickListener { pushIntent(Intent.AddToListClicked) }
        binding.buttonShare.setOnClickListener { pushIntent(Intent.ShareClicked) }
        binding.imageDeleteLink.setOnClickListener { pushIntent(Intent.DeleteLinkClicked) }
        initCustomerList()
    }

    private fun initCustomerList() {
        controller = StaffLinkEditDetailsController()
        controller.setSelectCustomerListener(this)
        binding.epoxyCustomers.setController(controller)
    }

    override fun onDestroyView() {
        controller.setSelectCustomerListener(null)
        super.onDestroyView()
    }

    override fun render(state: State) {
        controller.setData(state.customerList)
        binding.textAmountDue.text = getString(R.string.rupee_placeholder, CurrencyUtil.formatV2(state.totalDue))
        binding.textSelectedCustomers.text = getString(
            R.string.customer_collections_pending,
            state.customerCountWithBalanceDue.toString(),
            state.customerList.size.toString()
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.FinishScreen -> navigationListener?.navigateToSelectCustomer()
            is ViewEvent.GoToAddCustomer -> navigationListener?.navigateToSelectCustomer(
                linkId = event.linkId,
                link = event.link,
                selectedCustomers = event.customerIds,
                linkCreateTime = event.linkCreateTime,
            )
            is ViewEvent.ShareOnWhatsApp -> shareLinkOnWhatsApp(event)
            is ViewEvent.ShowConfirmDelete -> showDeleteConfirmation(event.customerId)
            is ViewEvent.ShowUpdateAddress -> showUpdateAddress(event.customerId, event.address)
            is ViewEvent.ShowUpdateMobile -> navigateToUpdateMobile(event.customerId, event.mobile)
            is ViewEvent.ShowError -> longToast(event.error)
        }
    }

    private fun showUpdateAddress(customerId: String, address: String?) {
        UpdateCustomerAddressBottomSheet.getInstance(customerId, address)
            .show(childFragmentManager, UpdateCustomerAddressBottomSheet::class.java.simpleName)
    }

    private fun showDeleteConfirmation(customerId: String?) {
        val fragment = if (customerId.isNullOrEmpty()) {
            DeleteItemBottomSheet.getInstance(
                title = getString(R.string.t_003_staff_collection_delete_list_heading),
                description = getString(R.string.t_003_staff_collection_delete_list_body),
                primaryCtaText = getString(R.string.t_003_staff_collection_delete_list_scta)
            ).apply {
                setListener(object : DeleteItemBottomSheet.DeleteConfirmListener {
                    override fun deleteCancelled() {
                        pushIntent(Intent.DeleteLinkCancelled)
                    }

                    override fun deleteConfirmed() {
                        pushIntent(Intent.DeleteLinkConfirm)
                    }
                })
            }
        } else {
            DeleteItemBottomSheet.getInstance(
                title = getString(R.string.t_003_staff_collection_remove_customer_heading),
                description = getString(R.string.t_003_staff_collection_remove_customer_body),
                iconRes = R.drawable.ic_remove_circle_24_dp,
                primaryCtaText = getString(R.string.t_003_staff_collection_remove_customer_pcta)
            ).apply {
                setListener(object : DeleteItemBottomSheet.DeleteConfirmListener {
                    override fun deleteCancelled() {
                        pushIntent(Intent.DeleteCustomerCancelled(customerId))
                    }

                    override fun deleteConfirmed() {
                        pushIntent(Intent.DeleteCustomerConfirmed(customerId))
                    }
                })
            }
        }
        fragment.show(childFragmentManager, DeleteItemBottomSheet::class.java.simpleName)
    }

    private fun navigateToUpdateMobile(customerId: String, mobile: String?) {
        val description = if (mobile.isNullOrEmpty()) {
            getString(R.string.t_003_staff_collection_add_number_body)
        } else {
            getString(R.string.please_add_customer_new_number_to_update)
        }
        AddNumberDialogScreen.newInstance(
            customerId = customerId,
            description = description,
            mobile = mobile,
            screen = StaffLinkEventsTracker.Screen.COLLECTIONS_LIST_FOR_STAFF,
        ).show(childFragmentManager, AddNumberDialogScreen::class.java.simpleName)
    }

    private fun shareLinkOnWhatsApp(event: ViewEvent.ShareOnWhatsApp) {
        lifecycleScope.launch {
            var intent: android.content.Intent? = null
            withContext(Dispatchers.IO) {
                try {
                    val imageBitmap = requireContext().assets.open("graphic_staff_link_instructions.png")
                        .use { BitmapFactory.decodeStream(it) }

                    val shareIntentBuilder = ShareIntentBuilder(
                        shareText = getString(
                            R.string.msg_share_staff_link,
                            event.linkSummary
                        ),
                        imageFrom = ImagePath.ImageUriFromBitMap(
                            bitmap = imageBitmap,
                            context = requireContext(),
                            folderName = "staff_link",
                            imageName = "instructions.png"
                        )
                    )
                    intent = communicationRepository.get().goToWhatsApp(shareIntentBuilder).await()
                } catch (noWhatsAppError: IntentHelper.NoWhatsAppError) {
                    withContext(Dispatchers.Main) { longToast(R.string.whatsapp_not_installed) }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) { longToast(exception.message ?: getString(R.string.err_default)) }
                }
            }
            intent?.let { startActivity(intent) }
        }
    }

    override fun onCustomerClicked(customerId: String) {
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
    }

    override fun onCustomerDeleteClicked(customerId: String) {
        pushIntent(Intent.DeleteCustomerClicked(customerId))
    }

    override fun onCustomerUpdateMobileClicked(customerId: String) {
        pushIntent(Intent.UpdateCustomerMobileClicked(customerId))
    }

    override fun onCustomerUpdateAddressClicked(customerId: String) {
        pushIntent(Intent.UpdateCustomerAddressClicked(customerId))
    }
}
