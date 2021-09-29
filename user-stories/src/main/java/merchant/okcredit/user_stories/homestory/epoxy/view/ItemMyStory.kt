package merchant.okcredit.user_stories.homestory.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import merchant.okcredit.user_stories.contract.model.HomeStories
import merchant.okcredit.user_stories.databinding.ItemMyStoryBinding
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import timber.log.Timber

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ItemMyStory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding = ItemMyStoryBinding.inflate(LayoutInflater.from(context), this, true)

    @CallbackProp
    fun setMyStoryClickedListener(onMyStoryClicked: (() -> Unit)?) {
        binding.rootView.setOnClickListener {
            onMyStoryClicked?.invoke()
        }
    }

    @ModelProp
    fun setMyStory(homeStories: HomeStories) {
        Timber.i(" Hashcode -- ${homeStories.hashCode()}")
        binding.circularStoryView.setData(1, 0)
        Glide.with(this).load(homeStories.lastStoryUrl).circleCrop().thumbnail(0.2f).into(binding.circularStoryView)
        if (!homeStories.isAllSynced) {
            binding.ibProgress.visible()
            AnimationUtils.rotationAnimation(binding.ibProgress)
        } else {
            binding.ibProgress.gone()
        }
    }
}
