package merchant.okcredit.user_stories.storypreview.di

import `in`.okcredit.shared.base.MviViewModel
import com.camera.models.models.Picture
import dagger.Module
import dagger.Provides
import merchant.okcredit.user_stories.storypreview.StoryPreviewActivity
import merchant.okcredit.user_stories.storypreview.StoryPreviewContract
import merchant.okcredit.user_stories.storypreview.StoryPreviewFragment
import merchant.okcredit.user_stories.storypreview.StoryPreviewViewModel
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Provider

@Module
abstract class StoryPreviewFragmentModule {
    companion object {

        @Provides
        fun initialState(): StoryPreviewContract.State = StoryPreviewContract.State()

        @Provides
        fun viewModel(
            fragment: StoryPreviewFragment,
            viewModelProvider: Provider<StoryPreviewViewModel>,
        ): MviViewModel<StoryPreviewContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        @ViewModelParam("images")
        fun images(activity: StoryPreviewActivity): ArrayList<Picture> {
            return activity.intent?.getSerializableExtra(StoryPreviewActivity.INTENT_KEY_ADDED_IMAGE) as ArrayList<Picture>
        }

        @Provides
        @ViewModelParam("captionMap")
        fun captionMap(activity: StoryPreviewActivity): HashMap<CapturedImage?, String>? {
            return activity.intent?.getSerializableExtra(StoryPreviewActivity.INTENT_KEY_CAPTION_MAP) as HashMap<CapturedImage?, String>?
        }

        @Provides
        @ViewModelParam("activeMerchantId")
        fun merchantId(activity: StoryPreviewActivity): String {
            return activity.intent?.getStringExtra(StoryPreviewActivity.INTENT_KEY_ACTIVE_MERCHANT_ID) ?: ""
        }
    }
}
