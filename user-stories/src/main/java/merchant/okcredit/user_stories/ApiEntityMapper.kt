package merchant.okcredit.user_stories

import `in`.okcredit.shared.utils.Timestamp
import com.google.common.base.Converter
import merchant.okcredit.user_stories.server.UserStoriesApiMessage
import merchant.okcredit.user_stories.store.database.MyStory
import merchant.okcredit.user_stories.store.database.OthersStory

object ApiEntityMapper {
    fun MY_STORY(businessId: String): Converter<UserStoriesApiMessage.MyStory, MyStory> =

        object : Converter<UserStoriesApiMessage.MyStory, MyStory>() {
            override fun doForward(a: UserStoriesApiMessage.MyStory): MyStory {
                return MyStory(
                    // TODO:: will remove else part once api is fixed
                    requestId = a.request_id ?: a.status_id,
                    storyId = a.status_id,
                    mediaId = a.media_id,
                    mediaType = a.media_type,
                    caption = a.caption,
                    views = a.views,
                    createdAt = Timestamp(a.created_at!!.toLong()),
                    expiresAt = Timestamp(a.expires_at!!.toLong()),
                    deleted = a.deleted,
                    imageUrlThumbnail = a.urls.thumbnail,
                    imageUrlMedium = a.urls.medium,
                    synced = true,
                    businessId = businessId
                )
            }

            override fun doBackward(b: MyStory): UserStoriesApiMessage.MyStory {
                TODO("Not yet implemented")
            }
        }

    fun OTHERS_STORY(businessId: String): Converter<UserStoriesApiMessage.OthersStory, OthersStory> =

        object : Converter<UserStoriesApiMessage.OthersStory, OthersStory>() {
            override fun doForward(a: UserStoriesApiMessage.OthersStory): OthersStory {
                return OthersStory(
                    storyId = a.storyId,
                    accountId = a.account_id,
                    name = a.name,
                    handle = a.handle,
                    profilePic = a.profile_pic,
                    mediaId = a.media_id,
                    mediaType = a.media_type,
                    caption = a.caption,
                    relationship = a.relationship,
                    createdAt = Timestamp(a.created_at.toLong()),
                    expiresAt = Timestamp(a.expires_at.toLong()),
                    viewed = a.viewed,
                    deleted = a.deleted,
                    link = a.link,
                    mobile = a.mobile,
                    imageUrlThumbnail = a.urls.thumbnail,
                    imageUrlMedium = a.urls.medium,
                    storyType = a.storyType,
                    businessId = businessId
                )
            }

            override fun doBackward(b: OthersStory): UserStoriesApiMessage.OthersStory {
                TODO("Not yet implemented")
            }
        }

    fun ADD_STORY(businessId: String): Converter<UserStoriesApiMessage.AddMyStatus, MyStory> =
        object : Converter<UserStoriesApiMessage.AddMyStatus, MyStory>() {
            override fun doForward(a: UserStoriesApiMessage.AddMyStatus): MyStory {
                return MyStory(
                    requestId = a.request_id,
                    uploadAt = Timestamp(a.updatedAt),
                    medialLocalUrl = a.media_url,
                    mediaType = "Image",
                    caption = a.caption,
                    deleted = false,
                    imageUrlThumbnail = a.media_url,
                    imageUrlMedium = a.media_url,
                    synced = false,
                    businessId = businessId
                )
            }

            override fun doBackward(b: MyStory): UserStoriesApiMessage.AddMyStatus {
                return UserStoriesApiMessage.AddMyStatus(
                    request_id = b.requestId,
                    updatedAt = b.uploadAt!!.epoch,
                    caption = b.caption,
                    media_url = b.medialLocalUrl!!
                )
            }
        }
}
