package merchant.okcredit.user_stories.contract

import android.content.Context
import merchant.okcredit.user_stories.contract.model.UserStories

interface HomeStoryNavigation {
    fun goToAddStoryScreen(context: Context, activeMyStoryCount: Int, activeMerchantId: String)
    fun goToViewUserStoryScreen(context: Context, userStories: UserStories)
    fun goToMyStoryScreen(context: Context)
}
