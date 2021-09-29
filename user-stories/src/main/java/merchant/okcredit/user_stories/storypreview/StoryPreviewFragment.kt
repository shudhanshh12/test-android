package merchant.okcredit.user_stories.storypreview

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.camera.camera_preview_images.CameraImagesPreview
import io.reactivex.Observable
import merchant.okcredit.user_stories.R
import merchant.okcredit.user_stories.analytics.UserStoriesTracker
import merchant.okcredit.user_stories.databinding.FragmentStoryPreviewBinding
import merchant.okcredit.user_stories.storycamera.UserStoryCameraActivity
import tech.okcredit.android.base.extensions.afterTextChange
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Inject

class StoryPreviewFragment :
    BaseFragment<StoryPreviewContract.State, StoryPreviewContract.ViewEvent, StoryPreviewContract.Intent>(
        "StoryPreviewFragment",
        R.layout.fragment_story_preview
    ),
    CameraImagesPreview.PreviewInteractor,
    CameraImagesPreview.PreviewItemInteractor {

    private val binding: FragmentStoryPreviewBinding by viewLifecycleScoped(FragmentStoryPreviewBinding::bind)
    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(Observable.ambArray())
    }

    @Inject
    lateinit var userStoryTracker: UserStoriesTracker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imagePreview.setListener(this)
        binding.imagePreview.setItemListener(this)
        binding.delete.setOnClickListener {
            userStoryTracker.trackEventStoryDeleteImages(getCurrentState().activeMerchantId)
            val deletedImage = binding.imagePreview.deleteImage()
            deletedImage?.let {
                pushIntent(StoryPreviewContract.Intent.DeleteImage(deletedImage to getCurrentState().imageList))
            }
        }

        binding.back.setOnClickListener {
            goBack()
        }
        binding.caption.afterTextChange {
            if (it.length < 2) {
                // track only when he starts typing
                userStoryTracker.trackEventStoryAddCaptionStarted(
                    getCurrentState().activeMerchantId, getCurrentState().imageList.size, selectedPosition
                )
            }
            pushIntent(StoryPreviewContract.Intent.AddCaption(it))
        }

        binding.send.setOnClickListener {
            userStoryTracker.trackEventStoryAddCaptionSuccess(
                getCurrentState().activeMerchantId,
                getCurrentState().imageList.size,
                selectedPosition
            )
            userStoryTracker.trackEventStoryUploadStory(
                getCurrentState().activeMerchantId,
                getCurrentState().imageList.size
            )
            pushIntent(
                StoryPreviewContract.Intent.SaveStory(
                    getCurrentState().imageList,
                    getCurrentState().imageCaptionMap
                )
            )
        }
    }

    override fun render(state: StoryPreviewContract.State) {
        showProgress(state)
        setImageList(state)
        showCaption(state)
    }

    private fun showProgress(state: StoryPreviewContract.State) {
        if (state.isLoading) {
            binding.progressBar.visible()
        } else {
            binding.progressBar.gone()
        }
    }

    private fun setImageList(state: StoryPreviewContract.State) {
        state.imageList.let {
            if (it.size > 0 && binding.imagePreview.getDataSet().size != it.size + 1) {
                binding.imagePreview.setImages(it)
            }
        }
    }

    private fun showCaption(state: StoryPreviewContract.State) {
        state.caption?.let {
            if (binding.caption.text.toString() != it) {
                binding.caption.setText(it)
            }
        } ?: run {
            binding.caption.setText("")
        }
    }

    override fun loadIntent(): UserIntent {
        return StoryPreviewContract.Intent.Load
    }

    override fun handleViewEvent(event: StoryPreviewContract.ViewEvent) {
        when (event) {
            is StoryPreviewContract.ViewEvent.GoToMyStoryScreen -> goToMyStory()
        }
    }

    private fun goToMyStory() {
        activity?.longToast("Done--> go to MyStoryScreen and pop last two activities ")
    }

    override fun onLastImageDeletion() {
        goBack()
    }

    override fun onFirstItemLeftScrolled() {
        goBack()
    }

    override fun onCameraClicked() {
        userStoryTracker.trackEventStoryAddMore(getCurrentState().activeMerchantId)
        goBack()
    }

    private var selectedPosition: Int = 0
    override fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int) {
        selectedPosition = capturedPosition
        pushIntent(StoryPreviewContract.Intent.CurrentSelectedImage(capturedPic))
    }

    fun goBack() {
        val intent = Intent()
        intent.putExtra(UserStoryCameraActivity.INTENT_KEY_CAPTION_MAP, getCurrentState().imageCaptionMap)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }
}
