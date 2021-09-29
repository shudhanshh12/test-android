package `in`.okcredit.referral.contract

import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.models.ShareContent
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ReferralRepository {

    fun signOut(): Completable

    fun getReferralInfo(): Observable<ReferralInfo>

    fun saveReferralLink(referralLink: String): Completable

    fun getReferralLink(businessId: String): Observable<String>

    fun syncReferralLink(businessId: String): Completable

    fun sync(businessId: String): Completable

    fun checkQualificationJourney(businessId: String): Completable

    fun getQualifiedForJourney(businessId: String): Single<Boolean>

    fun isReferralInAppDisplayed(): Single<Boolean>

    fun setReferralInAppPreference(Shown: Boolean): Completable

    suspend fun getTargetedUsers(businessId: String): List<TargetedUser>

    suspend fun getShareContent(targetedUserId: String? = null, businessId: String): ReferralApiMessages.GetShareContentResponse

    suspend fun syncReferralContent(businessId: String)

    suspend fun setGenericShareContent(response: ReferralApiMessages.GetShareContentResponse)

    suspend fun getGenericShareContent(): ShareContent

    suspend fun getReferralTargets(businessId: String): List<ReferralTargetBanner>

    suspend fun syncShareAppImage()

    suspend fun setTargetBannerCloseTime(currentTimeMillis: Long)

    suspend fun getTargetBannerCloseTime(): Long

    suspend fun setTransactionInitiatedTime(currentTimeMillis: Long)

    suspend fun getTransactionInitiatedTime(): Long

    suspend fun shouldShowShareNudge(): Boolean

    suspend fun setShareNudge(shouldShowNudge: Boolean)
}
