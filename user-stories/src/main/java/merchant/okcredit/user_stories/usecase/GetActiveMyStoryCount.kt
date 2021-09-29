package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import javax.inject.Inject

class GetActiveMyStoryCount @Inject constructor(
    private val userStoryLocalSource: Lazy<UserStoriesLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(currentTimestamp: Long): Single<Int> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            userStoryLocalSource.get().getActiveCountMyStory(currentTimestamp, businessId)
        }
    }
}
