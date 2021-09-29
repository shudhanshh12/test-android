package merchant.okcredit.user_stories

import android.content.Context
import android.widget.Toast
import merchant.okcredit.user_stories.contract.HomeStoryNavigation
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.storycamera.UserStoryCameraActivity
import tech.okcredit.android.base.extensions.longToast
import javax.inject.Inject

class HomeStoryNavigationImpl @Inject constructor() : HomeStoryNavigation {
    override fun goToAddStoryScreen(context: Context, activeMyStoryCount: Int, activeMerchantId: String) {
        if (activeMyStoryCount <= UserStoryCameraActivity.MAX_IMAGE_ALLOWED) {
            UserStoryCameraActivity.openCamera(context, activeMyStoryCount, activeMerchantId)
        } else {
            context.longToast(R.string.max_image_error_msg)
        }
    }

    override fun goToViewUserStoryScreen(context: Context, userStories: UserStories) {
        // TODO:: once view stories are done need to add navigation code
        Toast.makeText(context, "View stories Clicked! TODO Impl ${userStories.name}", Toast.LENGTH_SHORT).show()
    }

    override fun goToMyStoryScreen(context: Context) {
        // TODO:: once my stories ready add navigation code
    }
}
