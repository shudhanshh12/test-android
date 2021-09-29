package `in`.okcredit.merchant.customer_ui.ui.staff_link.education

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.FragmentStaffLinkEducationBinding
import `in`.okcredit.merchant.customer_ui.ui.staff_link.NavigationListener
import `in`.okcredit.merchant.customer_ui.ui.staff_link.StaffLinkEventsTracker
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.airbnb.epoxy.carousel
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class StaffLinkEducationFragment : Fragment(R.layout.fragment_staff_link_education) {

    private var navigationListener: NavigationListener? = null

    private val pageChangeLiveData = MutableLiveData(0)
    private var onItemTouched = false

    @Inject
    lateinit var staffLinkEventsTracker: Lazy<StaffLinkEventsTracker>

    private val binding: FragmentStaffLinkEducationBinding by viewLifecycleScoped(
        FragmentStaffLinkEducationBinding::bind
    )

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is NavigationListener) {
            navigationListener = context
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                staffLinkEventsTracker.get().tracCollectionListGoBack(
                    screen = StaffLinkEventsTracker.Screen.LIST_NOT_CREATED
                )
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            staffLinkEventsTracker.get().tracCollectionListGoBack(
                screen = StaffLinkEventsTracker.Screen.LIST_NOT_CREATED
            )
            requireActivity().finish()
        }
        setUpEpoxyCarousal()
        binding.buttonCreate.setOnClickListener {
            staffLinkEventsTracker.get().trackCreateListClicked()
            navigationListener?.navigateToSelectCustomer()
        }
    }

    private fun setUpEpoxyCarousal() {
        binding.epoxyBenefits.withModels {
            carousel {
                id("carouselView")
                numViewsToShowOnScreen(1f)
                padding(Carousel.Padding(0, 0))
                initialPrefetchItemCount(1)
                hasFixedSize(false)
                onVisibilityStateChanged { _, view, visibilityState ->
                    if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                        startAutoScroll(view)
                    }
                }
                models(
                    createEpoxyModelList()
                )
            }
        }
        EpoxyVisibilityTracker().attach(binding.epoxyBenefits)
    }

    private fun createEpoxyModelList(): List<EpoxyModel<StaffLinkEducationItem>> {
        val list = mutableListOf<EpoxyModel<StaffLinkEducationItem>>()
        for (index in 0 until 4) {
            list.add(
                StaffLinkEducationItemModel_()
                    .id("carousal_item_$index")
                    .graphicImage(getGraphicImage(index))
                    .description(getString(getDescription(index)))
                    .indicatorPosition(index)
                    .onVisibilityStateChanged { staffLinkEducationItemModel, _, visibilityState ->
                        if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                            pageChangeLiveData.value = staffLinkEducationItemModel.indicatorPosition()
                        }
                    }
            )
        }
        return list
    }

    private fun getDescription(index: Int): Int {
        return when (index) {
            0 -> R.string.t_003_staff_collection_page_heading1
            1 -> R.string.t_003_staff_collection_page_heading2
            2 -> R.string.t_003_staff_collection_page_heading3
            else -> R.string.t_003_staff_collection_page_heading4
        }
    }

    private fun getGraphicImage(index: Int): Int {
        return when (index) {
            0 -> R.drawable.collection_link_01
            1 -> R.drawable.collection_link_02
            2 -> R.drawable.collection_link_03
            else -> R.drawable.collection_link_04
        }
    }

    private fun getIndividualScreen(index: Int): String {
        return when (index) {
            0 -> StaffLinkEventsTracker.Type.CREATE_CUSTOMER_LIST
            1 -> StaffLinkEventsTracker.Type.SHARE_LIST_WITH_STAFF
            2 -> StaffLinkEventsTracker.Type.STAFF_QR_SHARE_LINK
            else -> StaffLinkEventsTracker.Type.PAYMENT_ENTRY
        }
    }

    private fun startAutoScroll(carousal: Carousel) {
        carousal.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                onItemTouched = true
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        })
        lifecycleScope.launch {
            // We run the auto scroll only till the point no item is touched
            while (pageChangeLiveData.value != null && pageChangeLiveData.value!! < 3 && !onItemTouched) {
                delay(2_000)
                carousal.smoothScrollToPosition(pageChangeLiveData.value!! + 1)
            }
        }
        pageChangeLiveData.observe(viewLifecycleOwner) {
            staffLinkEventsTracker.get().trackCollectionListEducation(getIndividualScreen(it))
        }
    }
}
