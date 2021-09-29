package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.StaffLinkAddCustomersFragmentBinding
import `in`.okcredit.merchant.customer_ui.ui.staff_link.NavigationListener
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.*
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class StaffLinkAddCustomerFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "StaffLinkAddCustomer",
        R.layout.staff_link_add_customers_fragment
    ),
    StaffLinkAddCustomerView.SelectCustomerListener {

    private lateinit var controller: StaffLinkAddCustomerController

    private var navigationListener: NavigationListener? = null

    @Inject
    lateinit var collectionNavigator: CollectionNavigator

    @Inject
    lateinit var communicationRepository: Lazy<CommunicationRepository>

    private val binding: StaffLinkAddCustomersFragmentBinding by viewLifecycleScoped(
        StaffLinkAddCustomersFragmentBinding::bind
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonAddDetails.setOnClickListener { pushIntent(Intent.AddDetailsClicked) }
        binding.buttonShare.setOnClickListener { pushIntent(Intent.ShareClicked) }
        binding.etSearch.afterTextChangedDebounce {
            pushIntent(Intent.SearchCustomer(it))
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.imageFilter.isVisible = true
                pushIntent(Intent.SearchClicked)
            } else {
                binding.imageFilter.isVisible = false
            }
        }
        binding.imageFilter.setOnClickListener {
            if (binding.etSearch.hasFocus()) {
                pushIntent(Intent.DismissSearch)
                binding.etSearch.setText("")
                hideSoftKeyboard(binding.etSearch)
                binding.etSearch.clearFocus()
            }
        }
        binding.textCommonSelection.setOnClickListener {
            if (getCurrentState().selectedCustomerIds.size == getCurrentState().originalCustomerList.size) {
                pushIntent(Intent.DeselectAllCustomers)
            } else {
                pushIntent(Intent.SelectAllCustomers)
            }
        }
        binding.imageCommonSelection.setOnClickListener {
            if (getCurrentState().selectedCustomerIds.size == getCurrentState().originalCustomerList.size) {
                pushIntent(Intent.DeselectAllCustomers)
            } else {
                pushIntent(Intent.SelectAllCustomers)
            }
        }
        initCustomerList()
    }

    private fun initCustomerList() {
        controller = StaffLinkAddCustomerController()
        controller.setSelectCustomerListener(this)
        binding.epoxyCustomers.setController(controller)
    }

    override fun render(state: State) {
        binding.cardDueSummary.isVisible = state.showTopSummaryCard
        binding.layoutSelectAllHeader.isVisible = state.showSelectAllHeader
        binding.viewBottomDivider.isVisible = state.showBottomActions
        binding.layoutBottomActions.isVisible = state.showBottomActions
        binding.textEmptyResult.isVisible = state.showEmptySearchResult
        binding.textEmptyResult.text = getString(R.string.t_003_staff_collection_no_result, state.searchQuery)
        binding.textAmountDue.text = getString(R.string.rupee_placeholder, CurrencyUtil.formatV2(state.totalDue))
        if (state.showNoCustomerMessage) {
            binding.textEducation.visible()
            binding.textEducation.text = getString(R.string.t_003_staff_collection_help_no_customer)
        } else {
            binding.textEducation.gone()
        }
        binding.textCustomerWithCount.text =
            getString(R.string.t_003_staff_collection_section_header, state.originalCustomerList.size.toString())
        controller.setData(state.filteredCustomerList)
        if (state.selectedCustomerIds.size == state.originalCustomerList.size) {
            binding.imageCommonSelection.imageTintList = null
            binding.textCommonSelection.text = getString(R.string.t_003_staff_collection_deselect)
        } else {
            binding.imageCommonSelection.imageTintList = ColorStateList.valueOf(getColorCompat(R.color.grey400))
            binding.textCommonSelection.text = getString(R.string.t_003_staff_collection_select)
        }
        binding.ivNavigationAndBack.setOnClickListener {
            pushIntent(Intent.GoBack)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        controller.setSelectCustomerListener(null)
        super.onDestroyView()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.MoveToAddDetails -> navigationListener?.navigateToAddEditDetails(
                linkId = event.staffLinkSummary.linkId,
                selectedCustomers = event.staffLinkSummary.customerIds.toList(),
                link = event.staffLinkSummary.link,
                linkCreateTime = System.currentTimeMillis(),
            )
            is ViewEvent.ShareOnWhatsApp -> {
                shareLinkOnWhatsApp(event)
            }
            is ViewEvent.ShowError -> longToast(event.error)
            ViewEvent.ShowSetUpCollection -> {
                showSetUpCollection()
            }
        }
    }

    private fun showSetUpCollection() {
        val bottomSheet = SetUpOnlinePaymentBottomSheet.getInstance()
        bottomSheet.setListener(object : SetUpOnlinePaymentBottomSheet.SetupOnlinePaymentListener {
            override fun onSetUpCancelled() {
            }

            override fun onSetUpConfirmed() {
                collectionNavigator.showAddMerchantDestinationDialog(
                    fragmentManager = childFragmentManager,
                    source = StaffLinkEventsTracker.Screen.COLLECTIONS_LIST_FOR_STAFF
                )
            }
        })
        bottomSheet.show(childFragmentManager, SetUpOnlinePaymentBottomSheet::class.java.simpleName)
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
                            event.staffLinkSummary.link
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
                    exception.printStackTrace()
                    withContext(Dispatchers.Main) { longToast(exception.message ?: getString(R.string.err_default)) }
                }
            }
            intent?.let { startActivity(intent) }
            navigationListener?.navigateToAddEditDetails(
                linkId = event.staffLinkSummary.linkId,
                selectedCustomers = event.staffLinkSummary.customerIds.toList(),
                link = event.staffLinkSummary.link,
                linkCreateTime = event.staffLinkSummary.linkCreateTime,
            )
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun onCustomerSelected(customerId: String) {
        pushIntent(Intent.CustomerTapped(customerId))
    }
}
