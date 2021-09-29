package tech.okcredit.bill_management_ui.billdetail

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.exhaustive
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bill_management_ui.billdetail.BillDetailContract.*
import tech.okcredit.bill_management_ui.databinding.BillDetailFragmentBinding
import tech.okcredit.bill_management_ui.editBill.EditBillActivity
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.store.database.TxnType
import javax.inject.Inject

class BillDetailFragment : BaseFragment<State, ViewEvent, Intent>(
    "BillDetailScreen",
    R.layout.bill_detail_fragment
) {

    companion object {
        private const val STORAGE_PERMISSIONS = 1
    }

    private lateinit var adapter: BillDocAdapter
    private val binding: BillDetailFragmentBinding by viewLifecycleScoped(BillDetailFragmentBinding::bind)

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var billTracker: Lazy<BillTracker>

    @Inject
    lateinit var imageCache: ImageCache

    override fun loadIntent() = Intent.Load

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        binding.deleteDetails.setOnClickListener {
            billTracker.get().trackBillDeleteClicked(getCurrentState().localBill!!)
            getCurrentState().localBill?.createdByMe?.let {
                if (it) {
                    if (getCurrentState().localBill?.transactionId.isNotNullOrBlank()) {
                        if (getCurrentState().role == BILL_INTENT_EXTRAS.CUSTOMER)
                            legacyNavigator.get()
                                .goToTransactionDetailFragment(requireContext(), getCurrentState().localBill!!.transactionId!!)
                        else {
                            legacyNavigator.get().goToSupplierTransactionScreen(
                                requireContext(),
                                getCurrentState().localBill!!.transactionId!!
                            )
                        }
                    } else {
                        getCurrentState().billId?.let { pushIntent(Intent.Delete(it)) }
                    }
                }
            }
        }
        binding.btnDownload.setOnClickListener {
            billTracker.get().trackDownloadBillClicked(getCurrentState().localBill!!)
            askStoragePermission()
        }
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rootView.setTracker(performanceTracker)
    }

    private fun initViewPager() {
        adapter = BillDocAdapter(imageCache)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.pageIndicator, binding.viewPager) { _, _ -> }.attach()
        binding.viewPager.setCurrentItem(0, true)

        binding.notesDetails.setOnClickListener {
            getCurrentState().localBill?.createdByMe?.let {
                if (it) {
                    if (getCurrentState().localBill?.transactionId.isNotNullOrBlank()) {
                        if (getCurrentState().role == BILL_INTENT_EXTRAS.CUSTOMER)
                            legacyNavigator.get()
                                .goToTransactionDetailFragment(requireContext(), getCurrentState().localBill!!.transactionId!!)
                        else {
                            legacyNavigator.get().goToSupplierTransactionScreen(
                                requireContext(),
                                getCurrentState().localBill!!.transactionId!!
                            )
                        }
                    } else {
                        val action =
                            BillDetailFragmentDirections.actionBilldetailsScreenToShowEditPhoneNumberDialog(
                                getCurrentState().localBill?.note,
                                getCurrentState().billId!!
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
        binding.transactionDetails.setOnClickListener {
            if (getCurrentState().localBill?.transactionId.isNotNullOrBlank()) {
                if (getCurrentState().role == BILL_INTENT_EXTRAS.CUSTOMER)
                    legacyNavigator.get()
                        .goToTransactionDetailFragment(requireContext(), getCurrentState().localBill!!.transactionId!!)
                else {
                    legacyNavigator.get().goToSupplierTransactionScreen(
                        requireContext(),
                        getCurrentState().localBill!!.transactionId!!
                    )
                }
            }
        }
    }

    override fun render(state: State) {
        state.localBill?.let { localBill ->
            if (localBill.localBillDocList.size == 1) {
                binding.pageIndicator.gone()
            } else {
                binding.pageIndicator.visible()
            }
            adapter.setItems(localBill.localBillDocList)
            if (localBill.createdByMe) {
                binding.otherContainer.gone()
                binding.deleteDetails.visible()
                // TODO hiding edit bill, till edit bill issue is resovled
                // binding.editBill.visible()
                binding.editBill.gone()
            } else {
                binding.deleteDetails.gone()
                binding.editBill.gone()
                binding.otherContainer.visible()
                binding.otherPartyName.visible()
                binding.otherPartyName.text = getString(R.string.added_by, state.accName)
            }
            if (localBill.amount != null && state.role != null && localBill.transactionId.isNotNullOrBlank()) {
                binding.amountLayout.visible()
                binding.amountType.visible()

                binding.arrow.visible()
                binding.txnAmount.text = TempCurrencyUtil.formatV2(localBill.amount!!.toLong())
                if (localBill.txnType == TxnType.CREDIT && state.role == BILL_INTENT_EXTRAS.CUSTOMER) {
                    val drawable =
                        binding.arrow.context.getDrawable(R.drawable.ic_take)
                    binding.arrow.setImageDrawable(drawable)
                    binding.arrow.rotation = 180f
                    binding.txnAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_primary))
                    binding.amountType.text = getString(R.string.amount_credit)
                } else if (localBill.txnType == TxnType.PAYMENT && state.role == BILL_INTENT_EXTRAS.CUSTOMER) {
                    val drawable =
                        binding.arrow.context.getDrawable(R.drawable.ic_give)
                    binding.arrow.rotation = 180f
                    binding.arrow.setImageDrawable(drawable)
                    binding.txnAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
                    binding.amountType.text = getString(R.string.amount_payment)
                } else if (localBill.txnType == TxnType.PAYMENT && state.role == BILL_INTENT_EXTRAS.SUPPLIER) {
                    val drawable =
                        binding.arrow.context.getDrawable(R.drawable.ic_give)
                    binding.arrow.setImageDrawable(drawable)
                    binding.txnAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
                    binding.amountType.text = getString(R.string.amount_payment)
                } else if (localBill.txnType == TxnType.CREDIT && state.role == BILL_INTENT_EXTRAS.CUSTOMER) {
                    val drawable =
                        binding.arrow.context.getDrawable(R.drawable.ic_take)
                    binding.arrow.setImageDrawable(drawable)
                    binding.txnAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_primary))
                    binding.amountType.text = getString(R.string.amount_credit)
                }
            } else {
                binding.amountLayout.gone()
            }
            if (localBill.note.isNotNullOrBlank()) {
                binding.notes.text = localBill.note
                binding.notes.setTextColor(resources.getColor(R.color.grey900))
            } else {
                binding.notes.text = getString(R.string.add_note)
                binding.notes.setTextColor(resources.getColor(R.color.grey500))
            }

            localBill.billDate?.let {
                binding.billsDate.visible()
                binding.billsDate.text =
                    getString(R.string.billed_on_date, DateTimeUtils.formatLong(DateTime(it.toLong())))
            }
            binding.addedDate.text =
                getString(R.string.added_on_date, DateTimeUtils.formatLong(DateTime(localBill.createdAt.toLong())))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSIONS -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                } else {
                    getCurrentState().billId?.let {
                        pushIntent(Intent.DownloadBill(it))
                    }
                }
            }
        }
    }

    private fun askStoragePermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSIONS
            )
        } else {
            getCurrentState().localBill?.let {
                pushIntent(Intent.DownloadBill(it.id))
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.GoToBillScreen -> {
                billTracker.get().trackBillDownloaded(getCurrentState().localBill!!)
                goBack()
            }
        }.exhaustive
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    fun openEditBillScreen(position: Int) {
        getCurrentState().localBill?.createdByMe?.let {
            if (it) {
                if (getCurrentState().localBill?.transactionId.isNotNullOrBlank()) {
                    if (getCurrentState().role == BILL_INTENT_EXTRAS.CUSTOMER)
                        legacyNavigator.get()
                            .goToTransactionDetailFragment(requireContext(), getCurrentState().localBill!!.transactionId!!)
                    else {
                        legacyNavigator.get().goToSupplierTransactionScreen(
                            requireContext(),
                            getCurrentState().localBill!!.transactionId!!
                        )
                    }
                } else {
                    val localBill = getCurrentState().localBill
                    billTracker.get().trackAddReceiptStarted(
                        "Edit Bill",
                        localBill?.id,
                        localBill?.transactionId,
                        localBill?.localBillDocList?.size
                    )
                    requireActivity().startActivity(
                        EditBillActivity.createIntent(
                            requireContext(),
                            position,
                            getCurrentState().billId!!
                        )
                    )
                }
            }
        }
    }
}
