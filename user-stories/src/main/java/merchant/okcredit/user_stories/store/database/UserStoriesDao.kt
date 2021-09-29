package merchant.okcredit.user_stories.store.database

import `in`.okcredit.shared.utils.Timestamp
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.StoriesConstants
import merchant.okcredit.user_stories.contract.model.UserStories
import tech.okcredit.android.base.utils.DateTimeUtils

@Dao
interface UserStoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyStory(myStoryList: List<MyStory>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOtherStory(othersStories: List<OthersStory>): Completable

    @Query("SELECT MAX(createdAt) FROM MyStory WHERE businessId = :businessId")
    fun getLastSyncMyStoryTimestamp(businessId: String): Single<Timestamp>

    @Query("SELECT MAX(createdAt) FROM OthersStory WHERE businessId = :businessId ")
    fun getLastSyncOtherStoryTimestamp(businessId: String): Single<Timestamp>

    @Query(
        """ SELECT  os1.accountId AS id,
        '${StoriesConstants.RELATIONSHIP_KNOWN}' AS type ,
        os1.name AS name ,
        os1.relationship,
        os1.storyType,
        IFNULL(os2.storyId,os3.storyId) storyId ,
        MAX(os1.createdAt) recentCreatedAt ,
        IFNULL(os2.imageUrlThumbnail,os3.imageUrlThumbnail) AS leastUnseenImageUrl ,
        COUNT(os1.viewed) totalStories ,
        SUM(os1.viewed) totalSeen,
        CASE WHEN count (os1.viewed)= sum (os1.viewed) THEN 1  ELSE  0 END AS allViewed
        FROM
        OthersStory AS os1
        LEFT JOIN
        (SELECT accountId,
        min(createdAt) createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        WHERE businessId = :businessId AND viewed = 0 AND handle= '${StoriesConstants.HANDLE_USER}'
        AND relationship !='${StoriesConstants.RELATIONSHIP_UNKNOWN}'
        AND deleted = 0 AND
        expiresAt  > :currentTimestamp
        GROUP BY accountId) AS os2
        ON  os1.accountId = os2.accountId
        LEFT JOIN
        (SELECT accountId, min(createdAt) createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        WHERE businessId = :businessId AND viewed = 1 AND  handle= '${StoriesConstants.HANDLE_USER}'
        AND relationship != '${StoriesConstants.RELATIONSHIP_UNKNOWN}' AND deleted = 0
        AND expiresAt  > :currentTimestamp GROUP BY accountId) AS os3
        ON  os1.accountId = os3.accountId
        WHERE os1.handle = '${StoriesConstants.HANDLE_USER}'
        AND os1.relationship != '${StoriesConstants.RELATIONSHIP_UNKNOWN}'
        AND os1.deleted = 0 AND  expiresAt  > :currentTimestamp
        GROUP BY os1.accountId

        UNION ALL

        SELECT  os1.mobile AS id,
        '${StoriesConstants.RELATIONSHIP_UNKNOWN}' AS type ,
        IFNULL(os1.localName , os1.name) AS name ,
        os1.relationship,
        os1.storyType,
        IFNULL(os2.storyId,os3.storyId) storyId,
        MAX(os1.createdAt) recentCreatedAt ,
        IFNULL(os2.imageUrlThumbnail,os3.imageUrlThumbnail) AS leastUnseenImageUrl ,
        COUNT(os1.viewed) totalStories ,
        SUM(os1.viewed) totalSeen,
        CASE WHEN count (os1.viewed)= sum (os1.viewed) THEN 1  ELSE  0 END AS allViewed
        FROM
        OthersStory AS os1
        LEFT JOIN
        (SELECT mobile, min(createdAt)
        createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        WHERE  businessId = :businessId AND viewed = 0 AND handle= '${StoriesConstants.HANDLE_USER}'
        AND relationship ='${StoriesConstants.RELATIONSHIP_UNKNOWN}' AND deleted = 0 AND
        expiresAt  > :currentTimestamp
        GROUP BY mobile) AS os2
        ON  os1.mobile = os2.mobile
        LEFT JOIN
        (SELECT mobile,
        min(createdAt) createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        where  businessId = :businessId AND viewed = 1 and
        handle= '${StoriesConstants.HANDLE_USER}' AND relationship ='${StoriesConstants.RELATIONSHIP_UNKNOWN}'
        AND deleted = 0 AND  expiresAt  > :currentTimestamp
        GROUP BY mobile) AS os3
        ON  os1.mobile = os3.mobile
        WHERE os1.handle = '${StoriesConstants.HANDLE_USER}'
        AND os1.relationship = '${StoriesConstants.RELATIONSHIP_UNKNOWN}'
        AND os1.deleted = 0 AND  expiresAt  > :currentTimestamp
        GROUP BY os1.mobile

        UNION ALL

        SELECT  os1.name AS id,
        '${StoriesConstants.HANDLE_VENDOR}' AS type ,
        os1.name AS name ,
        os1.relationship,
        os1.storyType,
        IFNULL(os2.storyId,os3.storyId) storyId,
        MAX(os1.createdAt) recentCreatedAt ,
        IFNULL(os2.imageUrlThumbnail,os3.imageUrlThumbnail) AS leastUnseenImageUrl ,
        COUNT(os1.viewed) totalStories ,
        SUM(os1.viewed) totalSeen,
        CASE WHEN count (os1.viewed)= sum (os1.viewed) THEN 1  ELSE  0 END AS allViewed
        FROM
        OthersStory AS os1
        LEFT JOIN
        (SELECT name, min(createdAt) createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        WHERE  businessId = :businessId AND viewed = 0
        AND handle= '${StoriesConstants.HANDLE_VENDOR}'
        AND deleted = 0
        AND  expiresAt  > :currentTimestamp GROUP BY name) AS os2
        ON  os1.name = os2.name
        LEFT JOIN
        (SELECT name, min(createdAt) createdAt,
        storyId,
        imageUrlThumbnail
        FROM OthersStory
        WHERE  businessId = :businessId AND viewed = 1 AND  handle= '${StoriesConstants.HANDLE_VENDOR}'
        AND deleted = 0 AND  expiresAt  > :currentTimestamp
        GROUP BY name) AS os3
        ON  os1.name = os3.name
        WHERE os1.businessId = :businessId AND os1.handle = '${StoriesConstants.HANDLE_VENDOR}'
        AND os1.deleted = 0 AND  expiresAt  > :currentTimestamp
        GROUP BY os1.name
        ORDER BY
        allViewed ASC,
        recentCreatedAt DESC
        """
    )
    fun getOtherStoryUserGroup(currentTimestamp: Long = DateTimeUtils.currentDateTime().millis, businessId: String): Observable<List<UserStories>>

    fun getDistinctOtherStoryGroup(businessId: String): Observable<List<UserStories>> =
        getOtherStoryUserGroup(businessId = businessId).distinctUntilChanged()

    @Query(
        """
            SELECT
            IFNULL(medialLocalUrl,imageUrlThumbnail) latestImageUrl,
            ms2.createdAt,
            ms2.allSynced,
            1 AS isMyStoryAdded
            FROM MyStory ms1
            JOIN

            (SELECT
            max(createdAt) AS createdAt,
            CASE WHEN count(synced)=sum(synced) THEN 1 ELSE 0 END AS allSynced
            FROM  MyStory  WHERE businessId = :businessId AND expiresAt > :currentTimestamp
            AND deleted=0) ms2
            ON ms1.createdAt=ms2.createdAt
            WHERE ms1.businessId = :businessId AND expiresAt > :currentTimestamp
            AND deleted=0
        """
    )
    fun getMyStoryHome(currentTimestamp: Long = DateTimeUtils.currentDateTime().millis, businessId: String): Observable<List<MyStoryHome>>

    fun getDistinctMyStoryHome(businessId: String): Observable<List<MyStoryHome>> =
        getMyStoryHome(businessId = businessId).distinctUntilChanged()

    @Query(
        """SELECT * FROM
                 othersstory
                 WHERE businessId = :businessId
                 AND relationship= :relationship
                 AND expiresAt > :currentTimestamp
                 AND deleted = 0
                 """
    )
    fun getOthersStoryByRelationShip(
        relationship: String = StoriesConstants.RELATIONSHIP_UNKNOWN,
        currentTimestamp: Long = DateTimeUtils.currentDateTime().millis,
        businessId: String
    ): Single<List<OthersStory>>

    @Query("SELECT * FROM mystory where businessId = :businessId and synced =0 ")
    fun getUnSyncedMyStory(businessId: String): Single<List<MyStory>>

    @Query(
        """SELECT count(1) FROM mystory where businessId = :businessId and
        ( expiresAt < :currentTimestamp OR expiresAt is NULL )
        AND deleted =0
    """
    )
    fun getActiveCountMyStory(currentTimestamp: Long, businessId: String): Single<Int>
}
