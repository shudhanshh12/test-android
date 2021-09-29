package merchant.okcredit.user_stories.storypreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.camera.models.models.Picture
import merchant.okcredit.user_stories.databinding.ActivityStoryPreviewBinding
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.camera_contract.CapturedImage

class StoryPreviewActivity : OkcActivity() {

    private val binding: ActivityStoryPreviewBinding by viewLifecycleScoped(ActivityStoryPreviewBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    companion object {
        const val INTENT_KEY_ADDED_IMAGE = "added_images"
        const val INTENT_KEY_CAPTION_MAP = "caption_image_map"
        const val INTENT_KEY_ACTIVE_MERCHANT_ID = "key_active_merchant_id"

        fun previewIntent(
            context: Context,
            images: ArrayList<Picture>,
            captionMap: HashMap<CapturedImage?, String>?,
            activeMerchantId: String
        ): Intent {
            return Intent(context, StoryPreviewActivity::class.java).also {
                it.putExtra(INTENT_KEY_ADDED_IMAGE, images)
                it.putExtra(INTENT_KEY_CAPTION_MAP, captionMap)
                it.putExtra(INTENT_KEY_ACTIVE_MERCHANT_ID, activeMerchantId)
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager
            .findFragmentByTag("story_preview") as StoryPreviewFragment
        fragment.goBack()
    }
}
