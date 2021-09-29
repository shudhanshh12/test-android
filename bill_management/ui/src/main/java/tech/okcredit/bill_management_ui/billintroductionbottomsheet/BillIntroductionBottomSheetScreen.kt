package tech.okcredit.bill_management_ui.billintroductionbottomsheet

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetContract.*
import tech.okcredit.bill_management_ui.databinding.BillintroductionbottomsheetScreenBinding
import tech.okcredit.sdk.analytics.BillTracker
import javax.inject.Inject

class BillIntroductionBottomSheetScreen : BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
    "BillIntroductionBottomSheetScreen"
) {
    internal var isFirstSelection: Boolean = true

    @Inject
    internal lateinit var billTracker: Lazy<BillTracker>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BillintroductionbottomsheetScreenBinding.inflate(inflater, container, false).root
    }

    private lateinit var adapter: IntroductionAdapter

    private val binding: BillintroductionbottomsheetScreenBinding by viewLifecycleScoped(
        BillintroductionbottomsheetScreenBinding::bind
    )

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        binding.okay.setOnClickListener {
            billTracker.get().trackPopupClicked("Bill Gallery", "Bill Management")
            findNavController().popBackStack()
        }
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}
            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        handleOutsideClick()
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            dismiss()
        }
    }

    private fun initViewPager() {
        adapter = IntroductionAdapter()
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.pageIndicator, binding.viewPager) { tab, position ->
        }.attach()
        binding.viewPager.setCurrentItem(0, true)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isFirstSelection) {
                    isFirstSelection = false
                } else {
                    billTracker.get().trackPopupScrolled(position)
                }
            }
        })
    }

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {}
}
