package merchant.okcredit.user_stories.server

import dagger.Lazy
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserStoriesRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<UserStoriesApiClient>,
) : UserStoriesRemoteSource {

    override fun getMyStory(startTime: Long?, businessId: String): Single<List<UserStoriesApiMessage.MyStory>> {
        return apiClient.get().getMyStory(startTime, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    Timber.i("my-status" + response.body())
                    return@map response.body()!!.response
                } else {
                    throw response.asError()
                }
            }
    }

    override fun getOthersStory(startTime: Long?, businessId: String): Single<List<UserStoriesApiMessage.OthersStory>> {
        return apiClient.get().getOtherStory(startTime, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    Timber.i("others-status" + response.body())
                    return@map response.body()!!.response
                } else {
                    throw response.asError()
                }
            }
    }

    override fun postStory(addStory: UserStoriesApiMessage.AddMyStatus?, businessId: String): Single<UserStoriesApiMessage.MyStory> {
        val file = File(addStory!!.media_url)
        val filePart =
            MultipartBody.Part.createFormData("media", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
        return apiClient.get().postStory(
            request_id = addStory.request_id.toRequestBody("text/plain".toMediaType()),
            media = filePart,
            upload_time = addStory.updatedAt.toString().toRequestBody("text/plain".toMediaType()),
            caption = addStory.caption?.toRequestBody("text/plain".toMediaType()),
            businessId = businessId
        ).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    Timber.i("saved response my story ${response.body()}")
                    return@map response.body()!!.response
                } else {
                    throw response.asError()
                }
            }
    }
}
