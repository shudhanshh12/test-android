package merchant.okcredit.user_stories

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.utils.Timestamp
import merchant.okcredit.user_stories.contract.model.HomeStories
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.StoriesConstants
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.server.UserStoriesApiMessage
import merchant.okcredit.user_stories.store.database.MyStory
import merchant.okcredit.user_stories.store.database.OthersStory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object TestData {

    val BUSINESS_ID = "business-id"

    val othersStatus = listOf(
        OthersStory(
            storyId = "asdada",
            accountId = "sddff12123",
            name = "test customer",
            handle = "user",
            profilePic = "https://img.png",
            mediaId = "adaaada",
            mediaType = "image",
            caption = "test caption",
            relationship = "customer",
            createdAt = Timestamp(2313131313421),
            expiresAt = Timestamp(4244442242342),
            viewed = true,
            deleted = false,
            link = "",
            imageUrlThumbnail = "https://test.png",
            imageUrlMedium = "https://test.png",
            localName = "test",
            storyType = "static",
            businessId = BUSINESS_ID
        ),
        OthersStory(
            storyId = "asdada4234",
            accountId = "sddff12123",
            name = "test customer2",
            handle = "user",
            profilePic = "https://img.png",
            mediaId = "adaaada",
            mediaType = "image",
            caption = "test caption",
            relationship = "customer",
            createdAt = Timestamp(2313131313421),
            expiresAt = Timestamp(4244442242342),
            viewed = true,
            deleted = false,
            link = "",
            imageUrlThumbnail = "https://test.png",
            imageUrlMedium = "https://test.png",
            localName = "test",
            storyType = "static",
            businessId = BUSINESS_ID
        )
    )

    val myStatus = listOf(
        MyStory(
            requestId = "31313123",
            storyId = "2442424",
            mediaId = "fsfsfs",
            mediaType = "image",
            medialLocalUrl = "testurl",
            caption = "Test caption",
            views = 10,
            createdAt = Timestamp(3313131313233),
            uploadAt = Timestamp(3131313132343),
            expiresAt = Timestamp(311313131323),
            deleted = true,
            synced = true,
            imageUrlMedium = "testurl",
            imageUrlThumbnail = "testurl1",
            businessId = BUSINESS_ID
        ),
        MyStory(
            requestId = "313131235",
            storyId = "2442424",
            mediaId = "fsfsfs",
            mediaType = "image",
            medialLocalUrl = "testurl",
            caption = "Test caption",
            views = 10,
            createdAt = Timestamp(3313131313233),
            uploadAt = Timestamp(3131313132343),
            expiresAt = Timestamp(311313131323),
            deleted = true,
            synced = true,
            imageUrlMedium = "testurl",
            imageUrlThumbnail = "testurl1",
            businessId = BUSINESS_ID
        )

    )

    val myStory = MyStoryHome(
        "https://external-preview.redd.it/sZ9p13maoqlt83X-TK6EgqOexM2rsI1BtUnmnaVTcsw.png?auto=webp&s=23e92e941d1630d02028f6b9d79d7c371b587c01",
        true,
        "7766513131234",
        false
    )

    val userStoryList = listOf<UserStories>(
        UserStories(
            id = "usesada-2231313n3144",
            type = StoriesConstants.RELATIONSHIP_KNOWN,
            leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
            totalSeen = 5,
            totalStories = 10,
            name = "Test Customer",
            recentCreatedAt = "1213131412152",
            storyId = "dkdfl24729471739",
            allViewed = 0,
            relationship = "customer",
            storyType = "Static"
        ),
        UserStories(
            id = "usesada-2231313n3145",
            type = StoriesConstants.RELATIONSHIP_UNKNOWN,
            leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
            totalSeen = 5,
            totalStories = 10,
            name = "Test User",
            recentCreatedAt = "1213131412152",
            storyId = "rkdfl247294717394",
            allViewed = 1,
            relationship = "unknown",
            storyType = "Static"
        ),
        UserStories(
            id = "usesada-2231313n31435",
            type = StoriesConstants.HANDLE_VENDOR,
            leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
            totalSeen = 5,
            totalStories = 10,
            name = "Test User",
            recentCreatedAt = "1213131412152",
            storyId = "tkdfl2472947173394",
            allViewed = 1,
            relationship = "vendor",
            storyType = "Static"
        )
    )

    val homeStories = HomeStories(
        userStories = userStoryList,
        isMyStoryAdded = myStory.isMyStoryAdded,
        isAllSynced = myStory.allSynced,
        lastStoryUrl = myStory.latestImageUrl
    )

    val timestamp = Timestamp(1234567897654)

    const val timestampLong = 1234567897654

    var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

    val merchant = Business(
        id = "id",
        name = "name",
        mobile = "mobile",
        createdAt = dt,
        updateCategory = true,
        updateMobile = true
    )

    private val urls =
        UserStoriesApiMessage.Urls(
            "https://ik.imagekit.io/wyvmhe0uzv5/tr:w-256,h-256/6037b3a662a52c7f762e74a4.jpg?ik-t" +
                "=1614868006&ik-s=af61509e8c3835009afc3d2d6c1546c3c82446db",
            "testMediumUrl"
        )

    private val othersStoryModel = UserStoriesApiMessage.OthersStory(
        storyId = "asdada",
        account_id = "sddff12123",
        name = "test customer",
        handle = "user",
        profile_pic = "https://img.png",
        media_id = "adaaada",
        media_type = "image",
        caption = "test caption",
        relationship = "customer",
        created_at = "2313131313421",
        expires_at = "2313131313421",
        viewed = true,
        deleted = false,
        link = "",
        image_url_thumbnail = "https://test.png",
        image_url_medium = "https://test.png",
        storyType = "static",
        mobile = "7669080764",
        urls = urls,
    )
    val otherStoryResponse = UserStoriesApiMessage.UserStatusListResponse(listOf(othersStoryModel))

    private val myStoryModel = UserStoriesApiMessage.MyStory(
        request_id = "asdf-12345",
        status_id = "asdd-asdf-12345",
        media_type = "image",
        caption = "test caption ",
        created_at = "324123131231",
        deleted = false,
        urls = urls
    )

    val myStoryTestModel = UserStoriesApiMessage.UserStatusListResponse(listOf(myStoryModel))
}
