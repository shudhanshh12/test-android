package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.usecase.UserStoryEnabled
import javax.inject.Inject

class UserStoryScheduleSyncIfEnabled @Inject constructor(
    private val userStoryEnabled: Lazy<UserStoryEnabled>,
    private val userStoryRepository: Lazy<UserStoryRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Boolean> {
        return userStoryEnabled.get().execute()
            .flatMap { isEnabled ->
                val maybeScheduleSync =
                    if (isEnabled) {
                        getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                            userStoryRepository.get().scheduleSyncStories(businessId)
                        }
                    } else Completable.complete()
                maybeScheduleSync
                    .andThen(Observable.just(isEnabled))
            }
    }
}
