package merchant.okcredit.user_stories.homestory.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.databinding.ItemStoryBinding

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ItemUserStory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ItemStoryBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var userStories: UserStories

    @CallbackProp
    fun setUserStoryClickedListener(onUserStoryClicked: (() -> Unit)?) {
        binding.rootView.setOnClickListener {
            onUserStoryClicked?.invoke()
        }
    }

    @ModelProp
    fun setUserStories(userStories: UserStories) {
        this.userStories = userStories
        binding.circularStoryView.setData(userStories.totalStories, userStories.totalSeen)
        binding.tvStoryPoster.text = userStories.name
        Glide.with(this).load(userStories.leastUnseenImageUrl).circleCrop().thumbnail(0.2f).into(binding.circularStoryView)
    }
}
