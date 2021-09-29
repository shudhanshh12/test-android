package `in`.okcredit.communication_inappnotification.ui.education_sheet

import `in`.okcredit.communication_inappnotification.R
import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.eventhandler.ClickEventHandler
import `in`.okcredit.communication_inappnotification.model.ActionButton
import `in`.okcredit.communication_inappnotification.model.EducationSheet
import `in`.okcredit.communication_inappnotification.model.EducationSheet.Companion.IMAGE_NO_TOP_MARGIN
import `in`.okcredit.communication_inappnotification.model.EducationSheet.Companion.IMAGE_WIDTH_MATCH_PARENT
import `in`.okcredit.communication_inappnotification.utils.InAppNotificationUtils.dpToPixel
import `in`.okcredit.communication_inappnotification.utils.ViewUtils.getButtonStartDrawableTarget
import `in`.okcredit.communication_inappnotification.utils.ViewUtils.getGlideRequestForUrl
import `in`.okcredit.communication_inappnotification.utils.ViewUtils.hide
import `in`.okcredit.communication_inappnotification.utils.ViewUtils.setTextAndShowOrHideTextView
import `in`.okcredit.communication_inappnotification.utils.ViewUtils.show
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.TOP
import com.google.android.material.button.MaterialButton
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.lang.ref.WeakReference
import javax.inject.Inject
import `in`.okcredit.communication_inappnotification.databinding.EducationStandardBottomSheetBinding as Binding

class StandardEducationBottomSheet(
    private val educationSheet: EducationSheet
) : BaseEducationBottomSheet(TAG) {

    companion object {
        const val TAG = "EducationSheetStandard"

        const val TEMPLATE_NAME = "standard"
    }

    @Inject
    internal lateinit var clickEventHandler: Lazy<ClickEventHandler>

    @Inject
    internal lateinit var tracker: Lazy<InAppNotificationTracker>

    internal val binding: Binding by viewLifecycleScoped(Binding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        Binding.inflate(layoutInflater).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)

        setImageViewUi(constraintSet)
        setTitleUi(constraintSet)
        setSubtitleUi(constraintSet)
        setPrimaryButtonUi(constraintSet)
        setSecondaryButtonUi(constraintSet)

        constraintSet.applyTo(binding.root)
    }

    private fun setImageViewUi(constraintSet: ConstraintSet) {
        if (educationSheet.imageUrl.isNotNullOrBlank()) {
            binding.image.show(constraintSet)
            setImageViewTopMargin(constraintSet)
            setImageViewSize(constraintSet)
            loadImageIntoView()
        } else {
            binding.image.hide(constraintSet)
        }
    }

    private fun setImageViewTopMargin(constraintSet: ConstraintSet) {
        if (educationSheet.imageWidth == IMAGE_WIDTH_MATCH_PARENT) {
            constraintSet.setMargin(R.id.image, TOP, IMAGE_NO_TOP_MARGIN)
        }
    }

    private fun setImageViewSize(constraintSet: ConstraintSet) {
        val width = if (educationSheet.imageWidth == IMAGE_WIDTH_MATCH_PARENT) {
            ConstraintSet.MATCH_CONSTRAINT
        } else {
            educationSheet.imageWidth.dpToPixel(requireContext())
        }
        constraintSet.constrainWidth(R.id.image, width)
        constraintSet.constrainHeight(R.id.image, educationSheet.imageHeight.dpToPixel(requireContext()))
    }

    private fun loadImageIntoView() {
        getGlideRequestForUrl(requireContext(), educationSheet.imageUrl).into(binding.image)
    }

    private fun setTitleUi(constraintSet: ConstraintSet) {
        setTextAndShowOrHideTextView(educationSheet.title, binding.tvTitle, constraintSet)
    }

    private fun setSubtitleUi(constraintSet: ConstraintSet) {
        setTextAndShowOrHideTextView(educationSheet.subtitle, binding.tvSubtitle, constraintSet)
    }

    private fun setPrimaryButtonUi(constraintSet: ConstraintSet) {
        if (educationSheet.primaryBtn != null && educationSheet.primaryBtn.text.isNotNullOrBlank()) {
            binding.btnPrimary.show(constraintSet)
            setOnClickListener(binding.btnPrimary, educationSheet.primaryBtn)
            binding.btnPrimary.text = educationSheet.primaryBtn.text
            loadImageAsStartDrawableIntoButton(educationSheet.primaryBtn.iconUrl, binding.btnPrimary)
        } else {
            binding.btnPrimary.hide(constraintSet)
        }
    }

    private fun setOnClickListener(materialButton: MaterialButton, actionButton: ActionButton) {
        materialButton.setOnClickListener {
            clickEventHandler.get().onClick(actionButton, WeakReference(this))
            trackNotificationClicked(actionButton)
        }
    }

    private fun trackNotificationClicked(actionButton: ActionButton) {
        tracker.get().trackNotificationClicked(
            type = educationSheet.getTypeForAnalyticsTracking(),
            id = educationSheet.id,
            name = educationSheet.name,
            source = educationSheet.source,
            value = actionButton.text
        )
    }

    private fun loadImageAsStartDrawableIntoButton(url: String?, button: MaterialButton) {
        if (url.isNotNullOrBlank()) {
            val btnPrimaryDrawableTarget = getButtonStartDrawableTarget(requireContext(), button)
            getGlideRequestForUrl(requireContext(), url).into(btnPrimaryDrawableTarget)
        }
    }

    private fun setSecondaryButtonUi(constraintSet: ConstraintSet) {
        if (educationSheet.secondaryBtn != null && educationSheet.secondaryBtn.text.isNotNullOrBlank()) {
            binding.btnSecondary.show(constraintSet)
            setOnClickListener(binding.btnSecondary, educationSheet.secondaryBtn)
            binding.btnSecondary.text = educationSheet.secondaryBtn.text
            loadImageAsStartDrawableIntoButton(educationSheet.secondaryBtn.iconUrl, binding.btnSecondary)
        } else {
            binding.btnSecondary.hide(constraintSet)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        trackNotificationCleared()
    }

    private fun trackNotificationCleared() {
        tracker.get().trackNotificationCleared(
            type = educationSheet.getTypeForAnalyticsTracking(),
            id = educationSheet.id,
            name = educationSheet.name,
            source = educationSheet.source
        )
    }
}
