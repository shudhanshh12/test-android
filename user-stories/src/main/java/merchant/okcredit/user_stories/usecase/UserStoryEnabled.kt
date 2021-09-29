package merchant.okcredit.user_stories.usecase

import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.user_stories.utils.UserStoriesFeature
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class UserStoryEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {
    fun execute(): Observable<Boolean> {
        return ab.get().isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES)
    }
}
