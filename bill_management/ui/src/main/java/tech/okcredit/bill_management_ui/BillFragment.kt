package tech.okcredit.bill_management_ui

import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.dialogs.DateRangePickerDialog
import `in`.okcredit.shared.utils.exhaustive
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.bill_management_ui.bill_camera.BillCameraActivity
import tech.okcredit.bill_management_ui.databinding.BillFragmentBinding
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.models.SelectedDate
import tech.okcredit.sdk.models.SelectedDateMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BillFragment :
    BaseFragment<BillContract.State, BillContract.ViewEvent, BillContract.Intent>(
        "BillScreen",
        R.layout.bill_fragment
    ),
    FilledBillsView.Listener {

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
    }

    private lateinit var adapter: EmptyBillAdapter
    lateinit var binding: BillFragmentBinding
    private val pageViewedSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private var currentMonth: String? = null
    private var lastToLastMonth: String? = null
    private var lastMonth: String? = null

    private var filledBillsController: FilledBillsController? = null

    @Inject
    internal lateinit var billTracker: Lazy<BillTracker>

    private val onDateChange: PublishSubject<SelectedDate> = PublishSubject.create()

    @Inject
    internal lateinit var imageLoader: IImageLoader

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var imageCache: Lazy<ImageCache>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BillFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun loadIntent(): UserIntent {
        return BillContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            pageViewedSubject.map {
                BillContract.Intent.PageViewed(it)
            },
            onDateChange.throttleFirst(200, TimeUnit.MILLISECONDS).map {
                BillContract.Intent.OnDateChange(it)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        binding.addBillCard.setOnClickListener {
            billTracker.get().trackAddReceiptStarted("Add Bill")
            askCameraAndGalleryPermission()
        }

        filledBillsController = FilledBillsController(this, imageCache.get())
        binding.recyclerView.adapter = filledBillsController?.adapter

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.dateRange.setOnClickListener {

            val state = getCurrentState()
            billTracker.get().trackDateRangeClick()

            DateRangePickerDialog.show(
                requireContext(),
                state.startDate,
                state.endDate,
                object : DateRangePickerDialog.Listener {
                    override fun onDone(startDate: DateTime, endDate: DateTime) {
                        onDateChange.onNext(
                            SelectedDate(
                                startDate,
                                endDate,
                                SelectedDateMode.CUSTOM_DATE
                            )
                        )
                        billTracker.get().trackFilterUpdate(
                            startDate,
                            endDate,
                            SelectedDateMode.CUSTOM_DATE
                        )
                    }
                }
            ).show()
        }
        binding.all.setOnClickListener {
            onDateChange.onNext(SelectedDate(selectedMode = SelectedDateMode.OVERALL))
            billTracker.get().trackFilterUpdate(
                null,
                DateTime.now().minusMillis(1),
                SelectedDateMode.OVERALL
            )
        }
        binding.current.setOnClickListener {
            val state = getCurrentState()
            val latestDateTime = state.current
            latestDateTime?.let {
                onDateChange.onNext(
                    SelectedDate(
                        it.withDayOfMonth(1).withTimeAtStartOfDay(),
                        it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                        SelectedDateMode.CURRENT
                    )
                )
                billTracker.get().trackFilterUpdate(
                    it.withDayOfMonth(1).withTimeAtStartOfDay(),
                    it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                    SelectedDateMode.CURRENT
                )
            }
        }
        binding.lastMonth.setOnClickListener {
            val state = getCurrentState()
            val latestDateTime = state.last
            latestDateTime?.let {
                onDateChange.onNext(
                    SelectedDate(
                        it.withDayOfMonth(1).withTimeAtStartOfDay(),
                        it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                        SelectedDateMode.LAST_MONTH
                    )
                )
                billTracker.get().trackFilterUpdate(
                    it.withDayOfMonth(1).withTimeAtStartOfDay(),
                    it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                    SelectedDateMode.LAST_MONTH
                )
            }
        }
        binding.lastToLastMonth.setOnClickListener {
            val state = getCurrentState()
            val latestDateTime = state.lastToLast
            latestDateTime?.let {
                onDateChange.onNext(
                    SelectedDate(
                        it.withDayOfMonth(1).withTimeAtStartOfDay(),
                        it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                        SelectedDateMode.LAST_TO_LAST
                    )
                )
                billTracker.get().trackFilterUpdate(
                    it.withDayOfMonth(1).withTimeAtStartOfDay(),
                    it.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().minusMillis(1),
                    SelectedDateMode.LAST_TO_LAST
                )
            }
        }

        binding.rootView.setTracker(performanceTracker)
    }

    private fun initViewPager() {
        adapter = EmptyBillAdapter()
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.pageIndicator, binding.viewPager) { tab, position ->
        }.attach()
        binding.viewPager.setCurrentItem(0, true)
    }

    override fun render(state: BillContract.State) {

        state.accountName?.let {
            binding.accounteeName.text = it
        }
        when (state.selectedMode) {
            SelectedDateMode.CURRENT -> {
                binding.current.isSelected = true
                binding.all.isSelected = false
                binding.lastMonth.isSelected = false
                binding.lastToLastMonth.isSelected = false
                binding.dateRange.isSelected = false
            }
            SelectedDateMode.OVERALL -> {
                binding.current.isSelected = false
                binding.all.isSelected = true
                binding.lastMonth.isSelected = false
                binding.lastToLastMonth.isSelected = false
                binding.dateRange.isSelected = false
            }
            SelectedDateMode.LAST_MONTH -> {
                binding.current.isSelected = false
                binding.all.isSelected = false
                binding.lastMonth.isSelected = true
                binding.lastToLastMonth.isSelected = false
                binding.dateRange.isSelected = false
            }
            SelectedDateMode.LAST_TO_LAST -> {
                binding.current.isSelected = false
                binding.all.isSelected = false
                binding.lastMonth.isSelected = false
                binding.lastToLastMonth.isSelected = true
                binding.dateRange.isSelected = false
            }
            SelectedDateMode.CUSTOM_DATE -> {
                binding.current.isSelected = false
                binding.all.isSelected = false
                binding.lastMonth.isSelected = false
                binding.lastToLastMonth.isSelected = false
                binding.dateRange.isSelected = true
            }
        }
        if (state.areBillsPresent.not()) {
            initViewPager()
            binding.filledGroup.gone()
            binding.emptyGroup.visible()
        } else {

            if (state.selectedMode == SelectedDateMode.CUSTOM_DATE) {
                state.map?.let {
                    if (it.isEmpty()) {
                        binding.emptyRangeContianer.visible()
                    } else {
                        binding.emptyRangeContianer.gone()
                    }
                }
            } else {
                binding.emptyRangeContianer.gone()
            }
            binding.filledGroup.visible()
            binding.emptyGroup.gone()
            filledBillsController?.setState(state)

            state.monthsList?.let {
                when {
                    it.size >= 3 -> {

                        binding.current.text = it[0]
                        binding.lastMonth.text = it[1]
                        binding.lastToLastMonth.text = it[2]
                        binding.current.visible()
                        binding.lastMonth.visible()
                        binding.lastToLastMonth.visible()
                        currentMonth = it[0]
                        lastMonth = it[1]
                        lastToLastMonth = it[2]
                    }
                    it.size >= 2 -> {
                        binding.current.text = it[0]
                        binding.lastMonth.text = it[1]
                        binding.current.visible()
                        binding.lastMonth.visible()
                        binding.lastToLastMonth.gone()
                        currentMonth = it[0]
                        lastMonth = it[1]
                    }
                    it.isNotEmpty() -> {
                        binding.current.text = it[0]
                        binding.current.visible()
                        binding.lastMonth.gone()
                        binding.lastToLastMonth.gone()
                        currentMonth = it[0]
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.recyclerView.scrollToPosition(0)
    }

    private fun askCameraAndGalleryPermission() {
        Permission.requestStorageAndCameraPermission(
            requireActivity(),
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                }

                override fun onPermissionGranted() {
                    startCamera()
                }

                override fun onPermissionDenied() {
                    longToast(R.string.camera_permission_denied)
                }
            }
        )
    }

    internal fun startCamera() {
        val existingImageListSize = 0
        startActivityForResult(
            BillCameraActivity.createIntent(
                requireActivity(),
                "Add Transaction",
                "Customer",
                "aonr",
                "Add Screen",
                getCurrentState().accountId!!,
                "123",
                existingImageListSize,
                "bill flow",
                0

            ),
            CAMERA_REQUEST_CODE
        )
    }

    override fun handleViewEvent(event: BillContract.ViewEvent) {
        when (event) {
            BillContract.ViewEvent.ShowBottomSheetTutorial -> {
                billTracker.get().trackPopUpDisplayed("Bill Gallery", "Bill Management")
                findNavController().navigate(R.id.goToBillIntroductionBottomSheet)
            }
        }.exhaustive
    }

    override fun clickedFilledBillsView(billId: String) {
        findNavController().navigate(R.id.action_bill_to_detail, bundleOf(BILL_INTENT_EXTRAS.BILL_ID to billId))
    }
}
