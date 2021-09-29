package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.user_stories.ApiEntityMapper
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.server.UserStoriesApiMessage
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.camera_contract.CapturedImage
import java.util.*
import javax.inject.Inject

class AddUserStory @Inject constructor(
    private val userStoriesLocalSource: Lazy<UserStoriesLocalSource>,
    private val userStoryRepository: Lazy<UserStoryRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<AddUserStory.Request, Unit> {
    override fun execute(req: Request): Observable<Result<Unit>> {
        val requestObservable = Observable.just(req)
            .observeOn(ThreadUtils.worker())

        return UseCase.wrapCompletable(
            requestObservable.flatMapCompletable { response ->
                val addMyStatus = response.capturedImage.mapIndexed { index, capture ->
                    UserStoriesApiMessage.AddMyStatus(
                        request_id = UUID.randomUUID().toString(),
                        caption = response.captionMap?.get(capture),
                        updatedAt = DateTimeUtils.currentDateTime().millis.plus(index + 1),
                        media_url = capture.file.absolutePath
                    )
                }
                getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                    insertToDb(addMyStatus, businessId).andThen(
                        userStoryRepository.get().syncAddStory(businessId)
                    )
                }
            }
        )
    }

    private fun insertToDb(addMyStatus: List<UserStoriesApiMessage.AddMyStatus>, businessId: String): Completable {
        val list = addMyStatus.map { addMyStatus -> ApiEntityMapper.ADD_STORY(businessId).convert(addMyStatus) }
        return userStoriesLocalSource.get().saveMyStory(list.requireNoNulls())
    }

    data class Request(val capturedImage: List<CapturedImage>, val captionMap: HashMap<CapturedImage?, String>?)
}
