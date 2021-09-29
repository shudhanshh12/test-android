package `in`.okcredit.collection_ui.ui.home.adoption

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.FragmentCollectionAdoptionBinding
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Contract.*
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.airbnb.epoxy.carousel
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class CollectionAdoptionV2Fragment :
    BaseFragment<State, ViewEvent, Intent>("CollectionAdoptionV2", R.layout.fragment_collection_adoption) {

    internal val binding: FragmentCollectionAdoptionBinding by viewLifecycleScoped(
        FragmentCollectionAdoptionBinding::bind
    )

    private val pageChangeLiveData = MutableLiveData(0)
    private var onItemTouched = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpEpoxyCarousal()
        binding.buttonSetup.setOnClickListener {
            pushIntent(Intent.SetupClicked)
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

    private fun createEpoxyModelList(): MutableList<EpoxyModel<ValuePropositionView>> {
        val list = mutableListOf<EpoxyModel<ValuePropositionView>>()
        for (index in 0 until 3) {
            list.add(
                ValuePropositionViewModel_()
                    .id("carousal_item_$index")
                    .adoptionItem(getAdoptionItem(index))
                    .onVisibilityStateChanged { staffLinkEducationItemModel, _, visibilityState ->
                        if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
                            pageChangeLiveData.value = staffLinkEducationItemModel.adoptionItem().position
                        }
                    }
            )
        }
        return list
    }

    private fun getAdoptionItem(index: Int): CollectionAdoptionItem {
        return when (index) {
            0 -> CollectionAdoptionItem(
                position = index,
                title = R.string.t_002_payments_onboarding_edu_1,
                illustration = R.drawable.graphics_payments_onboarding_edu_1,
            )
            1 -> CollectionAdoptionItem(
                position = index,
                title = R.string.t_002_payments_onboarding_edu_2,
                illustration = R.drawable.graphics_payments_onboarding_edu_2,
            )
            else -> CollectionAdoptionItem(
                position = index,
                title = R.string.t_002_payments_onboarding_edu_3,
                illustration = R.drawable.graphics_payments_onboarding_edu_3,
            )
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
            while (pageChangeLiveData.value != null && pageChangeLiveData.value!! < 2 && !onItemTouched) {
                delay(3_000)
                carousal.smoothScrollToPosition(pageChangeLiveData.value!! + 1)
            }
        }

        pageChangeLiveData.observe(
            viewLifecycleOwner,
            { position ->
                if (position == 2) {
                    startButtonShineEffect()
                }
                when (position) {
                    0 -> {
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(0.2f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(0.2f)
                    }
                    1 -> {
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(0.2f)
                    }
                    2 -> {
                        binding.indicatorOne.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorTwo.animateAlphaIfNotCurrentValue(1f)
                        binding.indicatorThree.animateAlphaIfNotCurrentValue(1f)
                    }
                }
            }
        )
    }

    private fun startButtonShineEffect() {
        if (binding.viewShine.isVisible) return

        binding.viewShine.isVisible = true
        binding.imageHandNudge.isVisible = true
        AnimationUtils.upDownMotion(view = binding.imageHandNudge, delta = dpToPixel(30f), startDelay = 0L)
        AnimationUtils.shineEffect(binding.buttonSetup, binding.viewShine)
    }

    private fun View.animateAlphaIfNotCurrentValue(value: Float) {
        if (this.alpha != value) {
            animate().alpha(value)
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToAddDestination -> showAddMerchantDestinationDialog()
        }
    }

    private fun showAddMerchantDestinationDialog() {
        val dialogFrag = AddMerchantDestinationDialog.newInstance(
            isUpdateCollection = false,
            paymentMethodType = null,
            source = "collection_adoption",
            referredByMerchantId = getCurrentState().referredByMerchantId
        )
        dialogFrag.show(childFragmentManager, AddMerchantDestinationDialog.TAG)
    }

    companion object {
        const val ARG_REFERRAL_MERCHANT_ID = "referral_merchant_id"
        const val ARG_MERCHANT_ID = "merchant_id"
    }
}
