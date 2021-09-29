package merchant.okcredit.user_stories.homestory.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import merchant.okcredit.user_stories.contract.model.HomeStories
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.homestory.epoxy.view.itemAddStory
import merchant.okcredit.user_stories.homestory.epoxy.view.itemMyStory
import merchant.okcredit.user_stories.homestory.epoxy.view.itemNoStory
import merchant.okcredit.user_stories.homestory.epoxy.view.itemUserStory
import javax.inject.Inject

class HomeUserStoryController @Inject constructor() : TypedEpoxyController<HomeStories>() {

    private var onUserStoryClicked: ((userStories: UserStories, position: Int) -> Unit)? = null
    private var onAddStoryClicked: (() -> Unit)? = null
    private var onMyStoryClicked: (() -> Unit)? = null

    fun setUserStoryClickListener(onUserStoryClicked: ((userStory: UserStories, position: Int) -> Unit)) {
        this.onUserStoryClicked = onUserStoryClicked
    }

    fun setAddStoryClickListener(onAddStoryClicked: () -> Unit) {
        this.onAddStoryClicked = onAddStoryClicked
    }

    fun setMyStoryClickedListener(onMyStoryClicked: () -> Unit) {
        this.onMyStoryClicked = onMyStoryClicked
    }

    override fun buildModels(data: HomeStories?) {
        data?.let {
            when {
                it.userStories.isNotEmpty() -> {
                    renderUserStories(it)
                }
                it.isMyStoryAdded -> {
                    renderMyStoriesUi(it)
                }
                else -> {
                    renderAddStoryEmpty()
                }
            }
        }
    }

    private fun renderAddStoryEmpty() {
        itemNoStory {
            id("noStory")
            addStoryClickedListener { onAddStoryClicked?.invoke() }
        }
    }

    private fun renderMyStoriesUi(it: HomeStories) {
        if (it.isMyStoryAdded) {
            itemMyStory {
                id("myStory")
                myStory(it)
                myStoryClickedListener { onMyStoryClicked?.invoke() }
            }
        } else {
            itemAddStory {
                id("addStory")
                addStoryClickedListener { onAddStoryClicked?.invoke() }
            }
        }
    }

    private fun renderUserStories(it: HomeStories) {
        renderMyStoriesUi(it)
        it.userStories.forEachIndexed { index, it ->
            itemUserStory {
                id(it.id)
                userStories(it)
                userStoryClickedListener { onUserStoryClicked?.invoke(it, index) }
            }
        }
    }
}
