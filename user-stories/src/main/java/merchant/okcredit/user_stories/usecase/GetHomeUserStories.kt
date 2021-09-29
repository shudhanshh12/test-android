package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.contract.model.HomeStories
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import javax.inject.Inject

class GetHomeUserStories @Inject constructor(
    private val userStoryRepository: Lazy<UserStoryRepository>,
    private val userStoryEnabled: Lazy<UserStoryEnabled>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<Unit, GetHomeUserStories.Response> {
    override fun execute(req: Unit): Observable<`in`.okcredit.shared.usecase.Result<Response>> {
        return userStoryEnabled.get().execute().flatMap {
            if (!it) {
                UseCase.wrapObservable(Observable.just(Response(false, null)))
            } else {
                UseCase.wrapObservable(
                    getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                        Observable.combineLatest(
                            userStoryRepository.get().getGroupUserStories(businessId),
                            userStoryRepository.get().getMyStoryHome(businessId),

                            BiFunction { userStories, myStory ->
                                val myStory = if (myStory.isNotEmpty()) {
                                    myStory[0]
                                } else {
                                    MyStoryHome(
                                        isMyStoryAdded = false, allSynced = false,
                                        latestImageUrl = "", createdAt = ""
                                    )
                                }
                                return@BiFunction Response(
                                    true,
                                    HomeStories(
                                        userStories,
                                        myStory.isMyStoryAdded,
                                        isAllSynced = myStory.allSynced,
                                        lastStoryUrl = myStory.latestImageUrl
                                    )
                                )
                            }
                        )
                    }
                )
            }
        }
    }

    data class Response(
        val isEnabled: Boolean,
        val homeStories: HomeStories?
    )
}
